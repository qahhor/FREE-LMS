import { Entity, Column, ManyToOne, JoinColumn, Index } from 'typeorm';
import { BaseEntity } from '../../../common/entities/base.entity';
import { CourseModule } from './course-module.entity';

export enum LessonType {
  VIDEO = 'video',
  TEXT = 'text',
  QUIZ = 'quiz',
  ASSIGNMENT = 'assignment',
  DOCUMENT = 'document',
}

@Entity('lessons')
@Index(['moduleId', 'orderIndex'])
export class Lesson extends BaseEntity {
  @Column({ type: 'varchar', length: 255 })
  title: string;

  @Column({ type: 'text', nullable: true })
  description?: string;

  @Column({
    type: 'enum',
    enum: LessonType,
    default: LessonType.VIDEO,
  })
  type: LessonType;

  @Column({ type: 'text', nullable: true })
  content?: string;

  @Column({ name: 'video_url', type: 'varchar', length: 500, nullable: true })
  videoUrl?: string;

  @Column({ type: 'int', default: 0 })
  duration: number; // in seconds

  @Column({ name: 'order_index', type: 'int' })
  orderIndex: number;

  @Column({ name: 'is_preview', type: 'boolean', default: false })
  isPreview: boolean;

  @Column({ name: 'is_published', type: 'boolean', default: false })
  isPublished: boolean;

  // Module
  @Column({ name: 'module_id' })
  moduleId: number;

  @ManyToOne(() => CourseModule, (module) => module.lessons, {
    onDelete: 'CASCADE',
  })
  @JoinColumn({ name: 'module_id' })
  module: CourseModule;
}
