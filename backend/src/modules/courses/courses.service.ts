import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Course } from './entities/course.entity';
import { CreateCourseDto } from './dto/create-course.dto';
import { UpdateCourseDto } from './dto/update-course.dto';
import { CourseStatus } from '../../common/enums/course-status.enum';

@Injectable()
export class CoursesService {
  constructor(
    @InjectRepository(Course)
    private coursesRepository: Repository<Course>,
  ) {}

  async create(createCourseDto: CreateCourseDto, instructorId: number): Promise<Course> {
    const course = this.coursesRepository.create({
      ...createCourseDto,
      instructorId,
      status: CourseStatus.DRAFT,
    });
    return this.coursesRepository.save(course);
  }

  async findAll(page: number = 1, limit: number = 20): Promise<{ data: Course[]; total: number }> {
    const [data, total] = await this.coursesRepository.findAndCount({
      where: { status: CourseStatus.PUBLISHED },
      relations: ['instructor', 'category', 'modules'],
      take: limit,
      skip: (page - 1) * limit,
      order: { createdAt: 'DESC' },
    });

    return { data, total };
  }

  async findById(id: number): Promise<Course> {
    const course = await this.coursesRepository.findOne({
      where: { id },
      relations: ['instructor', 'category', 'modules', 'modules.lessons'],
    });

    if (!course) {
      throw new NotFoundException('Course not found');
    }

    return course;
  }

  async update(id: number, updateCourseDto: UpdateCourseDto): Promise<Course> {
    const course = await this.findById(id);
    Object.assign(course, updateCourseDto);
    return this.coursesRepository.save(course);
  }

  async publish(id: number): Promise<Course> {
    const course = await this.findById(id);
    course.status = CourseStatus.PUBLISHED;
    course.publishedAt = new Date();
    return this.coursesRepository.save(course);
  }

  async remove(id: number): Promise<void> {
    const course = await this.findById(id);
    await this.coursesRepository.softRemove(course);
  }
}
