import {
  Controller,
  Get,
  Post,
  Body,
  Param,
  Query,
  UseGuards,
  ParseIntPipe,
} from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse, ApiBearerAuth } from '@nestjs/swagger';
import { RecommendationsService } from './recommendations.service';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { CurrentUser } from '../../common/decorators/current-user.decorator';
import { User } from '../users/entities/user.entity';
import { RateLimit } from '../../common/guards/rate-limit.guard';
import { Cacheable } from '../../common/decorators/cacheable.decorator';
import { InteractionType } from './entities/user-interaction.entity';

@ApiTags('recommendations')
@Controller('recommendations')
export class RecommendationsController {
  constructor(private recommendationsService: RecommendationsService) {}

  @Get('for-you')
  @UseGuards(JwtAuthGuard)
  @ApiBearerAuth()
  @Cacheable('recommendations:for-you', 300) // Cache for 5 minutes
  @RateLimit(20, 60)
  @ApiOperation({ summary: 'Get personalized course recommendations' })
  @ApiResponse({ status: 200, description: 'Personalized recommendations' })
  async getPersonalizedRecommendations(
    @CurrentUser() user: User,
    @Query('limit', ParseIntPipe) limit: number = 10,
  ) {
    const courses = await this.recommendationsService.getRecommendations(
      user.id,
      limit,
    );

    return {
      recommendations: courses,
      count: courses.length,
    };
  }

  @Get('similar/:courseId')
  @Cacheable('recommendations:similar', 600) // Cache for 10 minutes
  @RateLimit(50, 60)
  @ApiOperation({ summary: 'Get similar courses' })
  @ApiResponse({ status: 200, description: 'Similar courses' })
  async getSimilarCourses(
    @Param('courseId', ParseIntPipe) courseId: number,
    @Query('limit', ParseIntPipe) limit: number = 5,
  ) {
    const courses = await this.recommendationsService.getSimilarCourses(
      courseId,
      limit,
    );

    return {
      similar: courses,
      count: courses.length,
    };
  }

  @Get('trending')
  @Cacheable('recommendations:trending', 300) // Cache for 5 minutes
  @RateLimit(50, 60)
  @ApiOperation({ summary: 'Get trending courses' })
  @ApiResponse({ status: 200, description: 'Trending courses' })
  async getTrendingCourses(@Query('limit', ParseIntPipe) limit: number = 10) {
    const courses = await this.recommendationsService.getTrendingCourses(limit);

    return {
      trending: courses,
      count: courses.length,
    };
  }

  @Post('interaction')
  @UseGuards(JwtAuthGuard)
  @ApiBearerAuth()
  @RateLimit(100, 60)
  @ApiOperation({ summary: 'Record user interaction' })
  @ApiResponse({ status: 201, description: 'Interaction recorded' })
  async recordInteraction(
    @CurrentUser() user: User,
    @Body()
    body: {
      type: InteractionType;
      courseId?: number;
      value?: number;
      metadata?: Record<string, any>;
    },
  ) {
    const interaction = await this.recommendationsService.recordInteraction(
      user.id,
      body.type,
      body.courseId,
      body.value,
      body.metadata,
    );

    return {
      success: true,
      interactionId: interaction.id,
    };
  }
}
