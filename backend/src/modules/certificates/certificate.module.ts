import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { CertificateController } from './certificate.controller';
import { CertificateService } from './certificate.service';
import { Certificate } from './entities/certificate.entity';
import { Enrollment } from '../enrollments/entities/enrollment.entity';
import { User } from '../users/entities/user.entity';
import { Course } from '../courses/entities/course.entity';

@Module({
  imports: [TypeOrmModule.forFeature([Certificate, Enrollment, User, Course])],
  controllers: [CertificateController],
  providers: [CertificateService],
  exports: [CertificateService],
})
export class CertificateModule {}
