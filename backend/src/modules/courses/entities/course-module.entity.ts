import {
  Entity,
  Column,
  ManyToOne,
  OneToMany,
  JoinColumn,
  Index,
} from 'typeorm';
import { BaseEntity } from '../../../common/entities/base.entity';
import { Course } from './course.entity';
import { Lesson } from './lesson.entity';

@Entity('course_modules')
@Index(['courseId', 'orderIndex'])
export class CourseModule extends BaseEntity {
  @Column({ type: 'varchar', length: 255 })
  title: string;

  @Column({ type: 'text', nullable: true })
  description?: string;

  @Column({ name: 'order_index', type: 'int' })
  orderIndex: number;

  @Column({ name: 'is_published', type: 'boolean', default: false })
  isPublished: boolean;

  // Course
  @Column({ name: 'course_id' })
  courseId: number;

  @ManyToOne(() => Course, (course) => course.modules, {
    onDelete: 'CASCADE',
  })
  @JoinColumn({ name: 'course_id' })
  course: Course;

  // Lessons
  @OneToMany(() => Lesson, (lesson) => lesson.module, {
    cascade: true,
  })
  lessons: Lesson[];
}
