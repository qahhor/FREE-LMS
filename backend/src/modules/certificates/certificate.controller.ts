import {
  Controller,
  Get,
  Post,
  Param,
  Query,
  UseGuards,
  ParseIntPipe,
  Delete,
  Body,
} from '@nestjs/common';
import {
  ApiTags,
  ApiOperation,
  ApiResponse,
  ApiBearerAuth,
} from '@nestjs/swagger';
import { CertificateService } from './certificate.service';
import { Certificate } from './entities/certificate.entity';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { Roles } from '../../common/decorators/roles.decorator';
import { RolesGuard } from '../../common/guards/roles.guard';
import { UserRole } from '../../common/enums/user-role.enum';
import { CurrentUser } from '../../common/decorators/current-user.decorator';
import { User } from '../users/entities/user.entity';
import { Public } from '../../common/decorators/public.decorator';

/**
 * Certificate management endpoints
 */
@ApiTags('certificates')
@Controller('certificates')
export class CertificateController {
  constructor(private readonly certificateService: CertificateService) {}

  @Post('generate/:courseId')
  @UseGuards(JwtAuthGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Generate certificate for completed course' })
  @ApiResponse({ status: 201, description: 'Certificate generated' })
  @ApiResponse({ status: 400, description: 'Course not completed' })
  async generateCertificate(
    @Param('courseId', ParseIntPipe) courseId: number,
    @CurrentUser() user: User,
  ): Promise<Certificate> {
    return this.certificateService.generateCertificate(user.id, courseId);
  }

  @Get('my-certificates')
  @UseGuards(JwtAuthGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Get all certificates for current user' })
  @ApiResponse({ status: 200, description: 'User certificates' })
  async getMyCertificates(@CurrentUser() user: User): Promise<Certificate[]> {
    return this.certificateService.getUserCertificates(user.id);
  }

  @Get(':id')
  @UseGuards(JwtAuthGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Get certificate by ID' })
  @ApiResponse({ status: 200, description: 'Certificate data' })
  @ApiResponse({ status: 404, description: 'Certificate not found' })
  async getCertificate(
    @Param('id', ParseIntPipe) id: number,
    @CurrentUser() user: User,
  ): Promise<Certificate> {
    return this.certificateService.getCertificateById(id, user.id);
  }

  @Get(':id/download')
  @UseGuards(JwtAuthGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Download certificate PDF' })
  @ApiResponse({ status: 200, description: 'Certificate download tracked' })
  async downloadCertificate(
    @Param('id', ParseIntPipe) id: number,
    @CurrentUser() user: User,
  ): Promise<{ url: string }> {
    const certificate = await this.certificateService.getCertificateById(
      id,
      user.id,
    );
    await this.certificateService.trackDownload(id);
    return { url: certificate.pdfUrl };
  }

  @Get('verify/:code')
  @Public()
  @ApiOperation({ summary: 'Verify certificate by verification code' })
  @ApiResponse({ status: 200, description: 'Verification result' })
  async verifyCertificate(@Param('code') code: string): Promise<{
    isValid: boolean;
    certificate?: Certificate;
    message: string;
  }> {
    return this.certificateService.verifyCertificate(code);
  }

  @Post(':id/regenerate')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.ADMIN)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Regenerate certificate PDF (admin only)' })
  @ApiResponse({ status: 200, description: 'Certificate regenerated' })
  async regenerateCertificate(
    @Param('id', ParseIntPipe) id: number,
  ): Promise<Certificate> {
    return this.certificateService.regenerateCertificate(id);
  }

  @Delete(':id')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.ADMIN)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Revoke certificate (admin only)' })
  @ApiResponse({ status: 200, description: 'Certificate revoked' })
  async revokeCertificate(
    @Param('id', ParseIntPipe) id: number,
    @Body() body: { reason: string },
  ): Promise<Certificate> {
    return this.certificateService.revokeCertificate(id, body.reason);
  }

  @Get('course/:courseId/certificates')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.ADMIN, UserRole.INSTRUCTOR)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Get all certificates for a course' })
  @ApiResponse({ status: 200, description: 'Course certificates' })
  async getCourseCertificates(
    @Param('courseId', ParseIntPipe) courseId: number,
  ): Promise<Certificate[]> {
    return this.certificateService.getCourseCertificates(courseId);
  }

  @Get('search')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.ADMIN)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Search certificates (admin only)' })
  @ApiResponse({ status: 200, description: 'Search results' })
  async searchCertificates(
    @Query('q') query: string,
  ): Promise<Certificate[]> {
    return this.certificateService.searchCertificates(query);
  }

  @Get('stats/overview')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.ADMIN)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Get certificate statistics (admin only)' })
  @ApiResponse({ status: 200, description: 'Certificate stats' })
  async getCertificateStats() {
    return this.certificateService.getCertificateStats();
  }
}
