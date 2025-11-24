import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Video, VideoStatus } from './entities/video.entity';
import { VideoProgress } from './entities/video-progress.entity';

/**
 * Service for managing videos and video progress
 * Handles video streaming, HLS delivery, and progress tracking
 */
@Injectable()
export class VideoService {
  constructor(
    @InjectRepository(Video)
    private videoRepository: Repository<Video>,
    @InjectRepository(VideoProgress)
    private videoProgressRepository: Repository<VideoProgress>,
  ) {}

  /**
   * Get video by ID with access validation
   * Returns video with streaming URL (HLS master playlist)
   */
  async findById(id: number, userId?: number): Promise<Video> {
    const video = await this.videoRepository.findOne({
      where: { id },
      relations: ['lesson', 'lesson.module', 'lesson.module.course'],
    });

    if (!video) {
      throw new NotFoundException('Video not found');
    }

    // TODO: Check user access to course/lesson
    // For now, return video if it's ready
    if (video.status !== VideoStatus.READY) {
      throw new NotFoundException('Video is not ready for streaming');
    }

    return video;
  }

  /**
   * Get video streaming URL with access token
   * Returns signed URL for HLS streaming
   */
  async getStreamingUrl(videoId: number, userId: number): Promise<string> {
    const video = await this.findById(videoId, userId);

    // Generate time-limited access token
    const token = this.generateStreamingToken(videoId, userId);

    // Return HLS master playlist URL with token
    return `${video.hlsUrl}?token=${token}`;
  }

  /**
   * Update video progress
   * Tracks user's watching progress and calculates completion
   */
  async updateProgress(
    userId: number,
    videoId: number,
    position: number,
    watchTime?: number,
  ): Promise<VideoProgress> {
    const video = await this.findById(videoId);

    let progress = await this.videoProgressRepository.findOne({
      where: { userId, videoId },
    });

    if (!progress) {
      progress = this.videoProgressRepository.create({
        userId,
        videoId,
        lastPosition: position,
        watchTime: watchTime || 0,
        lastWatchedAt: new Date(),
      });
    } else {
      progress.lastPosition = position;
      progress.watchTime = (progress.watchTime || 0) + (watchTime || 0);
      progress.lastWatchedAt = new Date();
    }

    // Calculate completion percentage
    if (video.duration > 0) {
      progress.completionPercentage = Math.min(
        100,
        (position / video.duration) * 100,
      );

      // Mark as completed if watched >90%
      if (progress.completionPercentage >= 90 && !progress.isCompleted) {
        progress.isCompleted = true;
        progress.completedAt = new Date();
      }
    }

    return this.videoProgressRepository.save(progress);
  }

  /**
   * Get user's progress for a video
   */
  async getProgress(userId: number, videoId: number): Promise<VideoProgress | null> {
    return this.videoProgressRepository.findOne({
      where: { userId, videoId },
    });
  }

  /**
   * Increment view count
   */
  async incrementViewCount(videoId: number): Promise<void> {
    await this.videoRepository.increment({ id: videoId }, 'viewCount', 1);
  }

  /**
   * Generate streaming access token
   * In production, use JWT with expiration
   */
  private generateStreamingToken(videoId: number, userId: number): string {
    // TODO: Implement JWT token generation
    const payload = {
      videoId,
      userId,
      expiresAt: Date.now() + 3600000, // 1 hour
    };
    return Buffer.from(JSON.stringify(payload)).toString('base64');
  }

  /**
   * Validate streaming token
   */
  validateStreamingToken(token: string): { videoId: number; userId: number } {
    try {
      const payload = JSON.parse(Buffer.from(token, 'base64').toString());
      if (payload.expiresAt < Date.now()) {
        throw new Error('Token expired');
      }
      return { videoId: payload.videoId, userId: payload.userId };
    } catch (error) {
      throw new Error('Invalid token');
    }
  }
}
