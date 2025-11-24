import { Entity, Column, ManyToOne, JoinColumn, Index } from 'typeorm';
import { BaseEntity } from '../../../common/entities/base.entity';
import { User } from '../../users/entities/user.entity';
import { Badge } from './badge.entity';

/**
 * User badge unlocks
 */
@Entity('user_badges')
@Index(['userId', 'badgeId'], { unique: true })
export class UserBadge extends BaseEntity {
  @Column({ name: 'user_id' })
  userId: number;

  @ManyToOne(() => User)
  @JoinColumn({ name: 'user_id' })
  user: User;

  @Column({ name: 'badge_id' })
  badgeId: number;

  @ManyToOne(() => Badge, (badge) => badge.userBadges)
  @JoinColumn({ name: 'badge_id' })
  badge: Badge;

  @Column({ name: 'unlocked_at', type: 'timestamp' })
  unlockedAt: Date;

  @Column({ name: 'is_showcased', type: 'boolean', default: false })
  isShowcased: boolean; // Display on profile

  @Column({ name: 'view_count', type: 'int', default: 0 })
  viewCount: number;
}
