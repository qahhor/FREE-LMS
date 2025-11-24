import { Entity, Column, ManyToOne, JoinColumn, Index } from 'typeorm';
import { BaseEntity } from '../../../common/entities/base.entity';
import { User } from '../../users/entities/user.entity';
import { Video } from './video.entity';

/**
 * Tracks user progress for each video
 * Stores last watched position and completion status
 */
@Entity('video_progress')
@Index(['userId', 'videoId'], { unique: true })
export class VideoProgress extends BaseEntity {
  @Column({ name: 'user_id' })
  userId: number;

  @ManyToOne(() => User)
  @JoinColumn({ name: 'user_id' })
  user: User;

  @Column({ name: 'video_id' })
  videoId: number;

  @ManyToOne(() => Video, (video) => video.progress)
  @JoinColumn({ name: 'video_id' })
  video: Video;

  // Last watched position in seconds
  @Column({ name: 'last_position', type: 'int', default: 0 })
  lastPosition: number;

  // Total watch time in seconds
  @Column({ name: 'watch_time', type: 'int', default: 0 })
  watchTime: number;

  // Completion percentage
  @Column({ name: 'completion_percentage', type: 'decimal', precision: 5, scale: 2, default: 0 })
  completionPercentage: number;

  // Is video completed (watched >90%)
  @Column({ name: 'is_completed', type: 'boolean', default: false })
  isCompleted: boolean;

  // Last watched date
  @Column({ name: 'last_watched_at', type: 'timestamp', nullable: true })
  lastWatchedAt?: Date;

  // Completed date
  @Column({ name: 'completed_at', type: 'timestamp', nullable: true })
  completedAt?: Date;
}
