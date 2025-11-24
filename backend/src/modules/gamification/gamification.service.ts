import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Badge, BadgeCategory, BadgeRarity } from './entities/badge.entity';
import { UserBadge } from './entities/user-badge.entity';
import {
  PointsTransaction,
  TransactionType,
} from './entities/points-transaction.entity';
import { User } from '../users/entities/user.entity';

interface LeaderboardEntry {
  userId: number;
  user: User;
  totalPoints: number;
  level: number;
  badgeCount: number;
  rank: number;
}

interface UserProgress {
  totalPoints: number;
  level: number;
  pointsToNextLevel: number;
  badges: UserBadge[];
  recentTransactions: PointsTransaction[];
  rank?: number;
}

/**
 * Gamification service for badges, points, and leaderboards
 */
@Injectable()
export class GamificationService {
  private readonly POINTS_PER_LEVEL = 1000;

  constructor(
    @InjectRepository(Badge)
    private badgeRepository: Repository<Badge>,
    @InjectRepository(UserBadge)
    private userBadgeRepository: Repository<UserBadge>,
    @InjectRepository(PointsTransaction)
    private transactionRepository: Repository<PointsTransaction>,
    @InjectRepository(User)
    private userRepository: Repository<User>,
  ) {}

  /**
   * Award points to user
   */
  async awardPoints(
    userId: number,
    points: number,
    type: TransactionType,
    description?: string,
    referenceId?: number,
    referenceType?: string,
  ): Promise<PointsTransaction> {
    const user = await this.userRepository.findOne({ where: { id: userId } });
    if (!user) {
      throw new Error('User not found');
    }

    // Update user's total points
    user.totalPoints = (user.totalPoints || 0) + points;
    user.level = this.calculateLevel(user.totalPoints);
    await this.userRepository.save(user);

    // Create transaction record
    const transaction = this.transactionRepository.create({
      userId,
      points,
      type,
      description,
      referenceId,
      referenceType,
      balanceAfter: user.totalPoints,
    });

    await this.transactionRepository.save(transaction);

    // Check for badge unlocks
    await this.checkBadgeUnlocks(userId);

    return transaction;
  }

  /**
   * Calculate user level from total points
   */
  calculateLevel(totalPoints: number): number {
    return Math.floor(totalPoints / this.POINTS_PER_LEVEL) + 1;
  }

  /**
   * Check and unlock eligible badges
   */
  async checkBadgeUnlocks(userId: number): Promise<UserBadge[]> {
    const user = await this.userRepository.findOne({ where: { id: userId } });
    if (!user) return [];

    const allBadges = await this.badgeRepository.find({
      where: { isActive: true },
    });

    const userBadges = await this.userBadgeRepository.find({
      where: { userId },
    });

    const unlockedBadgeIds = new Set(userBadges.map((ub) => ub.badgeId));
    const newlyUnlocked: UserBadge[] = [];

    for (const badge of allBadges) {
      if (unlockedBadgeIds.has(badge.id)) continue;

      if (await this.checkBadgeCriteria(userId, badge)) {
        const userBadge = await this.unlockBadge(userId, badge.id);
        newlyUnlocked.push(userBadge);
      }
    }

    return newlyUnlocked;
  }

  /**
   * Check if user meets badge criteria
   */
  private async checkBadgeCriteria(
    userId: number,
    badge: Badge,
  ): Promise<boolean> {
    if (!badge.criteria) return false;

    const { type, value, comparison = 'gte' } = badge.criteria;

    let userValue = 0;

    switch (type) {
      case 'total_points':
        const user = await this.userRepository.findOne({
          where: { id: userId },
        });
        userValue = user?.totalPoints || 0;
        break;

      case 'level':
        const userLevel = await this.userRepository.findOne({
          where: { id: userId },
        });
        userValue = userLevel?.level || 0;
        break;

      case 'courses_completed':
        // Count completed enrollments
        userValue = await this.userRepository
          .createQueryBuilder('user')
          .leftJoin('user.enrollments', 'enrollment')
          .where('user.id = :userId', { userId })
          .andWhere('enrollment.status = :status', { status: 'completed' })
          .getCount();
        break;

      case 'quizzes_passed':
        // Count passed quiz attempts
        userValue = await this.transactionRepository.count({
          where: { userId, type: TransactionType.QUIZ_PASSED },
        });
        break;

      case 'streak_days':
        // This would require streak calculation logic
        // Placeholder for now
        userValue = 0;
        break;

      case 'badges_earned':
        userValue = await this.userBadgeRepository.count({ where: { userId } });
        break;
    }

    switch (comparison) {
      case 'gte':
        return userValue >= value;
      case 'lte':
        return userValue <= value;
      case 'eq':
        return userValue === value;
      default:
        return false;
    }
  }

  /**
   * Unlock badge for user
   */
  async unlockBadge(userId: number, badgeId: number): Promise<UserBadge> {
    const badge = await this.badgeRepository.findOne({
      where: { id: badgeId },
    });

    if (!badge) {
      throw new Error('Badge not found');
    }

    // Check if already unlocked
    const existing = await this.userBadgeRepository.findOne({
      where: { userId, badgeId },
    });

    if (existing) {
      return existing;
    }

    // Create user badge
    const userBadge = this.userBadgeRepository.create({
      userId,
      badgeId,
      unlockedAt: new Date(),
    });

    await this.userBadgeRepository.save(userBadge);

    // Increment badge unlock count
    badge.unlockCount++;
    await this.badgeRepository.save(badge);

    // Award badge points
    if (badge.pointsReward > 0) {
      await this.awardPoints(
        userId,
        badge.pointsReward,
        TransactionType.BADGE_UNLOCKED,
        `Unlocked badge: ${badge.name}`,
        badgeId,
        'badge',
      );
    }

    return userBadge;
  }

  /**
   * Get leaderboard
   */
  async getLeaderboard(limit = 100): Promise<LeaderboardEntry[]> {
    const users = await this.userRepository
      .createQueryBuilder('user')
      .select([
        'user.id',
        'user.firstName',
        'user.lastName',
        'user.avatarUrl',
        'user.totalPoints',
        'user.level',
      ])
      .orderBy('user.totalPoints', 'DESC')
      .limit(limit)
      .getMany();

    const leaderboard: LeaderboardEntry[] = [];

    for (let i = 0; i < users.length; i++) {
      const user = users[i];
      const badgeCount = await this.userBadgeRepository.count({
        where: { userId: user.id },
      });

      leaderboard.push({
        userId: user.id,
        user,
        totalPoints: user.totalPoints || 0,
        level: user.level || 1,
        badgeCount,
        rank: i + 1,
      });
    }

    return leaderboard;
  }

  /**
   * Get user's rank
   */
  async getUserRank(userId: number): Promise<number> {
    const user = await this.userRepository.findOne({ where: { id: userId } });
    if (!user) return 0;

    const rank = await this.userRepository
      .createQueryBuilder('user')
      .where('user.totalPoints > :points', { points: user.totalPoints || 0 })
      .getCount();

    return rank + 1;
  }

  /**
   * Get user progress
   */
  async getUserProgress(userId: number): Promise<UserProgress> {
    const user = await this.userRepository.findOne({ where: { id: userId } });
    if (!user) {
      throw new Error('User not found');
    }

    const badges = await this.userBadgeRepository.find({
      where: { userId },
      relations: ['badge'],
      order: { unlockedAt: 'DESC' },
    });

    const recentTransactions = await this.transactionRepository.find({
      where: { userId },
      order: { createdAt: 'DESC' },
      take: 10,
    });

    const totalPoints = user.totalPoints || 0;
    const level = this.calculateLevel(totalPoints);
    const pointsToNextLevel =
      level * this.POINTS_PER_LEVEL - totalPoints;

    const rank = await this.getUserRank(userId);

    return {
      totalPoints,
      level,
      pointsToNextLevel,
      badges,
      recentTransactions,
      rank,
    };
  }

  /**
   * Get all available badges
   */
  async getAllBadges(): Promise<Badge[]> {
    return this.badgeRepository.find({
      where: { isActive: true },
      order: { rarity: 'DESC', name: 'ASC' },
    });
  }

  /**
   * Get user's badges
   */
  async getUserBadges(userId: number): Promise<UserBadge[]> {
    return this.userBadgeRepository.find({
      where: { userId },
      relations: ['badge'],
      order: { unlockedAt: 'DESC' },
    });
  }

  /**
   * Create default badges
   */
  async seedBadges(): Promise<void> {
    const defaultBadges = [
      // Learning badges
      {
        name: 'First Steps',
        description: 'Complete your first lesson',
        icon: '🎯',
        category: BadgeCategory.LEARNING,
        rarity: BadgeRarity.COMMON,
        pointsReward: 10,
        criteria: { type: 'lessons_completed', value: 1, comparison: 'gte' as const },
      },
      {
        name: 'Knowledge Seeker',
        description: 'Complete 10 lessons',
        icon: '📚',
        category: BadgeCategory.LEARNING,
        rarity: BadgeRarity.UNCOMMON,
        pointsReward: 50,
        criteria: { type: 'lessons_completed', value: 10, comparison: 'gte' as const },
      },
      {
        name: 'Course Master',
        description: 'Complete your first course',
        icon: '🎓',
        category: BadgeCategory.ACHIEVEMENT,
        rarity: BadgeRarity.UNCOMMON,
        pointsReward: 100,
        criteria: { type: 'courses_completed', value: 1, comparison: 'gte' as const },
      },
      {
        name: 'Quiz Champion',
        description: 'Pass 5 quizzes',
        icon: '🏆',
        category: BadgeCategory.ACHIEVEMENT,
        rarity: BadgeRarity.UNCOMMON,
        pointsReward: 75,
        criteria: { type: 'quizzes_passed', value: 5, comparison: 'gte' as const },
      },
      {
        name: 'Perfect Score',
        description: 'Get 100% on any quiz',
        icon: '💯',
        category: BadgeCategory.ACHIEVEMENT,
        rarity: BadgeRarity.RARE,
        pointsReward: 150,
        criteria: { type: 'perfect_quiz', value: 1, comparison: 'gte' as const },
      },
      {
        name: 'Point Collector',
        description: 'Earn 1000 points',
        icon: '💰',
        category: BadgeCategory.ACHIEVEMENT,
        rarity: BadgeRarity.UNCOMMON,
        pointsReward: 100,
        criteria: { type: 'total_points', value: 1000, comparison: 'gte' as const },
      },
      {
        name: 'Point Master',
        description: 'Earn 5000 points',
        icon: '💎',
        category: BadgeCategory.ACHIEVEMENT,
        rarity: BadgeRarity.RARE,
        pointsReward: 250,
        criteria: { type: 'total_points', value: 5000, comparison: 'gte' as const },
      },
      {
        name: 'Rising Star',
        description: 'Reach level 5',
        icon: '⭐',
        category: BadgeCategory.ACHIEVEMENT,
        rarity: BadgeRarity.RARE,
        pointsReward: 200,
        criteria: { type: 'level', value: 5, comparison: 'gte' as const },
      },
      {
        name: 'Legend',
        description: 'Reach level 10',
        icon: '👑',
        category: BadgeCategory.ACHIEVEMENT,
        rarity: BadgeRarity.EPIC,
        pointsReward: 500,
        criteria: { type: 'level', value: 10, comparison: 'gte' as const },
      },
      {
        name: 'Badge Collector',
        description: 'Unlock 5 badges',
        icon: '🎖️',
        category: BadgeCategory.SPECIAL,
        rarity: BadgeRarity.RARE,
        pointsReward: 150,
        criteria: { type: 'badges_earned', value: 5, comparison: 'gte' as const },
      },
    ];

    for (const badgeData of defaultBadges) {
      const existing = await this.badgeRepository.findOne({
        where: { name: badgeData.name },
      });

      if (!existing) {
        const badge = this.badgeRepository.create(badgeData);
        await this.badgeRepository.save(badge);
      }
    }
  }

  /**
   * Toggle badge showcase
   */
  async toggleBadgeShowcase(
    userId: number,
    badgeId: number,
  ): Promise<UserBadge> {
    const userBadge = await this.userBadgeRepository.findOne({
      where: { userId, badgeId },
    });

    if (!userBadge) {
      throw new Error('Badge not unlocked');
    }

    userBadge.isShowcased = !userBadge.isShowcased;
    return this.userBadgeRepository.save(userBadge);
  }

  /**
   * Get showcased badges for user
   */
  async getShowcasedBadges(userId: number): Promise<UserBadge[]> {
    return this.userBadgeRepository.find({
      where: { userId, isShowcased: true },
      relations: ['badge'],
      order: { unlockedAt: 'DESC' },
    });
  }
}
