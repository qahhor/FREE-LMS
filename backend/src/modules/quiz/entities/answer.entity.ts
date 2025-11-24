import { Entity, Column, ManyToOne, JoinColumn } from 'typeorm';
import { BaseEntity } from '../../../common/entities/base.entity';
import { Question } from './question.entity';

/**
 * Answer options for quiz questions
 */
@Entity('answers')
export class Answer extends BaseEntity {
  @Column({ name: 'question_id' })
  questionId: number;

  @ManyToOne(() => Question, (question) => question.answers, {
    onDelete: 'CASCADE',
  })
  @JoinColumn({ name: 'question_id' })
  question: Question;

  @Column({ type: 'text' })
  text: string;

  @Column({ name: 'is_correct', type: 'boolean', default: false })
  isCorrect: boolean;

  @Column({ name: 'order_index', type: 'int' })
  orderIndex: number;

  // For image-based answers
  @Column({ name: 'image_url', type: 'varchar', length: 1000, nullable: true })
  imageUrl?: string;

  // For partial credit
  @Column({ name: 'partial_credit', type: 'float', default: 0 })
  partialCredit: number; // 0-1 for partial points
}
