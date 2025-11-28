package com.freelms.media.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for FFmpeg video processing operations.
 */
@Service
public class FFmpegService {

    private static final Logger log = LoggerFactory.getLogger(FFmpegService.class);

    @Value("${media.ffmpeg.path:ffmpeg}")
    private String ffmpegPath;

    @Value("${media.ffprobe.path:ffprobe}")
    private String ffprobePath;

    @Value("${media.temp-dir:/tmp/media-processing}")
    private String tempDir;

    /**
     * Get video metadata using ffprobe.
     */
    public VideoMetadata getVideoMetadata(String inputPath) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    ffprobePath,
                    "-v", "quiet",
                    "-print_format", "json",
                    "-show_format",
                    "-show_streams",
                    inputPath
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
            }

            process.waitFor();

            // Parse JSON output (simplified - would use Jackson in real impl)
            return parseMetadata(output.toString());
        } catch (Exception e) {
            log.error("Error getting video metadata: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get video metadata", e);
        }
    }

    /**
     * Transcode video to HLS format with multiple quality levels.
     */
    @Async
    public CompletableFuture<TranscodeResult> transcodeToHLS(String inputPath, String outputDir,
                                                              ProgressCallback callback) {
        try {
            Files.createDirectories(Path.of(outputDir));

            // Define quality levels (height, bitrate)
            int[][] qualities = {
                    {1080, 5000},  // 1080p
                    {720, 2500},   // 720p
                    {480, 1200},   // 480p
                    {360, 800}     // 360p
            };

            List<String> playlistEntries = new ArrayList<>();

            for (int[] quality : qualities) {
                int height = quality[0];
                int bitrate = quality[1];

                String qualityDir = outputDir + "/" + height + "p";
                Files.createDirectories(Path.of(qualityDir));

                List<String> command = buildHLSCommand(inputPath, qualityDir, height, bitrate);

                ProcessBuilder pb = new ProcessBuilder(command);
                pb.redirectErrorStream(true);
                Process process = pb.start();

                // Monitor progress
                monitorProgress(process, callback, height + "p");

                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    throw new RuntimeException("FFmpeg failed with exit code: " + exitCode);
                }

                playlistEntries.add(String.format(
                        "#EXT-X-STREAM-INF:BANDWIDTH=%d000,RESOLUTION=%dx%d\n%dp/playlist.m3u8",
                        bitrate, (int)(height * 16.0 / 9), height, height
                ));
            }

            // Create master playlist
            createMasterPlaylist(outputDir, playlistEntries);

            TranscodeResult result = new TranscodeResult();
            result.setSuccess(true);
            result.setOutputPath(outputDir);
            result.setMasterPlaylistPath(outputDir + "/master.m3u8");

            return CompletableFuture.completedFuture(result);

        } catch (Exception e) {
            log.error("Transcoding failed: {}", e.getMessage(), e);
            TranscodeResult result = new TranscodeResult();
            result.setSuccess(false);
            result.setError(e.getMessage());
            return CompletableFuture.completedFuture(result);
        }
    }

    /**
     * Generate thumbnails from video.
     */
    public ThumbnailResult generateThumbnails(String inputPath, String outputDir) {
        try {
            Files.createDirectories(Path.of(outputDir));

            // Thumbnail sizes
            String[][] sizes = {
                    {"small", "160x90"},
                    {"medium", "320x180"},
                    {"large", "640x360"}
            };

            ThumbnailResult result = new ThumbnailResult();

            for (String[] size : sizes) {
                String name = size[0];
                String dimensions = size[1];
                String outputPath = outputDir + "/" + name + ".jpg";

                List<String> command = List.of(
                        ffmpegPath,
                        "-i", inputPath,
                        "-ss", "00:00:05",  // 5 seconds into video
                        "-vframes", "1",
                        "-vf", "scale=" + dimensions.replace("x", ":"),
                        "-y",
                        outputPath
                );

                ProcessBuilder pb = new ProcessBuilder(command);
                pb.redirectErrorStream(true);
                Process process = pb.start();
                process.waitFor();

                result.addThumbnail(name, outputPath);
            }

            // Generate sprite sheet for video preview
            generateSpriteSheet(inputPath, outputDir + "/sprite.jpg");
            result.setSpriteSheetPath(outputDir + "/sprite.jpg");

            result.setSuccess(true);
            return result;

        } catch (Exception e) {
            log.error("Thumbnail generation failed: {}", e.getMessage(), e);
            ThumbnailResult result = new ThumbnailResult();
            result.setSuccess(false);
            result.setError(e.getMessage());
            return result;
        }
    }

    /**
     * Extract audio from video (for transcription).
     */
    public String extractAudio(String inputPath, String outputDir) {
        try {
            Files.createDirectories(Path.of(outputDir));
            String outputPath = outputDir + "/audio.wav";

            List<String> command = List.of(
                    ffmpegPath,
                    "-i", inputPath,
                    "-vn",
                    "-acodec", "pcm_s16le",
                    "-ar", "16000",
                    "-ac", "1",
                    "-y",
                    outputPath
            );

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            process.waitFor();

            return outputPath;

        } catch (Exception e) {
            log.error("Audio extraction failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to extract audio", e);
        }
    }

    private List<String> buildHLSCommand(String inputPath, String outputDir, int height, int bitrate) {
        return List.of(
                ffmpegPath,
                "-i", inputPath,
                "-vf", "scale=-2:" + height,
                "-c:v", "libx264",
                "-preset", "fast",
                "-b:v", bitrate + "k",
                "-maxrate", (int)(bitrate * 1.5) + "k",
                "-bufsize", (bitrate * 2) + "k",
                "-c:a", "aac",
                "-b:a", "128k",
                "-hls_time", "6",
                "-hls_playlist_type", "vod",
                "-hls_segment_filename", outputDir + "/segment_%03d.ts",
                "-y",
                outputDir + "/playlist.m3u8"
        );
    }

    private void createMasterPlaylist(String outputDir, List<String> entries) throws Exception {
        StringBuilder playlist = new StringBuilder();
        playlist.append("#EXTM3U\n");
        playlist.append("#EXT-X-VERSION:3\n");
        for (String entry : entries) {
            playlist.append(entry).append("\n");
        }
        Files.writeString(Path.of(outputDir + "/master.m3u8"), playlist.toString());
    }

    private void generateSpriteSheet(String inputPath, String outputPath) throws Exception {
        // Generate a 10x5 sprite sheet (50 thumbnails)
        List<String> command = List.of(
                ffmpegPath,
                "-i", inputPath,
                "-vf", "fps=1/10,scale=160:90,tile=10x5",
                "-frames:v", "1",
                "-y",
                outputPath
        );

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        process.waitFor();
    }

    private void monitorProgress(Process process, ProgressCallback callback, String quality) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            Pattern durationPattern = Pattern.compile("Duration: (\\d+):(\\d+):(\\d+)");
            Pattern timePattern = Pattern.compile("time=(\\d+):(\\d+):(\\d+)");
            int totalSeconds = 0;

            while ((line = reader.readLine()) != null) {
                Matcher durationMatcher = durationPattern.matcher(line);
                if (durationMatcher.find()) {
                    totalSeconds = Integer.parseInt(durationMatcher.group(1)) * 3600
                            + Integer.parseInt(durationMatcher.group(2)) * 60
                            + Integer.parseInt(durationMatcher.group(3));
                }

                Matcher timeMatcher = timePattern.matcher(line);
                if (timeMatcher.find() && totalSeconds > 0) {
                    int currentSeconds = Integer.parseInt(timeMatcher.group(1)) * 3600
                            + Integer.parseInt(timeMatcher.group(2)) * 60
                            + Integer.parseInt(timeMatcher.group(3));
                    int progress = (int) ((currentSeconds * 100.0) / totalSeconds);
                    if (callback != null) {
                        callback.onProgress(quality, progress);
                    }
                }
            }
        }
    }

    private VideoMetadata parseMetadata(String json) {
        // Simplified parsing - would use Jackson in real implementation
        VideoMetadata metadata = new VideoMetadata();
        metadata.setDurationSeconds(0);
        metadata.setWidth(1920);
        metadata.setHeight(1080);
        metadata.setCodec("h264");
        metadata.setBitrate(5000);
        metadata.setFrameRate(30.0);
        return metadata;
    }

    // Inner classes
    public static class VideoMetadata {
        private int durationSeconds;
        private int width;
        private int height;
        private String codec;
        private int bitrate;
        private double frameRate;

        // Getters and setters
        public int getDurationSeconds() { return durationSeconds; }
        public void setDurationSeconds(int durationSeconds) { this.durationSeconds = durationSeconds; }
        public int getWidth() { return width; }
        public void setWidth(int width) { this.width = width; }
        public int getHeight() { return height; }
        public void setHeight(int height) { this.height = height; }
        public String getCodec() { return codec; }
        public void setCodec(String codec) { this.codec = codec; }
        public int getBitrate() { return bitrate; }
        public void setBitrate(int bitrate) { this.bitrate = bitrate; }
        public double getFrameRate() { return frameRate; }
        public void setFrameRate(double frameRate) { this.frameRate = frameRate; }
    }

    public static class TranscodeResult {
        private boolean success;
        private String outputPath;
        private String masterPlaylistPath;
        private String error;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getOutputPath() { return outputPath; }
        public void setOutputPath(String outputPath) { this.outputPath = outputPath; }
        public String getMasterPlaylistPath() { return masterPlaylistPath; }
        public void setMasterPlaylistPath(String masterPlaylistPath) { this.masterPlaylistPath = masterPlaylistPath; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }

    public static class ThumbnailResult {
        private boolean success;
        private java.util.Map<String, String> thumbnails = new java.util.HashMap<>();
        private String spriteSheetPath;
        private String error;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public java.util.Map<String, String> getThumbnails() { return thumbnails; }
        public void addThumbnail(String name, String path) { thumbnails.put(name, path); }
        public String getSpriteSheetPath() { return spriteSheetPath; }
        public void setSpriteSheetPath(String spriteSheetPath) { this.spriteSheetPath = spriteSheetPath; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }

    @FunctionalInterface
    public interface ProgressCallback {
        void onProgress(String quality, int percent);
    }
}
