import { Entity, Column, ManyToOne, JoinColumn, Index } from 'typeorm';
import { BaseEntity } from '../../../common/entities/base.entity';
import { Enrollment } from './enrollment.entity';
import { Lesson } from '../../courses/entities/lesson.entity';

export enum ProgressStatus {
  NOT_STARTED = 'not_started',
  IN_PROGRESS = 'in_progress',
  COMPLETED = 'completed',
}

@Entity('lesson_progress')
@Index(['enrollmentId', 'lessonId'], { unique: true })
export class LessonProgress extends BaseEntity {
  @Column({ name: 'enrollment_id' })
  enrollmentId: number;

  @ManyToOne(() => Enrollment, (enrollment) => enrollment.lessonProgress)
  @JoinColumn({ name: 'enrollment_id' })
  enrollment: Enrollment;

  @Column({ name: 'lesson_id' })
  lessonId: number;

  @ManyToOne(() => Lesson)
  @JoinColumn({ name: 'lesson_id' })
  lesson: Lesson;

  @Column({
    type: 'enum',
    enum: ProgressStatus,
    default: ProgressStatus.NOT_STARTED,
  })
  status: ProgressStatus;

  @Column({ name: 'last_position', type: 'int', default: 0 })
  lastPosition: number; // in seconds for video

  @Column({ name: 'completed_at', type: 'timestamp', nullable: true })
  completedAt?: Date;
}
