import { Injectable, Logger } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { UserInteraction, InteractionType } from './entities/user-interaction.entity';
import { Course } from '../courses/entities/course.entity';

interface RecommendationScore {
  courseId: number;
  score: number;
  reasons: string[];
}

@Injectable()
export class RecommendationsService {
  private readonly logger = new Logger(RecommendationsService.name);

  constructor(
    @InjectRepository(UserInteraction)
    private interactionRepo: Repository<UserInteraction>,
    @InjectRepository(Course)
    private courseRepo: Repository<Course>,
  ) {}

  /**
   * Record user interaction
   */
  async recordInteraction(
    userId: number,
    type: InteractionType,
    courseId?: number,
    value?: number,
    metadata?: Record<string, any>,
  ): Promise<UserInteraction> {
    const interaction = this.interactionRepo.create({
      userId,
      type,
      courseId,
      value,
      metadata,
    });

    return this.interactionRepo.save(interaction);
  }

  /**
   * Get personalized course recommendations for user
   * Uses hybrid approach: collaborative filtering + content-based filtering
   */
  async getRecommendations(
    userId: number,
    limit: number = 10,
  ): Promise<Course[]> {
    try {
      // Step 1: Get user's interaction history
      const userInteractions = await this.getUserInteractions(userId);

      if (userInteractions.length === 0) {
        // New user - return popular courses
        return this.getPopularCourses(limit);
      }

      // Step 2: Calculate scores for all courses
      const scores = await this.calculateRecommendationScores(userId, userInteractions);

      // Step 3: Get top N courses
      const topCourseIds = scores
        .sort((a, b) => b.score - a.score)
        .slice(0, limit)
        .map(s => s.courseId);

      if (topCourseIds.length === 0) {
        return this.getPopularCourses(limit);
      }

      // Step 4: Fetch course details
      const courses = await this.courseRepo
        .createQueryBuilder('course')
        .where('course.id IN (:...ids)', { ids: topCourseIds })
        .andWhere('course.status = :status', { status: 'published' })
        .getMany();

      // Sort by recommendation score
      return courses.sort((a, b) => {
        const scoreA = scores.find(s => s.courseId === a.id)?.score || 0;
        const scoreB = scores.find(s => s.courseId === b.id)?.score || 0;
        return scoreB - scoreA;
      });
    } catch (error) {
      this.logger.error('Error getting recommendations:', error);
      return this.getPopularCourses(limit);
    }
  }

  /**
   * Calculate recommendation scores using multiple algorithms
   */
  private async calculateRecommendationScores(
    userId: number,
    userInteractions: UserInteraction[],
  ): Promise<RecommendationScore[]> {
    const scores = new Map<number, RecommendationScore>();

    // Get all available courses
    const allCourses = await this.courseRepo.find({
      where: { status: 'published' },
      relations: ['category', 'instructor'],
    });

    // Get user's enrolled courses
    const enrolledCourseIds = new Set(
      userInteractions
        .filter(i => i.type === InteractionType.ENROLL)
        .map(i => i.courseId)
        .filter(Boolean)
    );

    for (const course of allCourses) {
      // Skip already enrolled courses
      if (enrolledCourseIds.has(course.id)) {
        continue;
      }

      let score = 0;
      const reasons: string[] = [];

      // 1. Collaborative Filtering: Users who liked this also liked...
      const collaborativeScore = await this.getCollaborativeFilteringScore(
        userId,
        course.id,
      );
      score += collaborativeScore * 0.4; // 40% weight
      if (collaborativeScore > 0) {
        reasons.push('Популярно среди похожих пользователей');
      }

      // 2. Content-Based: Similar to user's interests
      const contentScore = this.getContentBasedScore(course, userInteractions);
      score += contentScore * 0.3; // 30% weight
      if (contentScore > 0) {
        reasons.push('Соответствует вашим интересам');
      }

      // 3. Popularity Score
      const popularityScore = await this.getPopularityScore(course.id);
      score += popularityScore * 0.2; // 20% weight

      // 4. Recency bonus
      const recencyScore = this.getRecencyScore(course.createdAt);
      score += recencyScore * 0.1; // 10% weight
      if (recencyScore > 0.5) {
        reasons.push('Новый курс');
      }

      if (score > 0) {
        scores.set(course.id, { courseId: course.id, score, reasons });
      }
    }

    return Array.from(scores.values());
  }

  /**
   * Collaborative filtering: Find similar users and their preferences
   */
  private async getCollaborativeFilteringScore(
    userId: number,
    courseId: number,
  ): Promise<number> {
    // Find users who have similar interaction patterns
    const similarUsers = await this.interactionRepo
      .createQueryBuilder('interaction')
      .select('interaction.userId', 'userId')
      .addSelect('COUNT(*)', 'commonInteractions')
      .where('interaction.courseId IN (SELECT courseId FROM user_interactions WHERE userId = :userId)', { userId })
      .andWhere('interaction.userId != :userId', { userId })
      .groupBy('interaction.userId')
      .orderBy('commonInteractions', 'DESC')
      .limit(10)
      .getRawMany();

    if (similarUsers.length === 0) {
      return 0;
    }

    const similarUserIds = similarUsers.map(u => u.userId);

    // Check how many similar users interacted with this course
    const courseInteractions = await this.interactionRepo.count({
      where: {
        courseId,
        userId: similarUserIds as any,
        type: InteractionType.ENROLL,
      },
    });

    return Math.min(courseInteractions / similarUsers.length, 1);
  }

  /**
   * Content-based filtering: Match course attributes with user preferences
   */
  private getContentBasedScore(
    course: Course,
    userInteractions: UserInteraction[],
  ): number {
    let score = 0;

    // Count interactions by category
    const categoryInteractions = userInteractions.filter(
      i => i.course && i.course.categoryId === course.categoryId
    );

    if (categoryInteractions.length > 0) {
      score += Math.min(categoryInteractions.length / 10, 1) * 0.6;
    }

    // Count interactions by instructor
    const instructorInteractions = userInteractions.filter(
      i => i.course && i.course.instructorId === course.instructorId
    );

    if (instructorInteractions.length > 0) {
      score += Math.min(instructorInteractions.length / 5, 1) * 0.4;
    }

    return Math.min(score, 1);
  }

  /**
   * Popularity score based on enrollments and ratings
   */
  private async getPopularityScore(courseId: number): Promise<number> {
    const enrollmentCount = await this.interactionRepo.count({
      where: { courseId, type: InteractionType.ENROLL },
    });

    const avgRating = await this.interactionRepo
      .createQueryBuilder('interaction')
      .select('AVG(interaction.value)', 'avgRating')
      .where('interaction.courseId = :courseId', { courseId })
      .andWhere('interaction.type = :type', { type: InteractionType.RATE })
      .getRawOne();

    const enrollmentScore = Math.min(enrollmentCount / 100, 1) * 0.5;
    const ratingScore = (avgRating?.avgRating || 0) / 5 * 0.5;

    return enrollmentScore + ratingScore;
  }

  /**
   * Recency score: Favor newer courses
   */
  private getRecencyScore(createdAt: Date): number {
    const daysSinceCreation = (Date.now() - createdAt.getTime()) / (1000 * 60 * 60 * 24);

    if (daysSinceCreation < 7) return 1; // Very new
    if (daysSinceCreation < 30) return 0.7; // New
    if (daysSinceCreation < 90) return 0.4; // Recent
    return 0.2; // Older
  }

  /**
   * Get user's interaction history
   */
  private async getUserInteractions(userId: number): Promise<UserInteraction[]> {
    return this.interactionRepo.find({
      where: { userId },
      relations: ['course', 'course.category'],
      order: { createdAt: 'DESC' },
      take: 100, // Last 100 interactions
    });
  }

  /**
   * Get popular courses as fallback
   */
  private async getPopularCourses(limit: number): Promise<Course[]> {
    const popularCourseIds = await this.interactionRepo
      .createQueryBuilder('interaction')
      .select('interaction.courseId', 'courseId')
      .addSelect('COUNT(*)', 'interactionCount')
      .where('interaction.type IN (:...types)', {
        types: [InteractionType.ENROLL, InteractionType.RATE],
      })
      .andWhere('interaction.createdAt > :date', {
        date: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000), // Last 30 days
      })
      .groupBy('interaction.courseId')
      .orderBy('interactionCount', 'DESC')
      .limit(limit)
      .getRawMany();

    if (popularCourseIds.length === 0) {
      // Fallback to newest courses
      return this.courseRepo.find({
        where: { status: 'published' },
        order: { createdAt: 'DESC' },
        take: limit,
      });
    }

    const courseIds = popularCourseIds.map(p => p.courseId);

    return this.courseRepo
      .createQueryBuilder('course')
      .where('course.id IN (:...ids)', { ids: courseIds })
      .andWhere('course.status = :status', { status: 'published' })
      .getMany();
  }

  /**
   * Get similar courses based on content
   */
  async getSimilarCourses(courseId: number, limit: number = 5): Promise<Course[]> {
    const course = await this.courseRepo.findOne({
      where: { id: courseId },
      relations: ['category'],
    });

    if (!course) {
      return [];
    }

    // Find courses in same category or by same instructor
    return this.courseRepo
      .createQueryBuilder('course')
      .where('course.id != :courseId', { courseId })
      .andWhere('course.status = :status', { status: 'published' })
      .andWhere(
        '(course.categoryId = :categoryId OR course.instructorId = :instructorId)',
        {
          categoryId: course.categoryId,
          instructorId: course.instructorId,
        },
      )
      .orderBy('course.rating', 'DESC')
      .limit(limit)
      .getMany();
  }

  /**
   * Get trending courses
   */
  async getTrendingCourses(limit: number = 10): Promise<Course[]> {
    const last7Days = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000);

    const trendingCourseIds = await this.interactionRepo
      .createQueryBuilder('interaction')
      .select('interaction.courseId', 'courseId')
      .addSelect('COUNT(*)', 'interactionCount')
      .where('interaction.createdAt > :date', { date: last7Days })
      .groupBy('interaction.courseId')
      .orderBy('interactionCount', 'DESC')
      .limit(limit)
      .getRawMany();

    if (trendingCourseIds.length === 0) {
      return [];
    }

    const courseIds = trendingCourseIds.map(t => t.courseId);

    return this.courseRepo
      .createQueryBuilder('course')
      .where('course.id IN (:...ids)', { ids: courseIds })
      .andWhere('course.status = :status', { status: 'published' })
      .getMany();
  }
}
