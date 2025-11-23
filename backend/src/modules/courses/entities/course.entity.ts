import {
  Entity,
  Column,
  ManyToOne,
  OneToMany,
  JoinColumn,
  Index,
} from 'typeorm';
import { BaseEntity } from '../../../common/entities/base.entity';
import {
  CourseStatus,
  CourseLevel,
} from '../../../common/enums/course-status.enum';
import { User } from '../../users/entities/user.entity';
import { CourseModule } from './course-module.entity';
import { Enrollment } from '../../enrollments/entities/enrollment.entity';
import { Category } from './category.entity';

@Entity('courses')
@Index(['status'])
@Index(['instructorId'])
export class Course extends BaseEntity {
  @Column({ type: 'varchar', length: 255 })
  title: string;

  @Column({ type: 'text', nullable: true })
  description?: string;

  @Column({ type: 'varchar', length: 500, nullable: true })
  thumbnail?: string;

  @Column({
    type: 'enum',
    enum: CourseStatus,
    default: CourseStatus.DRAFT,
  })
  status: CourseStatus;

  @Column({
    type: 'enum',
    enum: CourseLevel,
    default: CourseLevel.BEGINNER,
  })
  level: CourseLevel;

  @Column({ type: 'decimal', precision: 10, scale: 2, default: 0 })
  price: number;

  @Column({ name: 'is_free', type: 'boolean', default: false })
  isFree: boolean;

  @Column({ type: 'int', default: 0 })
  duration: number; // in minutes

  @Column({ name: 'student_count', type: 'int', default: 0 })
  studentCount: number;

  @Column({ type: 'decimal', precision: 3, scale: 2, default: 0 })
  rating: number;

  @Column({ name: 'rating_count', type: 'int', default: 0 })
  ratingCount: number;

  @Column({ name: 'published_at', type: 'timestamp', nullable: true })
  publishedAt?: Date;

  // Instructor
  @Column({ name: 'instructor_id' })
  instructorId: number;

  @ManyToOne(() => User, { eager: true })
  @JoinColumn({ name: 'instructor_id' })
  instructor: User;

  // Category
  @Column({ name: 'category_id', nullable: true })
  categoryId?: number;

  @ManyToOne(() => Category, (category) => category.courses, { eager: true })
  @JoinColumn({ name: 'category_id' })
  category?: Category;

  // Relations
  @OneToMany(() => CourseModule, (module) => module.course, {
    cascade: true,
  })
  modules: CourseModule[];

  @OneToMany(() => Enrollment, (enrollment) => enrollment.course)
  enrollments: Enrollment[];
}
