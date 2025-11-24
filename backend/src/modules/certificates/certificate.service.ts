import { Injectable, NotFoundException, BadRequestException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Certificate } from './entities/certificate.entity';
import { Enrollment } from '../enrollments/entities/enrollment.entity';
import { User } from '../users/entities/user.entity';
import { Course } from '../courses/entities/course.entity';
import * as crypto from 'crypto';

interface CertificateData {
  studentName: string;
  courseTitle: string;
  instructorName?: string;
  completionDate: Date;
  finalScore?: number;
  totalHours?: number;
  grade?: string;
}

/**
 * Certificate generation and management service
 */
@Injectable()
export class CertificateService {
  constructor(
    @InjectRepository(Certificate)
    private certificateRepository: Repository<Certificate>,
    @InjectRepository(Enrollment)
    private enrollmentRepository: Repository<Enrollment>,
    @InjectRepository(User)
    private userRepository: Repository<User>,
    @InjectRepository(Course)
    private courseRepository: Repository<Course>,
  ) {}

  /**
   * Generate certificate for completed course
   */
  async generateCertificate(
    userId: number,
    courseId: number,
  ): Promise<Certificate> {
    // Check if certificate already exists
    const existing = await this.certificateRepository.findOne({
      where: { userId, courseId },
    });

    if (existing) {
      return existing;
    }

    // Verify enrollment is completed
    const enrollment = await this.enrollmentRepository.findOne({
      where: { userId, courseId },
      relations: ['user', 'course', 'course.instructor'],
    });

    if (!enrollment) {
      throw new NotFoundException('Enrollment not found');
    }

    if (enrollment.status !== 'completed') {
      throw new BadRequestException('Course must be completed to generate certificate');
    }

    // Get user and course details
    const user = await this.userRepository.findOne({ where: { id: userId } });
    const course = await this.courseRepository.findOne({
      where: { id: courseId },
      relations: ['instructor'],
    });

    if (!user || !course) {
      throw new NotFoundException('User or course not found');
    }

    // Generate certificate number (format: CERT-YYYY-XXXXXX)
    const certificateNumber = this.generateCertificateNumber();
    const verificationCode = this.generateVerificationCode();

    // Calculate metrics
    const totalHours = enrollment.totalTimeSpent
      ? enrollment.totalTimeSpent / 3600
      : undefined;

    const finalScore = enrollment.averageScore;

    const grade = this.calculateGrade(finalScore);

    // Create certificate data
    const certificateData: CertificateData = {
      studentName: `${user.firstName} ${user.lastName}`,
      courseTitle: course.title,
      instructorName: course.instructor
        ? `${course.instructor.firstName} ${course.instructor.lastName}`
        : undefined,
      completionDate: enrollment.completedAt || new Date(),
      finalScore,
      totalHours,
      grade,
    };

    // Generate PDF (placeholder URL for now)
    // In production, this would call a PDF generation service
    const pdfUrl = await this.generatePDF(certificateData, certificateNumber);

    // Create certificate record
    const certificate = this.certificateRepository.create({
      certificateNumber,
      userId,
      courseId,
      issuedDate: new Date(),
      completionDate: certificateData.completionDate,
      studentName: certificateData.studentName,
      courseTitle: certificateData.courseTitle,
      instructorName: certificateData.instructorName,
      finalScore: certificateData.finalScore,
      totalHours: certificateData.totalHours,
      grade: certificateData.grade,
      pdfUrl,
      verificationCode,
      isValid: true,
    });

    return this.certificateRepository.save(certificate);
  }

  /**
   * Get user's certificates
   */
  async getUserCertificates(userId: number): Promise<Certificate[]> {
    return this.certificateRepository.find({
      where: { userId, isValid: true },
      relations: ['course'],
      order: { issuedDate: 'DESC' },
    });
  }

  /**
   * Get certificate by ID
   */
  async getCertificateById(id: number, userId?: number): Promise<Certificate> {
    const certificate = await this.certificateRepository.findOne({
      where: { id },
      relations: ['user', 'course'],
    });

    if (!certificate) {
      throw new NotFoundException('Certificate not found');
    }

    // If userId provided, verify ownership
    if (userId && certificate.userId !== userId) {
      throw new BadRequestException('Access denied');
    }

    // Increment view count
    certificate.viewCount++;
    certificate.lastViewedAt = new Date();
    await this.certificateRepository.save(certificate);

    return certificate;
  }

  /**
   * Verify certificate by verification code
   */
  async verifyCertificate(verificationCode: string): Promise<{
    isValid: boolean;
    certificate?: Certificate;
    message: string;
  }> {
    const certificate = await this.certificateRepository.findOne({
      where: { verificationCode },
      relations: ['user', 'course'],
    });

    if (!certificate) {
      return {
        isValid: false,
        message: 'Certificate not found',
      };
    }

    if (!certificate.isValid) {
      return {
        isValid: false,
        certificate,
        message: `Certificate revoked: ${certificate.revokedReason || 'Unknown reason'}`,
      };
    }

    return {
      isValid: true,
      certificate,
      message: 'Certificate is valid',
    };
  }

  /**
   * Revoke certificate
   */
  async revokeCertificate(
    id: number,
    reason: string,
  ): Promise<Certificate> {
    const certificate = await this.certificateRepository.findOne({
      where: { id },
    });

    if (!certificate) {
      throw new NotFoundException('Certificate not found');
    }

    certificate.isValid = false;
    certificate.revokedAt = new Date();
    certificate.revokedReason = reason;

    return this.certificateRepository.save(certificate);
  }

  /**
   * Track certificate download
   */
  async trackDownload(id: number): Promise<void> {
    await this.certificateRepository.increment({ id }, 'downloadCount', 1);
  }

  /**
   * Get certificate statistics
   */
  async getCertificateStats(): Promise<{
    totalIssued: number;
    totalValid: number;
    totalRevoked: number;
    issuedThisMonth: number;
    issuedThisYear: number;
  }> {
    const totalIssued = await this.certificateRepository.count();
    const totalValid = await this.certificateRepository.count({
      where: { isValid: true },
    });
    const totalRevoked = await this.certificateRepository.count({
      where: { isValid: false },
    });

    const now = new Date();
    const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);
    const startOfYear = new Date(now.getFullYear(), 0, 1);

    const issuedThisMonth = await this.certificateRepository
      .createQueryBuilder('certificate')
      .where('certificate.issuedDate >= :startOfMonth', { startOfMonth })
      .getCount();

    const issuedThisYear = await this.certificateRepository
      .createQueryBuilder('certificate')
      .where('certificate.issuedDate >= :startOfYear', { startOfYear })
      .getCount();

    return {
      totalIssued,
      totalValid,
      totalRevoked,
      issuedThisMonth,
      issuedThisYear,
    };
  }

  /**
   * Generate unique certificate number
   */
  private generateCertificateNumber(): string {
    const year = new Date().getFullYear();
    const random = Math.floor(Math.random() * 1000000)
      .toString()
      .padStart(6, '0');
    return `CERT-${year}-${random}`;
  }

  /**
   * Generate verification code
   */
  private generateVerificationCode(): string {
    return crypto.randomBytes(32).toString('hex');
  }

  /**
   * Calculate letter grade from score
   */
  private calculateGrade(score?: number): string | undefined {
    if (score === undefined) return undefined;

    if (score >= 90) return 'A';
    if (score >= 80) return 'B';
    if (score >= 70) return 'C';
    if (score >= 60) return 'D';
    return 'F';
  }

  /**
   * Generate PDF certificate (placeholder)
   * In production, this would use a PDF generation library like PDFKit
   */
  private async generatePDF(
    data: CertificateData,
    certificateNumber: string,
  ): Promise<string> {
    // TODO: Implement actual PDF generation
    // This is a placeholder that returns a mock URL
    // In production, you would:
    // 1. Use PDFKit or similar to generate PDF
    // 2. Upload to MinIO/S3
    // 3. Return the storage URL

    const mockUrl = `/certificates/${certificateNumber}.pdf`;
    return mockUrl;
  }

  /**
   * Regenerate certificate PDF (for template updates)
   */
  async regenerateCertificate(id: number): Promise<Certificate> {
    const certificate = await this.certificateRepository.findOne({
      where: { id },
    });

    if (!certificate) {
      throw new NotFoundException('Certificate not found');
    }

    const certificateData: CertificateData = {
      studentName: certificate.studentName,
      courseTitle: certificate.courseTitle,
      instructorName: certificate.instructorName,
      completionDate: certificate.completionDate,
      finalScore: certificate.finalScore,
      totalHours: certificate.totalHours,
      grade: certificate.grade,
    };

    const pdfUrl = await this.generatePDF(
      certificateData,
      certificate.certificateNumber,
    );

    certificate.pdfUrl = pdfUrl;
    return this.certificateRepository.save(certificate);
  }

  /**
   * Get certificates for a course
   */
  async getCourseCertificates(
    courseId: number,
  ): Promise<Certificate[]> {
    return this.certificateRepository.find({
      where: { courseId, isValid: true },
      relations: ['user'],
      order: { issuedDate: 'DESC' },
    });
  }

  /**
   * Search certificates
   */
  async searchCertificates(query: string): Promise<Certificate[]> {
    return this.certificateRepository
      .createQueryBuilder('certificate')
      .leftJoinAndSelect('certificate.user', 'user')
      .leftJoinAndSelect('certificate.course', 'course')
      .where('certificate.certificateNumber LIKE :query', {
        query: `%${query}%`,
      })
      .orWhere('certificate.studentName LIKE :query', { query: `%${query}%` })
      .orWhere('certificate.courseTitle LIKE :query', { query: `%${query}%` })
      .orWhere('certificate.verificationCode = :code', { code: query })
      .getMany();
  }
}
