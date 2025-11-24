import { Entity, Column, ManyToOne, JoinColumn, Index } from 'typeorm';
import { BaseEntity } from '../../../common/entities/base.entity';
import { QuizAttempt } from './quiz-attempt.entity';
import { Question } from './question.entity';

/**
 * Stores user responses to quiz questions
 */
@Entity('question_responses')
@Index(['attemptId', 'questionId'])
export class QuestionResponse extends BaseEntity {
  @Column({ name: 'attempt_id' })
  attemptId: number;

  @ManyToOne(() => QuizAttempt, (attempt) => attempt.responses, {
    onDelete: 'CASCADE',
  })
  @JoinColumn({ name: 'attempt_id' })
  attempt: QuizAttempt;

  @Column({ name: 'question_id' })
  questionId: number;

  @ManyToOne(() => Question)
  @JoinColumn({ name: 'question_id' })
  question: Question;

  // Response data
  @Column({ name: 'selected_answers', type: 'json', nullable: true })
  selectedAnswers?: number[]; // Answer IDs for multiple choice/select

  @Column({ name: 'text_answer', type: 'text', nullable: true })
  textAnswer?: string; // For short answer, essay, fill blank

  @Column({ name: 'matching_pairs', type: 'json', nullable: true })
  matchingPairs?: Array<{ leftId: string; rightId: string }>; // For matching

  // Scoring
  @Column({ name: 'is_correct', type: 'boolean', nullable: true })
  isCorrect?: boolean; // null for manual grading needed

  @Column({ name: 'points_earned', type: 'float', default: 0 })
  pointsEarned: number;

  @Column({ name: 'points_possible', type: 'int' })
  pointsPossible: number;

  // Manual grading
  @Column({ name: 'requires_manual_grading', type: 'boolean', default: false })
  requiresManualGrading: boolean;

  @Column({ name: 'graded_by', type: 'int', nullable: true })
  gradedBy?: number; // User ID of grader

  @Column({ name: 'graded_at', type: 'timestamp', nullable: true })
  gradedAt?: Date;

  @Column({ name: 'grader_feedback', type: 'text', nullable: true })
  graderFeedback?: string;

  // Timing
  @Column({ name: 'time_spent', type: 'int', nullable: true })
  timeSpent?: number; // in seconds
}
