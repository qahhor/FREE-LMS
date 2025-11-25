import {
  Controller,
  Get,
  Query,
  UseGuards,
  ParseIntPipe,
} from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse, ApiBearerAuth } from '@nestjs/swagger';
import { AdvancedAnalyticsService } from './advanced-analytics.service';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { CurrentUser } from '../../common/decorators/current-user.decorator';
import { User } from '../users/entities/user.entity';
import { RateLimit } from '../../common/guards/rate-limit.guard';
import { Cacheable } from '../../common/decorators/cacheable.decorator';
import { Roles } from '../../common/decorators/roles.decorator';
import { RolesGuard } from '../../common/guards/roles.guard';
import { UserRole } from '../../common/enums/user-role.enum';

@ApiTags('advanced-analytics')
@Controller('analytics/advanced')
@UseGuards(JwtAuthGuard)
@ApiBearerAuth()
export class AdvancedAnalyticsController {
  constructor(private advancedAnalyticsService: AdvancedAnalyticsService) {}

  @Get('student-risk')
  @RateLimit(20, 60)
  @ApiOperation({ summary: 'Assess student at-risk level' })
  @ApiResponse({ status: 200, description: 'Risk assessment' })
  async assessStudentRisk(
    @CurrentUser() user: User,
    @Query('courseId', ParseIntPipe) courseId?: number,
  ) {
    const assessments = await this.advancedAnalyticsService.assessStudentRisk(
      user.id,
      courseId,
    );

    return { assessments };
  }

  @Get('predict-completion')
  @RateLimit(20, 60)
  @Cacheable('analytics:predict', 300)
  @ApiOperation({ summary: 'Predict course completion' })
  @ApiResponse({ status: 200, description: 'Completion prediction' })
  async predictCompletion(
    @CurrentUser() user: User,
    @Query('courseId', ParseIntPipe) courseId: number,
  ) {
    const prediction = await this.advancedAnalyticsService.predictCompletion(
      user.id,
      courseId,
    );

    return { prediction };
  }

  @Get('learning-path')
  @RateLimit(20, 60)
  @Cacheable('analytics:learning-path', 600)
  @ApiOperation({ summary: 'Get optimized learning path' })
  @ApiResponse({ status: 200, description: 'Learning path recommendation' })
  async optimizeLearningPath(
    @CurrentUser() user: User,
    @Query('courseId', ParseIntPipe) courseId: number,
  ) {
    const recommendation = await this.advancedAnalyticsService.optimizeLearningPath(
      user.id,
      courseId,
    );

    return { recommendation };
  }

  @Get('performance-metrics')
  @RateLimit(20, 60)
  @Cacheable('analytics:performance', 300)
  @ApiOperation({ summary: 'Get performance metrics summary' })
  @ApiResponse({ status: 200, description: 'Performance metrics' })
  async getPerformanceMetrics(@CurrentUser() user: User) {
    const metrics = await this.advancedAnalyticsService.getPerformanceMetrics(
      user.id,
    );

    return { metrics };
  }

  @Get('cohort-analysis')
  @UseGuards(RolesGuard)
  @Roles(UserRole.INSTRUCTOR, UserRole.ADMIN)
  @RateLimit(10, 60)
  @Cacheable('analytics:cohort', 600)
  @ApiOperation({ summary: 'Get cohort analysis for course (Instructor/Admin only)' })
  @ApiResponse({ status: 200, description: 'Cohort analysis' })
  async getCohortAnalysis(@Query('courseId', ParseIntPipe) courseId: number) {
    const analysis = await this.advancedAnalyticsService.getCohortAnalysis(courseId);

    return { analysis };
  }
}
