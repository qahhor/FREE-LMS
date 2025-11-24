import {
  Component,
  Input,
  Output,
  EventEmitter,
  OnInit,
  OnDestroy,
  AfterViewInit,
  ViewChild,
  ElementRef,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { environment } from '@environments/environment';
import { interval, Subscription } from 'rxjs';

interface VideoData {
  id: number;
  title: string;
  duration: number;
  hlsUrl: string;
  thumbnailUrl?: string;
  subtitles?: Array<{ language: string; label: string; url: string }>;
}

/**
 * Advanced video player component with HLS support
 * Features:
 * - Adaptive bitrate streaming (HLS)
 * - Progress tracking
 * - Keyboard shortcuts
 * - Picture-in-picture
 * - Playback speed control
 * - Subtitle support
 * - Download protection
 */
@Component({
  selector: 'app-video-player',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="video-player-container" [class.fullscreen]="isFullscreen">
      <video
        #videoElement
        class="video-element"
        [poster]="video?.thumbnailUrl"
        playsinline
        (contextmenu)="onContextMenu($event)"
      ></video>

      <div class="video-controls" [class.visible]="showControls">
        <div class="progress-bar" (click)="seek($event)">
          <div class="progress-buffer" [style.width.%]="bufferProgress"></div>
          <div class="progress-played" [style.width.%]="playedProgress"></div>
          <div class="progress-handle" [style.left.%]="playedProgress"></div>
        </div>

        <div class="control-buttons">
          <button class="control-btn" (click)="togglePlay()">
            <span *ngIf="!isPlaying">▶</span>
            <span *ngIf="isPlaying">⏸</span>
          </button>

          <button class="control-btn" (click)="toggleMute()">
            <span *ngIf="!isMuted">🔊</span>
            <span *ngIf="isMuted">🔇</span>
          </button>

          <input
            type="range"
            class="volume-slider"
            min="0"
            max="100"
            [value]="volume * 100"
            (input)="setVolume($event)"
          />

          <span class="time-display">
            {{ formatTime(currentTime) }} / {{ formatTime(duration) }}
          </span>

          <div class="spacer"></div>

          <select class="playback-rate" (change)="setPlaybackRate($event)">
            <option value="0.5">0.5x</option>
            <option value="0.75">0.75x</option>
            <option value="1" selected>1x</option>
            <option value="1.25">1.25x</option>
            <option value="1.5">1.5x</option>
            <option value="2">2x</option>
          </select>

          <select class="quality-selector" (change)="setQuality($event)">
            <option value="auto" selected>Auto</option>
            <option value="1080p">1080p</option>
            <option value="720p">720p</option>
            <option value="480p">480p</option>
            <option value="360p">360p</option>
          </select>

          <button class="control-btn" (click)="togglePip()" *ngIf="pipSupported">
            📺
          </button>

          <button class="control-btn" (click)="toggleFullscreen()">
            <span *ngIf="!isFullscreen">⛶</span>
            <span *ngIf="isFullscreen">⛶</span>
          </button>
        </div>
      </div>

      <div class="loading-spinner" *ngIf="isLoading">
        <div class="spinner"></div>
      </div>

      <div class="watermark" *ngIf="showWatermark">
        {{ watermarkText }}
      </div>
    </div>
  `,
  styles: [`
    .video-player-container {
      position: relative;
      width: 100%;
      max-width: 100%;
      background: #000;
      aspect-ratio: 16 / 9;
    }

    .video-player-container.fullscreen {
      position: fixed;
      top: 0;
      left: 0;
      width: 100vw;
      height: 100vh;
      z-index: 9999;
      aspect-ratio: unset;
    }

    .video-element {
      width: 100%;
      height: 100%;
      object-fit: contain;
    }

    .video-controls {
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      background: linear-gradient(transparent, rgba(0, 0, 0, 0.8));
      padding: 20px 10px 10px;
      opacity: 0;
      transition: opacity 0.3s;
    }

    .video-controls.visible {
      opacity: 1;
    }

    .progress-bar {
      position: relative;
      height: 6px;
      background: rgba(255, 255, 255, 0.3);
      cursor: pointer;
      margin-bottom: 10px;
      border-radius: 3px;
    }

    .progress-buffer,
    .progress-played {
      position: absolute;
      height: 100%;
      border-radius: 3px;
    }

    .progress-buffer {
      background: rgba(255, 255, 255, 0.5);
    }

    .progress-played {
      background: #2196F3;
    }

    .progress-handle {
      position: absolute;
      top: 50%;
      transform: translate(-50%, -50%);
      width: 14px;
      height: 14px;
      background: #2196F3;
      border-radius: 50%;
      border: 2px solid white;
    }

    .control-buttons {
      display: flex;
      align-items: center;
      gap: 10px;
      color: white;
    }

    .control-btn {
      background: none;
      border: none;
      color: white;
      font-size: 20px;
      cursor: pointer;
      padding: 5px 10px;
      transition: opacity 0.2s;
    }

    .control-btn:hover {
      opacity: 0.8;
    }

    .volume-slider {
      width: 80px;
    }

    .time-display {
      font-size: 14px;
      user-select: none;
    }

    .spacer {
      flex: 1;
    }

    .playback-rate,
    .quality-selector {
      background: rgba(255, 255, 255, 0.1);
      color: white;
      border: 1px solid rgba(255, 255, 255, 0.3);
      padding: 5px;
      border-radius: 4px;
      cursor: pointer;
    }

    .loading-spinner {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
    }

    .spinner {
      width: 50px;
      height: 50px;
      border: 4px solid rgba(255, 255, 255, 0.3);
      border-top-color: white;
      border-radius: 50%;
      animation: spin 1s linear infinite;
    }

    @keyframes spin {
      to { transform: rotate(360deg); }
    }

    .watermark {
      position: absolute;
      bottom: 60px;
      right: 20px;
      color: rgba(255, 255, 255, 0.5);
      font-size: 12px;
      pointer-events: none;
      user-select: none;
    }
  `],
})
export class VideoPlayerComponent implements OnInit, AfterViewInit, OnDestroy {
  @Input() videoId!: number;
  @Input() enrollmentId?: number;
  @Input() showWatermark = true;
  @Input() watermarkText = 'FREE LMS';
  @Output() completed = new EventEmitter<void>();
  @Output() progress = new EventEmitter<number>();

  @ViewChild('videoElement') videoElement!: ElementRef<HTMLVideoElement>;

  video?: VideoData;
  isLoading = true;
  isPlaying = false;
  isMuted = false;
  isFullscreen = false;
  showControls = false;
  pipSupported = false;

  currentTime = 0;
  duration = 0;
  volume = 1;
  playedProgress = 0;
  bufferProgress = 0;

  private hls: any;
  private progressSubscription?: Subscription;
  private controlsTimeout?: any;
  private lastProgressUpdate = 0;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.pipSupported = 'pictureInPictureEnabled' in document;
    this.loadVideo();
  }

  ngAfterViewInit(): void {
    this.setupEventListeners();
  }

  ngOnDestroy(): void {
    this.cleanup();
  }

  private async loadVideo(): Promise<void> {
    try {
      const video = await this.http
        .get<VideoData>(`${environment.apiUrl}/videos/${this.videoId}`)
        .toPromise();

      if (!video) return;

      this.video = video;
      await this.initializeHls();

      // Load saved progress
      this.loadProgress();
    } catch (error) {
      console.error('Error loading video:', error);
    }
  }

  private async initializeHls(): Promise<void> {
    if (!this.video?.hlsUrl) return;

    const video = this.videoElement.nativeElement;

    // Check if HLS is natively supported (Safari)
    if (video.canPlayType('application/vnd.apple.mpegurl')) {
      video.src = this.video.hlsUrl;
      this.isLoading = false;
    } else {
      // Use hls.js for other browsers
      const Hls = (await import('hls.js')).default;

      if (Hls.isSupported()) {
        this.hls = new Hls({
          enableWorker: true,
          lowLatencyMode: true,
        });

        this.hls.loadSource(this.video.hlsUrl);
        this.hls.attachMedia(video);

        this.hls.on(Hls.Events.MANIFEST_PARSED, () => {
          this.isLoading = false;
        });

        this.hls.on(Hls.Events.ERROR, (event: any, data: any) => {
          console.error('HLS error:', data);
        });
      }
    }
  }

  private setupEventListeners(): void {
    const video = this.videoElement.nativeElement;

    video.addEventListener('loadedmetadata', () => {
      this.duration = video.duration;
    });

    video.addEventListener('timeupdate', () => {
      this.currentTime = video.currentTime;
      this.playedProgress = (video.currentTime / video.duration) * 100;
      this.updateProgress();
    });

    video.addEventListener('progress', () => {
      if (video.buffered.length > 0) {
        const buffered = video.buffered.end(video.buffered.length - 1);
        this.bufferProgress = (buffered / video.duration) * 100;
      }
    });

    video.addEventListener('play', () => {
      this.isPlaying = true;
      this.startProgressTracking();
    });

    video.addEventListener('pause', () => {
      this.isPlaying = false;
      this.stopProgressTracking();
    });

    video.addEventListener('ended', () => {
      this.onVideoCompleted();
    });

    // Keyboard shortcuts
    document.addEventListener('keydown', this.handleKeyboard.bind(this));

    // Show/hide controls
    video.addEventListener('mousemove', () => {
      this.showControls = true;
      this.resetControlsTimeout();
    });

    video.addEventListener('mouseleave', () => {
      if (this.isPlaying) {
        this.showControls = false;
      }
    });
  }

  private handleKeyboard(event: KeyboardEvent): void {
    const video = this.videoElement.nativeElement;

    switch (event.key) {
      case ' ':
      case 'k':
        event.preventDefault();
        this.togglePlay();
        break;
      case 'ArrowLeft':
        video.currentTime -= 5;
        break;
      case 'ArrowRight':
        video.currentTime += 5;
        break;
      case 'ArrowUp':
        video.volume = Math.min(1, video.volume + 0.1);
        break;
      case 'ArrowDown':
        video.volume = Math.max(0, video.volume - 0.1);
        break;
      case 'f':
        this.toggleFullscreen();
        break;
      case 'm':
        this.toggleMute();
        break;
    }
  }

  togglePlay(): void {
    const video = this.videoElement.nativeElement;
    if (video.paused) {
      video.play();
    } else {
      video.pause();
    }
  }

  toggleMute(): void {
    const video = this.videoElement.nativeElement;
    video.muted = !video.muted;
    this.isMuted = video.muted;
  }

  setVolume(event: any): void {
    const video = this.videoElement.nativeElement;
    video.volume = event.target.value / 100;
    this.volume = video.volume;
  }

  setPlaybackRate(event: any): void {
    const video = this.videoElement.nativeElement;
    video.playbackRate = parseFloat(event.target.value);
  }

  setQuality(event: any): void {
    const quality = event.target.value;
    // TODO: Implement quality switching with HLS
    console.log('Quality changed to:', quality);
  }

  seek(event: MouseEvent): void {
    const progressBar = event.currentTarget as HTMLElement;
    const rect = progressBar.getBoundingClientRect();
    const percent = (event.clientX - rect.left) / rect.width;
    const video = this.videoElement.nativeElement;
    video.currentTime = percent * video.duration;
  }

  async togglePip(): Promise<void> {
    const video = this.videoElement.nativeElement;
    try {
      if (document.pictureInPictureElement) {
        await document.exitPictureInPicture();
      } else {
        await (video as any).requestPictureInPicture();
      }
    } catch (error) {
      console.error('PiP error:', error);
    }
  }

  async toggleFullscreen(): Promise<void> {
    const container = this.videoElement.nativeElement.parentElement;
    if (!container) return;

    try {
      if (!document.fullscreenElement) {
        await container.requestFullscreen();
        this.isFullscreen = true;
      } else {
        await document.exitFullscreen();
        this.isFullscreen = false;
      }
    } catch (error) {
      console.error('Fullscreen error:', error);
    }
  }

  private startProgressTracking(): void {
    this.progressSubscription = interval(10000).subscribe(() => {
      this.saveProgress();
    });
  }

  private stopProgressTracking(): void {
    this.progressSubscription?.unsubscribe();
    this.saveProgress();
  }

  private updateProgress(): void {
    const now = Date.now();
    if (now - this.lastProgressUpdate > 5000) {
      this.progress.emit(this.currentTime);
      this.lastProgressUpdate = now;
    }

    // Check completion (>90%)
    const completionPercent = (this.currentTime / this.duration) * 100;
    if (completionPercent >= 90) {
      this.onVideoCompleted();
    }
  }

  private async loadProgress(): Promise<void> {
    try {
      const progress = await this.http
        .get<any>(`${environment.apiUrl}/videos/${this.videoId}/progress`)
        .toPromise();

      if (progress?.lastPosition) {
        this.videoElement.nativeElement.currentTime = progress.lastPosition;
      }
    } catch (error) {
      console.error('Error loading progress:', error);
    }
  }

  private async saveProgress(): Promise<void> {
    try {
      await this.http
        .post(`${environment.apiUrl}/videos/${this.videoId}/progress`, {
          position: this.currentTime,
          watchTime: 10, // 10 seconds interval
        })
        .toPromise();
    } catch (error) {
      console.error('Error saving progress:', error);
    }
  }

  private onVideoCompleted(): void {
    this.completed.emit();
  }

  private resetControlsTimeout(): void {
    clearTimeout(this.controlsTimeout);
    this.controlsTimeout = setTimeout(() => {
      if (this.isPlaying) {
        this.showControls = false;
      }
    }, 3000);
  }

  onContextMenu(event: MouseEvent): boolean {
    event.preventDefault();
    return false;
  }

  formatTime(seconds: number): string {
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    const s = Math.floor(seconds % 60);

    if (h > 0) {
      return `${h}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
    }
    return `${m}:${s.toString().padStart(2, '0')}`;
  }

  private cleanup(): void {
    this.stopProgressTracking();
    if (this.hls) {
      this.hls.destroy();
    }
    clearTimeout(this.controlsTimeout);
  }
}
