import { Entity, Column, ManyToOne, JoinColumn, Index } from 'typeorm';
import { BaseEntity } from '../../../common/entities/base.entity';
import { User } from '../../users/entities/user.entity';
import { Course } from '../../courses/entities/course.entity';

/**
 * Certificate entity for course completion certificates
 */
@Entity('certificates')
@Index(['userId', 'courseId'], { unique: true })
export class Certificate extends BaseEntity {
  @Column({ name: 'certificate_number', unique: true })
  certificateNumber: string;

  @Column({ name: 'user_id' })
  userId: number;

  @ManyToOne(() => User)
  @JoinColumn({ name: 'user_id' })
  user: User;

  @Column({ name: 'course_id' })
  courseId: number;

  @ManyToOne(() => Course)
  @JoinColumn({ name: 'course_id' })
  course: Course;

  @Column({ name: 'issued_date', type: 'timestamp' })
  issuedDate: Date;

  @Column({ name: 'completion_date', type: 'timestamp' })
  completionDate: Date;

  // Certificate details
  @Column({ name: 'student_name', type: 'varchar', length: 255 })
  studentName: string;

  @Column({ name: 'course_title', type: 'varchar', length: 500 })
  courseTitle: string;

  @Column({ name: 'instructor_name', type: 'varchar', length: 255, nullable: true })
  instructorName?: string;

  // Performance metrics
  @Column({ name: 'final_score', type: 'float', nullable: true })
  finalScore?: number;

  @Column({ name: 'total_hours', type: 'float', nullable: true })
  totalHours?: number;

  @Column({ name: 'grade', type: 'varchar', length: 10, nullable: true })
  grade?: string; // A, B, C, etc. or Pass/Fail

  // File storage
  @Column({ name: 'pdf_url', type: 'varchar', length: 1000 })
  pdfUrl: string;

  @Column({ name: 'thumbnail_url', type: 'varchar', length: 1000, nullable: true })
  thumbnailUrl?: string;

  // Verification
  @Column({ name: 'verification_code', type: 'varchar', length: 64, unique: true })
  verificationCode: string;

  @Column({ name: 'is_valid', type: 'boolean', default: true })
  isValid: boolean;

  @Column({ name: 'revoked_at', type: 'timestamp', nullable: true })
  revokedAt?: Date;

  @Column({ name: 'revoked_reason', type: 'text', nullable: true })
  revokedReason?: string;

  // Blockchain integration (future)
  @Column({ name: 'blockchain_hash', type: 'varchar', length: 128, nullable: true })
  blockchainHash?: string;

  // Views
  @Column({ name: 'view_count', type: 'int', default: 0 })
  viewCount: number;

  @Column({ name: 'download_count', type: 'int', default: 0 })
  downloadCount: number;

  @Column({ name: 'last_viewed_at', type: 'timestamp', nullable: true })
  lastViewedAt?: Date;
}
