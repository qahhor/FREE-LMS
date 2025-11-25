import { Injectable, Logger } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Course } from '../courses/entities/course.entity';

interface SearchResult {
  course: Course;
  relevanceScore: number;
  matchedTerms: string[];
}

@Injectable()
export class SmartSearchService {
  private readonly logger = new Logger(SmartSearchService.name);

  // Common stop words in Russian
  private readonly stopWords = new Set([
    'и', 'в', 'на', 'с', 'по', 'для', 'от', 'к', 'из', 'о', 'об',
    'the', 'a', 'an', 'in', 'on', 'at', 'to', 'for', 'of', 'with',
  ]);

  // Synonym mappings for query expansion
  private readonly synonyms: Record<string, string[]> = {
    'программирование': ['coding', 'кодинг', 'разработка', 'dev'],
    'javascript': ['js', 'джаваскрипт', 'ecmascript'],
    'python': ['пайтон', 'питон', 'py'],
    'design': ['дизайн', 'проектирование', 'ui', 'ux'],
    'marketing': ['маркетинг', 'smm', 'продвижение'],
    'business': ['бизнес', 'предпринимательство', 'entrepreneurship'],
  };

  constructor(
    @InjectRepository(Course)
    private courseRepo: Repository<Course>,
  ) {}

  /**
   * Smart search with NLP features
   */
  async search(query: string, options?: {
    limit?: number;
    categoryId?: number;
    level?: string;
    minRating?: number;
  }): Promise<SearchResult[]> {
    try {
      // Step 1: Process query with NLP
      const processedQuery = this.processQuery(query);
      const expandedTerms = this.expandQueryWithSynonyms(processedQuery.terms);

      // Step 2: Build PostgreSQL full-text search query
      const searchQuery = this.buildSearchQuery(expandedTerms);

      // Step 3: Execute search with ranking
      let queryBuilder = this.courseRepo
        .createQueryBuilder('course')
        .where('course.status = :status', { status: 'published' });

      // Add full-text search
      if (searchQuery) {
        queryBuilder = queryBuilder.andWhere(
          `(
            to_tsvector('russian', course.title || ' ' || course.description) @@ plainto_tsquery('russian', :query)
            OR to_tsvector('english', course.title || ' ' || course.description) @@ plainto_tsquery('english', :query)
          )`,
          { query: searchQuery }
        );
      }

      // Add filters
      if (options?.categoryId) {
        queryBuilder = queryBuilder.andWhere('course.categoryId = :categoryId', {
          categoryId: options.categoryId,
        });
      }

      if (options?.level) {
        queryBuilder = queryBuilder.andWhere('course.level = :level', {
          level: options.level,
        });
      }

      if (options?.minRating) {
        queryBuilder = queryBuilder.andWhere('course.rating >= :minRating', {
          minRating: options.minRating,
        });
      }

      // Execute query
      const courses = await queryBuilder
        .leftJoinAndSelect('course.category', 'category')
        .leftJoinAndSelect('course.instructor', 'instructor')
        .take(options?.limit || 20)
        .getMany();

      // Step 4: Calculate relevance scores
      const results = courses.map(course => {
        const relevanceScore = this.calculateRelevanceScore(course, processedQuery, expandedTerms);
        const matchedTerms = this.findMatchedTerms(course, expandedTerms);

        return {
          course,
          relevanceScore,
          matchedTerms,
        };
      });

      // Step 5: Sort by relevance
      results.sort((a, b) => b.relevanceScore - a.relevanceScore);

      return results;
    } catch (error) {
      this.logger.error('Smart search error:', error);
      // Fallback to basic search
      return this.fallbackSearch(query, options);
    }
  }

  /**
   * Get search suggestions (autocomplete)
   */
  async getSuggestions(query: string, limit: number = 5): Promise<string[]> {
    const processedQuery = this.processQuery(query);

    if (processedQuery.terms.length === 0) {
      return [];
    }

    // Get courses that match the beginning of the query
    const courses = await this.courseRepo
      .createQueryBuilder('course')
      .select('DISTINCT course.title', 'title')
      .where('course.status = :status', { status: 'published' })
      .andWhere('LOWER(course.title) LIKE :query', {
        query: `%${query.toLowerCase()}%`,
      })
      .limit(limit)
      .getRawMany();

    return courses.map(c => c.title);
  }

  /**
   * Get popular search queries
   */
  async getPopularSearches(limit: number = 10): Promise<string[]> {
    // In production, this would query a search_logs table
    // For now, return predefined popular searches
    return [
      'JavaScript для начинающих',
      'Python разработка',
      'UI/UX дизайн',
      'Цифровой маркетинг',
      'Data Science',
      'React.js',
      'Machine Learning',
      'Фотография',
      'Английский язык',
      'Бизнес аналитика',
    ].slice(0, limit);
  }

  /**
   * Process query with NLP techniques
   */
  private processQuery(query: string): {
    original: string;
    normalized: string;
    terms: string[];
  } {
    // Normalize: lowercase, trim
    const normalized = query.toLowerCase().trim();

    // Tokenize
    let terms = normalized.split(/\s+/);

    // Remove stop words
    terms = terms.filter(term => !this.stopWords.has(term));

    // Stem words (simplified - just remove common endings)
    terms = terms.map(term => this.simpleStem(term));

    return {
      original: query,
      normalized,
      terms,
    };
  }

  /**
   * Simple stemming (remove common endings)
   */
  private simpleStem(word: string): string {
    // Russian endings
    if (word.endsWith('ние')) return word.slice(0, -3);
    if (word.endsWith('ость')) return word.slice(0, -4);
    if (word.endsWith('ство')) return word.slice(0, -4);

    // English endings
    if (word.endsWith('ing')) return word.slice(0, -3);
    if (word.endsWith('tion')) return word.slice(0, -4);
    if (word.endsWith('ness')) return word.slice(0, -4);

    return word;
  }

  /**
   * Expand query with synonyms
   */
  private expandQueryWithSynonyms(terms: string[]): string[] {
    const expanded = new Set(terms);

    for (const term of terms) {
      // Add synonyms
      for (const [key, synonyms] of Object.entries(this.synonyms)) {
        if (key === term || synonyms.includes(term)) {
          expanded.add(key);
          synonyms.forEach(s => expanded.add(s));
        }
      }
    }

    return Array.from(expanded);
  }

  /**
   * Build PostgreSQL search query
   */
  private buildSearchQuery(terms: string[]): string {
    return terms.join(' | '); // OR operator for full-text search
  }

  /**
   * Calculate relevance score
   */
  private calculateRelevanceScore(
    course: Course,
    processedQuery: { terms: string[] },
    expandedTerms: string[],
  ): number {
    let score = 0;

    const titleLower = course.title.toLowerCase();
    const descLower = course.description?.toLowerCase() || '';

    // Exact match in title (highest weight)
    if (titleLower.includes(processedQuery.terms.join(' '))) {
      score += 100;
    }

    // Term matches in title
    for (const term of expandedTerms) {
      if (titleLower.includes(term)) {
        score += 10;
      }
      if (descLower.includes(term)) {
        score += 5;
      }
    }

    // Boost by rating
    score += (course.rating || 0) * 2;

    // Boost by popularity (student count)
    score += Math.log10((course.studentCount || 0) + 1) * 3;

    return score;
  }

  /**
   * Find matched terms
   */
  private findMatchedTerms(course: Course, terms: string[]): string[] {
    const matched: string[] = [];
    const titleLower = course.title.toLowerCase();
    const descLower = course.description?.toLowerCase() || '';

    for (const term of terms) {
      if (titleLower.includes(term) || descLower.includes(term)) {
        matched.push(term);
      }
    }

    return matched;
  }

  /**
   * Fallback search (basic LIKE query)
   */
  private async fallbackSearch(
    query: string,
    options?: { limit?: number; categoryId?: number },
  ): Promise<SearchResult[]> {
    const courses = await this.courseRepo
      .createQueryBuilder('course')
      .where('course.status = :status', { status: 'published' })
      .andWhere(
        '(LOWER(course.title) LIKE :query OR LOWER(course.description) LIKE :query)',
        { query: `%${query.toLowerCase()}%` }
      )
      .leftJoinAndSelect('course.category', 'category')
      .leftJoinAndSelect('course.instructor', 'instructor')
      .take(options?.limit || 20)
      .getMany();

    return courses.map(course => ({
      course,
      relevanceScore: 50,
      matchedTerms: [query],
    }));
  }
}
