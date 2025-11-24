import { Entity, Column, OneToMany } from 'typeorm';
import { BaseEntity } from '../../../common/entities/base.entity';
import { UserBadge } from './user-badge.entity';

export enum BadgeCategory {
  LEARNING = 'learning',
  ACHIEVEMENT = 'achievement',
  SOCIAL = 'social',
  SPECIAL = 'special',
}

export enum BadgeRarity {
  COMMON = 'common',
  UNCOMMON = 'uncommon',
  RARE = 'rare',
  EPIC = 'epic',
  LEGENDARY = 'legendary',
}

/**
 * Badge entity for gamification achievements
 */
@Entity('badges')
export class Badge extends BaseEntity {
  @Column({ type: 'varchar', length: 255 })
  name: string;

  @Column({ type: 'text' })
  description: string;

  @Column({ type: 'varchar', length: 500 })
  icon: string; // URL or emoji

  @Column({
    type: 'enum',
    enum: BadgeCategory,
    default: BadgeCategory.ACHIEVEMENT,
  })
  category: BadgeCategory;

  @Column({
    type: 'enum',
    enum: BadgeRarity,
    default: BadgeRarity.COMMON,
  })
  rarity: BadgeRarity;

  @Column({ name: 'points_reward', type: 'int', default: 0 })
  pointsReward: number;

  // Unlock criteria
  @Column({ type: 'json', nullable: true })
  criteria: {
    type: string; // 'courses_completed', 'quizzes_passed', 'streak_days', etc.
    value: number;
    comparison?: 'gte' | 'lte' | 'eq'; // greater than or equal, less than or equal, equal
  };

  @Column({ name: 'is_active', type: 'boolean', default: true })
  isActive: boolean;

  @Column({ name: 'unlock_count', type: 'int', default: 0 })
  unlockCount: number; // How many users have unlocked this badge

  @OneToMany(() => UserBadge, (userBadge) => userBadge.badge)
  userBadges: UserBadge[];
}
