import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { SmartSearchController } from './smart-search.controller';
import { SmartSearchService } from './smart-search.service';
import { Course } from '../courses/entities/course.entity';

@Module({
  imports: [TypeOrmModule.forFeature([Course])],
  controllers: [SmartSearchController],
  providers: [SmartSearchService],
  exports: [SmartSearchService],
})
export class SearchModule {}
