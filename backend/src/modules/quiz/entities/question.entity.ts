import { Entity, Column, ManyToOne, JoinColumn, OneToMany } from 'typeorm';
import { BaseEntity } from '../../../common/entities/base.entity';
import { Quiz } from './quiz.entity';
import { Answer } from './answer.entity';

export enum QuestionType {
  MULTIPLE_CHOICE = 'multiple_choice', // One correct answer
  MULTIPLE_SELECT = 'multiple_select', // Multiple correct answers
  TRUE_FALSE = 'true_false',
  SHORT_ANSWER = 'short_answer',
  ESSAY = 'essay',
  FILL_BLANK = 'fill_blank',
  MATCHING = 'matching',
}

/**
 * Question entity with support for various question types
 */
@Entity('questions')
export class Question extends BaseEntity {
  @Column({ name: 'quiz_id' })
  quizId: number;

  @ManyToOne(() => Quiz, (quiz) => quiz.questions, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'quiz_id' })
  quiz: Quiz;

  @Column({ type: 'enum', enum: QuestionType })
  type: QuestionType;

  @Column({ type: 'text' })
  question: string;

  @Column({ type: 'text', nullable: true })
  explanation?: string; // Explanation shown after answer

  @Column({ type: 'int', default: 1 })
  points: number;

  @Column({ name: 'order_index', type: 'int' })
  orderIndex: number;

  // For media support
  @Column({ name: 'image_url', type: 'varchar', length: 1000, nullable: true })
  imageUrl?: string;

  @Column({ name: 'video_url', type: 'varchar', length: 1000, nullable: true })
  videoUrl?: string;

  // Settings
  @Column({ name: 'is_required', type: 'boolean', default: true })
  isRequired: boolean;

  @Column({ name: 'case_sensitive', type: 'boolean', default: false })
  caseSensitive: boolean; // For short answer questions

  // For matching questions
  @Column({ type: 'json', nullable: true })
  matchingPairs?: Array<{ left: string; right: string }>;

  // Relations
  @OneToMany(() => Answer, (answer) => answer.question, { cascade: true })
  answers: Answer[];
}
