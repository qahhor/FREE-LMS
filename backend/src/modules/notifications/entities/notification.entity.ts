import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  ManyToOne,
  CreateDateColumn,
  UpdateDateColumn,
  Index,
} from 'typeorm';
import { User } from '../../users/entities/user.entity';

export enum NotificationType {
  COURSE_ENROLLMENT = 'course_enrollment',
  LESSON_COMPLETED = 'lesson_completed',
  CERTIFICATE_EARNED = 'certificate_earned',
  ACHIEVEMENT_UNLOCKED = 'achievement_unlocked',
  NEW_COMMENT = 'new_comment',
  ASSIGNMENT_DUE = 'assignment_due',
  WEBINAR_REMINDER = 'webinar_reminder',
  SUBSCRIPTION_EXPIRING = 'subscription_expiring',
  COURSE_UPDATE = 'course_update',
  SYSTEM = 'system',
}

@Entity('notifications')
@Index(['userId', 'read'])
@Index(['userId', 'createdAt'])
export class Notification {
  @PrimaryGeneratedColumn()
  id: number;

  @Column()
  @Index()
  userId: number;

  @ManyToOne(() => User, { onDelete: 'CASCADE' })
  user: User;

  @Column({
    type: 'enum',
    enum: NotificationType,
  })
  @Index()
  type: NotificationType;

  @Column()
  title: string;

  @Column('text')
  message: string;

  @Column({ nullable: true })
  actionUrl: string;

  @Column({ type: 'jsonb', nullable: true })
  metadata: Record<string, any>;

  @Column({ default: false })
  read: boolean;

  @Column({ nullable: true })
  readAt: Date;

  @Column({ default: false })
  sent: boolean;

  @Column({ nullable: true })
  sentAt: Date;

  @CreateDateColumn()
  createdAt: Date;

  @UpdateDateColumn()
  updatedAt: Date;
}
