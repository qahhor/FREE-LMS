import {
  Controller,
  Get,
  Post,
  Body,
  Param,
  UseGuards,
  ParseIntPipe,
  Query,
} from '@nestjs/common';
import {
  ApiTags,
  ApiOperation,
  ApiResponse,
  ApiBearerAuth,
} from '@nestjs/swagger';
import { VideoService } from './video.service';
import { Video } from './entities/video.entity';
import { VideoProgress } from './entities/video-progress.entity';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { CurrentUser } from '../../common/decorators/current-user.decorator';
import { User } from '../users/entities/user.entity';

/**
 * Video streaming and progress tracking endpoints
 */
@ApiTags('videos')
@Controller('videos')
@UseGuards(JwtAuthGuard)
@ApiBearerAuth()
export class VideoController {
  constructor(private readonly videoService: VideoService) {}

  @Get(':id')
  @ApiOperation({ summary: 'Get video metadata' })
  @ApiResponse({ status: 200, description: 'Video data' })
  @ApiResponse({ status: 404, description: 'Video not found' })
  async findOne(
    @Param('id', ParseIntPipe) id: number,
    @CurrentUser() user: User,
  ): Promise<Video> {
    return this.videoService.findById(id, user.id);
  }

  @Get(':id/stream-url')
  @ApiOperation({ summary: 'Get HLS streaming URL' })
  @ApiResponse({ status: 200, description: 'Streaming URL with token' })
  async getStreamingUrl(
    @Param('id', ParseIntPipe) id: number,
    @CurrentUser() user: User,
  ): Promise<{ url: string }> {
    const url = await this.videoService.getStreamingUrl(id, user.id);
    return { url };
  }

  @Post(':id/progress')
  @ApiOperation({ summary: 'Update video progress' })
  @ApiResponse({ status: 200, description: 'Progress updated' })
  async updateProgress(
    @Param('id', ParseIntPipe) id: number,
    @CurrentUser() user: User,
    @Body() body: { position: number; watchTime?: number },
  ): Promise<VideoProgress> {
    return this.videoService.updateProgress(
      user.id,
      id,
      body.position,
      body.watchTime,
    );
  }

  @Get(':id/progress')
  @ApiOperation({ summary: 'Get video progress' })
  @ApiResponse({ status: 200, description: 'Progress data' })
  async getProgress(
    @Param('id', ParseIntPipe) id: number,
    @CurrentUser() user: User,
  ): Promise<VideoProgress | null> {
    return this.videoService.getProgress(user.id, id);
  }

  @Post(':id/view')
  @ApiOperation({ summary: 'Increment view count' })
  @ApiResponse({ status: 200, description: 'View count incremented' })
  async incrementView(
    @Param('id', ParseIntPipe) id: number,
  ): Promise<{ message: string }> {
    await this.videoService.incrementViewCount(id);
    return { message: 'View count incremented' };
  }
}
