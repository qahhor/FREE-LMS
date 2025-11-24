import {
  Controller,
  Get,
  Post,
  Body,
  Param,
  Delete,
  Put,
  UseGuards,
  ParseIntPipe,
  Request,
} from '@nestjs/common';
import {
  ApiTags,
  ApiOperation,
  ApiResponse,
  ApiBearerAuth,
} from '@nestjs/swagger';
import { QuizService } from './quiz.service';
import { Quiz } from './entities/quiz.entity';
import { QuizAttempt } from './entities/quiz-attempt.entity';
import { CreateQuizDto } from './dto/create-quiz.dto';
import { UpdateQuizDto } from './dto/update-quiz.dto';
import { SubmitQuizDto } from './dto/submit-quiz.dto';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { Roles } from '../../common/decorators/roles.decorator';
import { RolesGuard } from '../../common/guards/roles.guard';
import { UserRole } from '../../common/enums/user-role.enum';
import { CurrentUser } from '../../common/decorators/current-user.decorator';
import { User } from '../users/entities/user.entity';

/**
 * Quiz and assessment endpoints
 */
@ApiTags('quizzes')
@Controller('quizzes')
@UseGuards(JwtAuthGuard)
@ApiBearerAuth()
export class QuizController {
  constructor(private readonly quizService: QuizService) {}

  @Post()
  @UseGuards(RolesGuard)
  @Roles(UserRole.ADMIN, UserRole.INSTRUCTOR)
  @ApiOperation({ summary: 'Create a new quiz' })
  @ApiResponse({ status: 201, description: 'Quiz created' })
  async create(@Body() createQuizDto: CreateQuizDto): Promise<Quiz> {
    return this.quizService.create(createQuizDto);
  }

  @Get(':id')
  @ApiOperation({ summary: 'Get quiz details' })
  @ApiResponse({ status: 200, description: 'Quiz data' })
  @ApiResponse({ status: 404, description: 'Quiz not found' })
  async findOne(
    @Param('id', ParseIntPipe) id: number,
    @CurrentUser() user: User,
  ): Promise<any> {
    // Check if user is instructor/admin or student
    if (user.role === UserRole.ADMIN || user.role === UserRole.INSTRUCTOR) {
      return this.quizService.findById(id, true);
    } else {
      return this.quizService.getQuizForStudent(id, user.id);
    }
  }

  @Put(':id')
  @UseGuards(RolesGuard)
  @Roles(UserRole.ADMIN, UserRole.INSTRUCTOR)
  @ApiOperation({ summary: 'Update quiz' })
  @ApiResponse({ status: 200, description: 'Quiz updated' })
  async update(
    @Param('id', ParseIntPipe) id: number,
    @Body() updateQuizDto: UpdateQuizDto,
  ): Promise<Quiz> {
    return this.quizService.update(id, updateQuizDto);
  }

  @Delete(':id')
  @UseGuards(RolesGuard)
  @Roles(UserRole.ADMIN, UserRole.INSTRUCTOR)
  @ApiOperation({ summary: 'Delete quiz' })
  @ApiResponse({ status: 200, description: 'Quiz deleted' })
  async remove(@Param('id', ParseIntPipe) id: number): Promise<void> {
    return this.quizService.remove(id);
  }

  @Post(':id/start')
  @ApiOperation({ summary: 'Start a new quiz attempt' })
  @ApiResponse({ status: 201, description: 'Quiz attempt started' })
  @ApiResponse({ status: 403, description: 'Maximum attempts reached' })
  async startAttempt(
    @Param('id', ParseIntPipe) id: number,
    @CurrentUser() user: User,
    @Request() req: any,
  ): Promise<QuizAttempt> {
    const ipAddress = req.ip;
    const userAgent = req.headers['user-agent'];
    return this.quizService.startAttempt(id, user.id, ipAddress, userAgent);
  }

  @Post('attempts/:attemptId/submit')
  @ApiOperation({ summary: 'Submit quiz answers' })
  @ApiResponse({ status: 200, description: 'Quiz submitted and graded' })
  @ApiResponse({ status: 400, description: 'Invalid submission' })
  async submitQuiz(
    @Param('attemptId', ParseIntPipe) attemptId: number,
    @CurrentUser() user: User,
    @Body() submitQuizDto: SubmitQuizDto,
  ): Promise<QuizAttempt> {
    return this.quizService.submitQuiz(attemptId, user.id, submitQuizDto);
  }

  @Get('attempts/:attemptId/results')
  @ApiOperation({ summary: 'Get quiz attempt results' })
  @ApiResponse({ status: 200, description: 'Attempt results' })
  @ApiResponse({ status: 404, description: 'Attempt not found' })
  async getResults(
    @Param('attemptId', ParseIntPipe) attemptId: number,
    @CurrentUser() user: User,
  ): Promise<QuizAttempt> {
    return this.quizService.getAttemptResults(attemptId, user.id);
  }

  @Get(':id/history')
  @ApiOperation({ summary: 'Get user quiz attempt history' })
  @ApiResponse({ status: 200, description: 'Attempt history' })
  async getHistory(
    @Param('id', ParseIntPipe) id: number,
    @CurrentUser() user: User,
  ): Promise<QuizAttempt[]> {
    return this.quizService.getUserQuizHistory(user.id, id);
  }
}
