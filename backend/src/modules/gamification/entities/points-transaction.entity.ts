import { Entity, Column, ManyToOne, JoinColumn, Index } from 'typeorm';
import { BaseEntity } from '../../../common/entities/base.entity';
import { User } from '../../users/entities/user.entity';

export enum TransactionType {
  LESSON_COMPLETED = 'lesson_completed',
  QUIZ_PASSED = 'quiz_passed',
  COURSE_COMPLETED = 'course_completed',
  BADGE_UNLOCKED = 'badge_unlocked',
  STREAK_BONUS = 'streak_bonus',
  DAILY_LOGIN = 'daily_login',
  COMMENT_POSTED = 'comment_posted',
  MANUAL_ADJUSTMENT = 'manual_adjustment',
}

/**
 * Points transaction history
 */
@Entity('points_transactions')
@Index(['userId', 'createdAt'])
export class PointsTransaction extends BaseEntity {
  @Column({ name: 'user_id' })
  userId: number;

  @ManyToOne(() => User)
  @JoinColumn({ name: 'user_id' })
  user: User;

  @Column({ type: 'int' })
  points: number; // Can be positive or negative

  @Column({
    type: 'enum',
    enum: TransactionType,
  })
  type: TransactionType;

  @Column({ type: 'text', nullable: true })
  description?: string;

  @Column({ name: 'reference_id', type: 'int', nullable: true })
  referenceId?: number; // ID of related entity (course, quiz, etc.)

  @Column({ name: 'reference_type', type: 'varchar', length: 50, nullable: true })
  referenceType?: string; // 'course', 'quiz', 'badge', etc.

  @Column({ name: 'balance_after', type: 'int' })
  balanceAfter: number; // User's total points after this transaction
}
