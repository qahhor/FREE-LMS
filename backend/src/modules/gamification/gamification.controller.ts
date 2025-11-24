import {
  Controller,
  Get,
  Post,
  Param,
  Query,
  UseGuards,
  ParseIntPipe,
} from '@nestjs/common';
import {
  ApiTags,
  ApiOperation,
  ApiResponse,
  ApiBearerAuth,
} from '@nestjs/swagger';
import { GamificationService } from './gamification.service';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { CurrentUser } from '../../common/decorators/current-user.decorator';
import { User } from '../users/entities/user.entity';
import { Public } from '../../common/decorators/public.decorator';

/**
 * Gamification endpoints for badges, points, and leaderboards
 */
@ApiTags('gamification')
@Controller('gamification')
export class GamificationController {
  constructor(private readonly gamificationService: GamificationService) {}

  @Get('leaderboard')
  @Public()
  @ApiOperation({ summary: 'Get global leaderboard' })
  @ApiResponse({ status: 200, description: 'Leaderboard data' })
  async getLeaderboard(@Query('limit') limit?: number) {
    return this.gamificationService.getLeaderboard(limit || 100);
  }

  @Get('my-progress')
  @UseGuards(JwtAuthGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Get current user progress and stats' })
  @ApiResponse({ status: 200, description: 'User progress data' })
  async getMyProgress(@CurrentUser() user: User) {
    return this.gamificationService.getUserProgress(user.id);
  }

  @Get('my-badges')
  @UseGuards(JwtAuthGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Get current user badges' })
  @ApiResponse({ status: 200, description: 'User badges' })
  async getMyBadges(@CurrentUser() user: User) {
    return this.gamificationService.getUserBadges(user.id);
  }

  @Get('badges')
  @Public()
  @ApiOperation({ summary: 'Get all available badges' })
  @ApiResponse({ status: 200, description: 'All badges' })
  async getAllBadges() {
    return this.gamificationService.getAllBadges();
  }

  @Post('badges/:badgeId/showcase')
  @UseGuards(JwtAuthGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Toggle badge showcase on profile' })
  @ApiResponse({ status: 200, description: 'Badge showcase toggled' })
  async toggleBadgeShowcase(
    @Param('badgeId', ParseIntPipe) badgeId: number,
    @CurrentUser() user: User,
  ) {
    return this.gamificationService.toggleBadgeShowcase(user.id, badgeId);
  }

  @Get('showcased-badges/:userId')
  @Public()
  @ApiOperation({ summary: 'Get showcased badges for user' })
  @ApiResponse({ status: 200, description: 'Showcased badges' })
  async getShowcasedBadges(@Param('userId', ParseIntPipe) userId: number) {
    return this.gamificationService.getShowcasedBadges(userId);
  }

  @Get('rank')
  @UseGuards(JwtAuthGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Get current user rank' })
  @ApiResponse({ status: 200, description: 'User rank' })
  async getMyRank(@CurrentUser() user: User) {
    const rank = await this.gamificationService.getUserRank(user.id);
    return { rank };
  }

  @Post('seed-badges')
  @Public()
  @ApiOperation({ summary: 'Seed default badges (dev only)' })
  @ApiResponse({ status: 200, description: 'Badges seeded' })
  async seedBadges() {
    await this.gamificationService.seedBadges();
    return { message: 'Default badges created' };
  }
}
