import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  ManyToOne,
  CreateDateColumn,
  Index,
} from 'typeorm';
import { User } from '../../users/entities/user.entity';
import { Course } from '../../courses/entities/course.entity';

export enum InteractionType {
  VIEW = 'view',
  ENROLL = 'enroll',
  COMPLETE = 'complete',
  RATE = 'rate',
  SEARCH = 'search',
  SHARE = 'share',
  BOOKMARK = 'bookmark',
}

@Entity('user_interactions')
@Index(['userId', 'courseId'])
@Index(['userId', 'type'])
@Index(['createdAt'])
export class UserInteraction {
  @PrimaryGeneratedColumn()
  id: number;

  @Column()
  @Index()
  userId: number;

  @ManyToOne(() => User, { onDelete: 'CASCADE' })
  user: User;

  @Column({ nullable: true })
  @Index()
  courseId: number;

  @ManyToOne(() => Course, { nullable: true, onDelete: 'CASCADE' })
  course: Course;

  @Column({
    type: 'enum',
    enum: InteractionType,
  })
  type: InteractionType;

  @Column({ type: 'float', nullable: true })
  value: number; // For ratings, duration, etc.

  @Column({ type: 'jsonb', nullable: true })
  metadata: Record<string, any>;

  @CreateDateColumn()
  createdAt: Date;
}
