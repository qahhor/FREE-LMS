import { Entity, Column, ManyToOne, JoinColumn, OneToMany } from 'typeorm';
import { BaseEntity } from '../../../common/entities/base.entity';
import { Lesson } from '../../courses/entities/lesson.entity';
import { VideoProgress } from './video-progress.entity';

export enum VideoQuality {
  AUTO = 'auto',
  HD_1080 = '1080p',
  HD_720 = '720p',
  SD_480 = '480p',
  SD_360 = '360p',
  SD_240 = '240p',
}

export enum VideoStatus {
  UPLOADING = 'uploading',
  PROCESSING = 'processing',
  READY = 'ready',
  FAILED = 'failed',
}

/**
 * Video entity for storing video metadata and streaming information
 * Supports HLS (HTTP Live Streaming) for adaptive bitrate streaming
 */
@Entity('videos')
export class Video extends BaseEntity {
  @Column({ type: 'varchar', length: 500 })
  title: string;

  @Column({ type: 'varchar', length: 500, nullable: true })
  description?: string;

  // Original video file
  @Column({ name: 'original_url', type: 'varchar', length: 1000 })
  originalUrl: string;

  // HLS master playlist URL
  @Column({ name: 'hls_url', type: 'varchar', length: 1000, nullable: true })
  hlsUrl?: string;

  // Thumbnail image
  @Column({ name: 'thumbnail_url', type: 'varchar', length: 1000, nullable: true })
  thumbnailUrl?: string;

  // Video duration in seconds
  @Column({ type: 'int', default: 0 })
  duration: number;

  // File size in bytes
  @Column({ name: 'file_size', type: 'bigint', default: 0 })
  fileSize: number;

  // Available qualities (JSON array)
  @Column({ type: 'json', nullable: true })
  availableQualities?: VideoQuality[];

  // Processing status
  @Column({
    type: 'enum',
    enum: VideoStatus,
    default: VideoStatus.UPLOADING,
  })
  status: VideoStatus;

  // Subtitle tracks (JSON array)
  @Column({ type: 'json', nullable: true })
  subtitles?: Array<{
    language: string;
    label: string;
    url: string;
  }>;

  // Video protection
  @Column({ name: 'is_downloadable', type: 'boolean', default: false })
  isDownloadable: boolean;

  @Column({ name: 'watermark_enabled', type: 'boolean', default: true })
  watermarkEnabled: boolean;

  // Lesson relation
  @Column({ name: 'lesson_id', nullable: true })
  lessonId?: number;

  @ManyToOne(() => Lesson, { nullable: true })
  @JoinColumn({ name: 'lesson_id' })
  lesson?: Lesson;

  // Progress tracking
  @OneToMany(() => VideoProgress, (progress) => progress.video)
  progress: VideoProgress[];

  // View count
  @Column({ name: 'view_count', type: 'int', default: 0 })
  viewCount: number;
}
