import { Entity, Column, ManyToOne, JoinColumn, OneToMany } from 'typeorm';
import { BaseEntity } from '../../../common/entities/base.entity';
import { Lesson } from '../../courses/entities/lesson.entity';
import { Question } from './question.entity';
import { QuizAttempt } from './quiz-attempt.entity';

export enum QuizDifficulty {
  EASY = 'easy',
  MEDIUM = 'medium',
  HARD = 'hard',
}

/**
 * Quiz entity for course assessments
 * Supports multiple question types, time limits, and randomization
 */
@Entity('quizzes')
export class Quiz extends BaseEntity {
  @Column({ type: 'varchar', length: 500 })
  title: string;

  @Column({ type: 'text', nullable: true })
  description?: string;

  @Column({ name: 'lesson_id', nullable: true })
  lessonId?: number;

  @ManyToOne(() => Lesson, { nullable: true })
  @JoinColumn({ name: 'lesson_id' })
  lesson?: Lesson;

  // Quiz settings
  @Column({ name: 'time_limit', type: 'int', nullable: true })
  timeLimit?: number; // in seconds

  @Column({ name: 'passing_score', type: 'int', default: 70 })
  passingScore: number; // percentage

  @Column({ name: 'max_attempts', type: 'int', default: 3 })
  maxAttempts: number;

  @Column({ name: 'randomize_questions', type: 'boolean', default: false })
  randomizeQuestions: boolean;

  @Column({ name: 'randomize_answers', type: 'boolean', default: false })
  randomizeAnswers: boolean;

  @Column({ name: 'show_correct_answers', type: 'boolean', default: true })
  showCorrectAnswers: boolean;

  @Column({ name: 'show_results_immediately', type: 'boolean', default: true })
  showResultsImmediately: boolean;

  @Column({
    type: 'enum',
    enum: QuizDifficulty,
    default: QuizDifficulty.MEDIUM,
  })
  difficulty: QuizDifficulty;

  @Column({ name: 'is_published', type: 'boolean', default: false })
  isPublished: boolean;

  // Relations
  @OneToMany(() => Question, (question) => question.quiz, { cascade: true })
  questions: Question[];

  @OneToMany(() => QuizAttempt, (attempt) => attempt.quiz)
  attempts: QuizAttempt[];

  // Metadata
  @Column({ name: 'total_questions', type: 'int', default: 0 })
  totalQuestions: number;

  @Column({ name: 'total_points', type: 'int', default: 0 })
  totalPoints: number;

  @Column({ name: 'attempt_count', type: 'int', default: 0 })
  attemptCount: number;

  @Column({ name: 'average_score', type: 'float', default: 0 })
  averageScore: number;
}
