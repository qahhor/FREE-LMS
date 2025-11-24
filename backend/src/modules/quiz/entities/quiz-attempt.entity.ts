import {
  Entity,
  Column,
  ManyToOne,
  JoinColumn,
  OneToMany,
  Index,
} from 'typeorm';
import { BaseEntity } from '../../../common/entities/base.entity';
import { Quiz } from './quiz.entity';
import { User } from '../../users/entities/user.entity';
import { QuestionResponse } from './question-response.entity';

export enum AttemptStatus {
  IN_PROGRESS = 'in_progress',
  COMPLETED = 'completed',
  ABANDONED = 'abandoned',
  TIME_EXPIRED = 'time_expired',
}

/**
 * Tracks user quiz attempts with timing and scoring
 */
@Entity('quiz_attempts')
@Index(['userId', 'quizId'])
export class QuizAttempt extends BaseEntity {
  @Column({ name: 'user_id' })
  userId: number;

  @ManyToOne(() => User)
  @JoinColumn({ name: 'user_id' })
  user: User;

  @Column({ name: 'quiz_id' })
  quizId: number;

  @ManyToOne(() => Quiz, (quiz) => quiz.attempts)
  @JoinColumn({ name: 'quiz_id' })
  quiz: Quiz;

  @Column({ name: 'attempt_number', type: 'int' })
  attemptNumber: number;

  @Column({
    type: 'enum',
    enum: AttemptStatus,
    default: AttemptStatus.IN_PROGRESS,
  })
  status: AttemptStatus;

  // Timing
  @Column({ name: 'started_at', type: 'timestamp' })
  startedAt: Date;

  @Column({ name: 'submitted_at', type: 'timestamp', nullable: true })
  submittedAt?: Date;

  @Column({ name: 'time_spent', type: 'int', nullable: true })
  timeSpent?: number; // in seconds

  // Scoring
  @Column({ name: 'total_points', type: 'int', default: 0 })
  totalPoints: number;

  @Column({ name: 'earned_points', type: 'float', default: 0 })
  earnedPoints: number;

  @Column({ name: 'score_percentage', type: 'float', default: 0 })
  scorePercentage: number;

  @Column({ name: 'is_passed', type: 'boolean', default: false })
  isPassed: boolean;

  // Question randomization seed (for consistent order during attempt)
  @Column({ name: 'randomization_seed', type: 'int', nullable: true })
  randomizationSeed?: number;

  // Relations
  @OneToMany(() => QuestionResponse, (response) => response.attempt, {
    cascade: true,
  })
  responses: QuestionResponse[];

  // Metadata
  @Column({ name: 'ip_address', type: 'varchar', length: 45, nullable: true })
  ipAddress?: string;

  @Column({ name: 'user_agent', type: 'text', nullable: true })
  userAgent?: string;
}
