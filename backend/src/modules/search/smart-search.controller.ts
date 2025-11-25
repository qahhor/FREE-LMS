import {
  Controller,
  Get,
  Query,
  ParseIntPipe,
} from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse } from '@nestjs/swagger';
import { SmartSearchService } from './smart-search.service';
import { RateLimit } from '../../common/guards/rate-limit.guard';
import { Cacheable } from '../../common/decorators/cacheable.decorator';

@ApiTags('search')
@Controller('search')
export class SmartSearchController {
  constructor(private smartSearchService: SmartSearchService) {}

  @Get()
  @RateLimit(50, 60)
  @Cacheable('search:query', 180) // Cache for 3 minutes
  @ApiOperation({ summary: 'Smart search with NLP' })
  @ApiResponse({ status: 200, description: 'Search results with relevance scores' })
  async search(
    @Query('q') query: string,
    @Query('limit', ParseIntPipe) limit: number = 20,
    @Query('categoryId', ParseIntPipe) categoryId?: number,
    @Query('level') level?: string,
    @Query('minRating', ParseIntPipe) minRating?: number,
  ) {
    const results = await this.smartSearchService.search(query, {
      limit,
      categoryId,
      level,
      minRating,
    });

    return {
      query,
      results: results.map(r => ({
        ...r.course,
        _relevanceScore: r.relevanceScore,
        _matchedTerms: r.matchedTerms,
      })),
      total: results.length,
    };
  }

  @Get('suggestions')
  @RateLimit(100, 60)
  @Cacheable('search:suggestions', 300)
  @ApiOperation({ summary: 'Get search suggestions (autocomplete)' })
  @ApiResponse({ status: 200, description: 'Search suggestions' })
  async getSuggestions(
    @Query('q') query: string,
    @Query('limit', ParseIntPipe) limit: number = 5,
  ) {
    const suggestions = await this.smartSearchService.getSuggestions(query, limit);

    return { suggestions };
  }

  @Get('popular')
  @RateLimit(50, 60)
  @Cacheable('search:popular', 3600) // Cache for 1 hour
  @ApiOperation({ summary: 'Get popular searches' })
  @ApiResponse({ status: 200, description: 'Popular search queries' })
  async getPopularSearches(@Query('limit', ParseIntPipe) limit: number = 10) {
    const popular = await this.smartSearchService.getPopularSearches(limit);

    return { popular };
  }
}
