import { Injectable, Logger } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { User } from '../users/entities/user.entity';
import { Enrollment } from '../enrollments/entities/enrollment.entity';
import { Progress } from '../progress/entities/progress.entity';
import { Course } from '../courses/entities/course.entity';

interface StudentRiskAssessment {
  userId: number;
  riskLevel: 'low' | 'medium' | 'high' | 'critical';
  riskScore: number;
  factors: string[];
  recommendations: string[];
}

interface LearningPathRecommendation {
  currentCourse: number;
  suggestedNext: number[];
  reasoning: string[];
  estimatedCompletionTime: number; // in days
}

interface PredictiveMetrics {
  completionProbability: number;
  estimatedCompletionDate: Date;
  engagementScore: number;
  performanceTrend: 'improving' | 'stable' | 'declining';
}

@Injectable()
export class AdvancedAnalyticsService {
  private readonly logger = new Logger(AdvancedAnalyticsService.name);

  constructor(
    @InjectRepository(User)
    private userRepo: Repository<User>,
    @InjectRepository(Enrollment)
    private enrollmentRepo: Repository<Enrollment>,
    @InjectRepository(Progress)
    private progressRepo: Repository<Progress>,
    @InjectRepository(Course)
    private courseRepo: Repository<Course>,
  ) {}

  /**
   * Assess student at-risk level using multiple factors
   */
  async assessStudentRisk(userId: number, courseId?: number): Promise<StudentRiskAssessment[]> {
    const enrollments = courseId
      ? [await this.enrollmentRepo.findOne({ where: { userId, courseId }, relations: ['course'] })]
      : await this.enrollmentRepo.find({ where: { userId }, relations: ['course'] });

    const assessments: StudentRiskAssessment[] = [];

    for (const enrollment of enrollments.filter(e => e)) {
      const riskFactors: string[] = [];
      let riskScore = 0;

      // Factor 1: Inactivity (30 points)
      const daysSinceLastActivity = await this.getDaysSinceLastActivity(userId, enrollment.courseId);
      if (daysSinceLastActivity > 14) {
        riskScore += 30;
        riskFactors.push(`Неактивен ${daysSinceLastActivity} дней`);
      } else if (daysSinceLastActivity > 7) {
        riskScore += 15;
        riskFactors.push(`Неактивен ${daysSinceLastActivity} дней`);
      }

      // Factor 2: Low progress (25 points)
      const progressRate = await this.getProgressRate(userId, enrollment.courseId);
      if (progressRate < 0.3) {
        riskScore += 25;
        riskFactors.push(`Низкий прогресс: ${(progressRate * 100).toFixed(0)}%`);
      } else if (progressRate < 0.5) {
        riskScore += 12;
        riskFactors.push(`Средний прогресс: ${(progressRate * 100).toFixed(0)}%`);
      }

      // Factor 3: Slow pace (20 points)
      const expectedDaysElapsed = await this.getExpectedDaysElapsed(enrollment);
      const actualDaysElapsed = Math.floor((Date.now() - enrollment.createdAt.getTime()) / (1000 * 60 * 60 * 24));
      if (actualDaysElapsed > expectedDaysElapsed * 1.5) {
        riskScore += 20;
        riskFactors.push('Отстает от графика');
      }

      // Factor 4: Failed quizzes (15 points)
      const failedQuizzes = await this.getFailedQuizzesCount(userId, enrollment.courseId);
      if (failedQuizzes > 2) {
        riskScore += 15;
        riskFactors.push(`${failedQuizzes} проваленных тестов`);
      }

      // Factor 5: Low engagement (10 points)
      const engagementScore = await this.getEngagementScore(userId, enrollment.courseId);
      if (engagementScore < 0.3) {
        riskScore += 10;
        riskFactors.push('Низкая вовлеченность');
      }

      // Determine risk level
      let riskLevel: 'low' | 'medium' | 'high' | 'critical';
      if (riskScore >= 70) riskLevel = 'critical';
      else if (riskScore >= 50) riskLevel = 'high';
      else if (riskScore >= 30) riskLevel = 'medium';
      else riskLevel = 'low';

      // Generate recommendations
      const recommendations = this.generateRiskRecommendations(riskFactors, riskLevel);

      assessments.push({
        userId,
        riskLevel,
        riskScore,
        factors: riskFactors,
        recommendations,
      });
    }

    return assessments;
  }

  /**
   * Predict course completion probability
   */
  async predictCompletion(userId: number, courseId: number): Promise<PredictiveMetrics> {
    const enrollment = await this.enrollmentRepo.findOne({
      where: { userId, courseId },
    });

    if (!enrollment) {
      throw new Error('Enrollment not found');
    }

    // Calculate completion probability based on multiple factors
    const progressRate = await this.getProgressRate(userId, courseId);
    const engagementScore = await this.getEngagementScore(userId, courseId);
    const daysSinceLastActivity = await this.getDaysSinceLastActivity(userId, courseId);
    const performanceTrend = await this.getPerformanceTrend(userId, courseId);

    // Weighted probability calculation
    let probability = 0;
    probability += progressRate * 0.4; // 40% weight on current progress
    probability += engagementScore * 0.3; // 30% weight on engagement
    probability += (daysSinceLastActivity < 7 ? 0.2 : 0) * 0.2; // 20% weight on recent activity
    probability += (performanceTrend === 'improving' ? 0.8 : performanceTrend === 'stable' ? 0.5 : 0.2) * 0.1;

    // Estimate completion date
    const daysElapsed = Math.floor((Date.now() - enrollment.createdAt.getTime()) / (1000 * 60 * 60 * 24));
    const estimatedTotalDays = progressRate > 0 ? Math.ceil(daysElapsed / progressRate) : daysElapsed * 3;
    const daysRemaining = estimatedTotalDays - daysElapsed;
    const estimatedCompletionDate = new Date(Date.now() + daysRemaining * 24 * 60 * 60 * 1000);

    return {
      completionProbability: Math.min(probability, 1),
      estimatedCompletionDate,
      engagementScore,
      performanceTrend,
    };
  }

  /**
   * Optimize learning path for student
   */
  async optimizeLearningPath(userId: number, currentCourseId: number): Promise<LearningPathRecommendation> {
    const currentCourse = await this.courseRepo.findOne({
      where: { id: currentCourseId },
      relations: ['category'],
    });

    if (!currentCourse) {
      throw new Error('Course not found');
    }

    // Get user's completed courses
    const completedEnrollments = await this.enrollmentRepo.find({
      where: { userId, status: 'completed' },
      relations: ['course'],
    });

    const completedCourseIds = completedEnrollments.map(e => e.courseId);

    // Find similar courses in same category
    const suggestedCourses = await this.courseRepo
      .createQueryBuilder('course')
      .where('course.categoryId = :categoryId', { categoryId: currentCourse.categoryId })
      .andWhere('course.id != :currentId', { currentId: currentCourseId })
      .andWhere('course.id NOT IN (:...completedIds)', {
        completedIds: completedCourseIds.length > 0 ? completedCourseIds : [0],
      })
      .andWhere('course.status = :status', { status: 'published' })
      .orderBy('course.rating', 'DESC')
      .limit(5)
      .getMany();

    // Calculate estimated completion time
    const avgLessonTime = 30; // minutes per lesson
    const estimatedCompletionTime = Math.ceil(
      (suggestedCourses[0]?.duration || 0) / (avgLessonTime * 60)
    );

    const reasoning = [
      `Основано на вашем прогрессе в категории ${currentCourse.category?.name}`,
      'Курсы с высоким рейтингом',
      'Последовательное развитие навыков',
    ];

    return {
      currentCourse: currentCourseId,
      suggestedNext: suggestedCourses.map(c => c.id),
      reasoning,
      estimatedCompletionTime,
    };
  }

  /**
   * Get cohort analysis for course
   */
  async getCohortAnalysis(courseId: number): Promise<any> {
    const enrollments = await this.enrollmentRepo.find({
      where: { courseId },
      relations: ['user'],
    });

    const totalStudents = enrollments.length;
    const completedStudents = enrollments.filter(e => e.status === 'completed').length;
    const activeStudents = enrollments.filter(e => e.status === 'active').length;
    const droppedStudents = enrollments.filter(e => e.status === 'dropped').length;

    // Calculate average progress
    const progressData = await Promise.all(
      enrollments.map(e => this.getProgressRate(e.userId, courseId))
    );
    const avgProgress = progressData.reduce((sum, p) => sum + p, 0) / totalStudents;

    // Calculate completion rate by week
    const completionRateByWeek: { week: number; completed: number }[] = [];
    const maxWeeks = 12;

    for (let week = 1; week <= maxWeeks; week++) {
      const weekEnd = new Date(Date.now() - (maxWeeks - week) * 7 * 24 * 60 * 60 * 1000);
      const completedByWeek = enrollments.filter(
        e => e.completedAt && e.completedAt <= weekEnd
      ).length;
      completionRateByWeek.push({ week, completed: completedByWeek });
    }

    return {
      courseId,
      totalStudents,
      completedStudents,
      activeStudents,
      droppedStudents,
      completionRate: (completedStudents / totalStudents) * 100,
      averageProgress: avgProgress * 100,
      completionRateByWeek,
    };
  }

  /**
   * Get performance metrics summary
   */
  async getPerformanceMetrics(userId: number): Promise<any> {
    const enrollments = await this.enrollmentRepo.find({
      where: { userId },
      relations: ['course'],
    });

    const totalCourses = enrollments.length;
    const completedCourses = enrollments.filter(e => e.status === 'completed').length;
    const inProgressCourses = enrollments.filter(e => e.status === 'active').length;

    // Calculate average completion time
    const completedWithTime = enrollments.filter(e => e.completedAt && e.createdAt);
    const avgCompletionDays = completedWithTime.length > 0
      ? completedWithTime.reduce((sum, e) => {
          const days = Math.floor((e.completedAt!.getTime() - e.createdAt.getTime()) / (1000 * 60 * 60 * 24));
          return sum + days;
        }, 0) / completedWithTime.length
      : 0;

    // Calculate average engagement
    const engagementScores = await Promise.all(
      enrollments.map(e => this.getEngagementScore(userId, e.courseId))
    );
    const avgEngagement = engagementScores.reduce((sum, s) => sum + s, 0) / totalCourses;

    return {
      totalCourses,
      completedCourses,
      inProgressCourses,
      completionRate: (completedCourses / totalCourses) * 100,
      averageCompletionDays: Math.round(avgCompletionDays),
      averageEngagement: avgEngagement,
      performanceLevel: this.getPerformanceLevel(avgEngagement, completionRate),
    };
  }

  // Helper methods

  private async getDaysSinceLastActivity(userId: number, courseId: number): Promise<number> {
    const lastProgress = await this.progressRepo.findOne({
      where: { userId, lesson: { courseId } as any },
      order: { updatedAt: 'DESC' },
    });

    if (!lastProgress) return 999;

    return Math.floor((Date.now() - lastProgress.updatedAt.getTime()) / (1000 * 60 * 60 * 24));
  }

  private async getProgressRate(userId: number, courseId: number): Promise<number> {
    const course = await this.courseRepo.findOne({
      where: { id: courseId },
      relations: ['lessons'],
    });

    if (!course || course.lessons.length === 0) return 0;

    const completedLessons = await this.progressRepo.count({
      where: {
        userId,
        lesson: { courseId } as any,
        completed: true,
      },
    });

    return completedLessons / course.lessons.length;
  }

  private async getExpectedDaysElapsed(enrollment: Enrollment): Promise<number> {
    // Estimate based on course duration (assume 2 hours study per day)
    const course = await this.courseRepo.findOne({ where: { id: enrollment.courseId } });
    if (!course) return 30;

    return Math.ceil((course.duration || 3600) / (2 * 3600)); // duration in seconds
  }

  private async getFailedQuizzesCount(userId: number, courseId: number): Promise<number> {
    // This would query quiz_attempts table
    // Simplified for now
    return 0;
  }

  private async getEngagementScore(userId: number, courseId: number): Promise<number> {
    const enrollment = await this.enrollmentRepo.findOne({
      where: { userId, courseId },
    });

    if (!enrollment) return 0;

    const daysElapsed = Math.floor((Date.now() - enrollment.createdAt.getTime()) / (1000 * 60 * 60 * 24));
    const daysSinceLastActivity = await this.getDaysSinceLastActivity(userId, courseId);
    const progressRate = await this.getProgressRate(userId, courseId);

    // Calculate engagement score
    let score = 0;
    score += progressRate * 0.4;
    score += (daysSinceLastActivity < 7 ? 1 : daysSinceLastActivity < 14 ? 0.5 : 0) * 0.3;
    score += (daysElapsed > 0 ? Math.min(progressRate / (daysElapsed / 30), 1) : 0) * 0.3;

    return Math.min(score, 1);
  }

  private async getPerformanceTrend(userId: number, courseId: number): Promise<'improving' | 'stable' | 'declining'> {
    const recentProgress = await this.progressRepo.find({
      where: {
        userId,
        lesson: { courseId } as any,
      },
      order: { updatedAt: 'DESC' },
      take: 10,
    });

    if (recentProgress.length < 3) return 'stable';

    // Compare first half vs second half
    const mid = Math.floor(recentProgress.length / 2);
    const recentCount = recentProgress.slice(0, mid).filter(p => p.completed).length;
    const olderCount = recentProgress.slice(mid).filter(p => p.completed).length;

    if (recentCount > olderCount * 1.2) return 'improving';
    if (recentCount < olderCount * 0.8) return 'declining';
    return 'stable';
  }

  private generateRiskRecommendations(factors: string[], riskLevel: string): string[] {
    const recommendations: string[] = [];

    if (factors.some(f => f.includes('Неактивен'))) {
      recommendations.push('Установите регулярное расписание занятий');
      recommendations.push('Включите уведомления о новых материалах');
    }

    if (factors.some(f => f.includes('прогресс'))) {
      recommendations.push('Разбейте обучение на маленькие шаги');
      recommendations.push('Сконцентрируйтесь на одном уроке за раз');
    }

    if (factors.some(f => f.includes('график'))) {
      recommendations.push('Пересмотрите темп обучения');
      recommendations.push('Обратитесь за помощью к инструктору');
    }

    if (factors.some(f => f.includes('тестов'))) {
      recommendations.push('Повторите пройденный материал');
      recommendations.push('Используйте дополнительные ресурсы');
    }

    if (riskLevel === 'critical') {
      recommendations.push('⚠️ Рекомендуем связаться с поддержкой');
    }

    return recommendations;
  }

  private getPerformanceLevel(engagement: number, completionRate: number): string {
    const score = (engagement + completionRate / 100) / 2;

    if (score >= 0.8) return 'Отлично';
    if (score >= 0.6) return 'Хорошо';
    if (score >= 0.4) return 'Удовлетворительно';
    return 'Требует улучшения';
  }
}
