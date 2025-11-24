import {
  Injectable,
  NotFoundException,
  BadRequestException,
  ForbiddenException,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Quiz } from './entities/quiz.entity';
import { Question, QuestionType } from './entities/question.entity';
import { Answer } from './entities/answer.entity';
import { QuizAttempt, AttemptStatus } from './entities/quiz-attempt.entity';
import { QuestionResponse } from './entities/question-response.entity';
import { CreateQuizDto } from './dto/create-quiz.dto';
import { UpdateQuizDto } from './dto/update-quiz.dto';
import { SubmitQuizDto } from './dto/submit-quiz.dto';

/**
 * Quiz service handling quiz creation, attempts, and grading
 */
@Injectable()
export class QuizService {
  constructor(
    @InjectRepository(Quiz)
    private quizRepository: Repository<Quiz>,
    @InjectRepository(Question)
    private questionRepository: Repository<Question>,
    @InjectRepository(Answer)
    private answerRepository: Repository<Answer>,
    @InjectRepository(QuizAttempt)
    private attemptRepository: Repository<QuizAttempt>,
    @InjectRepository(QuestionResponse)
    private responseRepository: Repository<QuestionResponse>,
  ) {}

  /**
   * Create a new quiz with questions and answers
   */
  async create(createQuizDto: CreateQuizDto): Promise<Quiz> {
    const quiz = this.quizRepository.create({
      title: createQuizDto.title,
      description: createQuizDto.description,
      lessonId: createQuizDto.lessonId,
      timeLimit: createQuizDto.timeLimit,
      passingScore: createQuizDto.passingScore,
      maxAttempts: createQuizDto.maxAttempts,
      randomizeQuestions: createQuizDto.randomizeQuestions,
      randomizeAnswers: createQuizDto.randomizeAnswers,
      showCorrectAnswers: createQuizDto.showCorrectAnswers,
      showResultsImmediately: createQuizDto.showResultsImmediately,
      difficulty: createQuizDto.difficulty,
    });

    const savedQuiz = await this.quizRepository.save(quiz);

    // Add questions if provided
    if (createQuizDto.questions && createQuizDto.questions.length > 0) {
      await this.addQuestions(savedQuiz.id, createQuizDto.questions);
    }

    return this.findById(savedQuiz.id);
  }

  /**
   * Add questions to a quiz
   */
  async addQuestions(quizId: number, questionsData: any[]): Promise<void> {
    const quiz = await this.quizRepository.findOne({ where: { id: quizId } });
    if (!quiz) {
      throw new NotFoundException('Quiz not found');
    }

    let totalPoints = 0;

    for (const [index, questionData] of questionsData.entries()) {
      const question = this.questionRepository.create({
        quizId,
        type: questionData.type,
        question: questionData.question,
        explanation: questionData.explanation,
        points: questionData.points || 1,
        orderIndex: index,
        imageUrl: questionData.imageUrl,
        videoUrl: questionData.videoUrl,
        caseSensitive: questionData.caseSensitive,
        matchingPairs: questionData.matchingPairs,
      });

      const savedQuestion = await this.questionRepository.save(question);
      totalPoints += question.points;

      // Add answers
      if (questionData.answers && questionData.answers.length > 0) {
        for (const [answerIndex, answerData] of questionData.answers.entries()) {
          const answer = this.answerRepository.create({
            questionId: savedQuestion.id,
            text: answerData.text,
            isCorrect: answerData.isCorrect,
            orderIndex: answerIndex,
            imageUrl: answerData.imageUrl,
            partialCredit: answerData.partialCredit || 0,
          });

          await this.answerRepository.save(answer);
        }
      }
    }

    // Update quiz totals
    quiz.totalQuestions = questionsData.length;
    quiz.totalPoints = totalPoints;
    await this.quizRepository.save(quiz);
  }

  /**
   * Find quiz by ID with questions and answers
   */
  async findById(id: number, includeAnswers = false): Promise<Quiz> {
    const relations = ['questions'];
    if (includeAnswers) {
      relations.push('questions.answers');
    }

    const quiz = await this.quizRepository.findOne({
      where: { id },
      relations,
    });

    if (!quiz) {
      throw new NotFoundException('Quiz not found');
    }

    // Sort questions by order
    if (quiz.questions) {
      quiz.questions.sort((a, b) => a.orderIndex - b.orderIndex);

      if (includeAnswers) {
        quiz.questions.forEach((question) => {
          if (question.answers) {
            question.answers.sort((a, b) => a.orderIndex - b.orderIndex);
          }
        });
      }
    }

    return quiz;
  }

  /**
   * Get quiz for student (without correct answers)
   */
  async getQuizForStudent(id: number, userId: number): Promise<any> {
    const quiz = await this.findById(id, true);

    // Check remaining attempts
    const attempts = await this.attemptRepository.find({
      where: { quizId: id, userId },
    });

    const remainingAttempts = quiz.maxAttempts - attempts.length;

    if (remainingAttempts <= 0) {
      throw new ForbiddenException('Maximum attempts reached');
    }

    // Remove correct answer information
    const questionsForStudent = quiz.questions.map((question) => {
      const questionCopy: any = { ...question };

      if (question.answers) {
        questionCopy.answers = question.answers.map((answer) => ({
          id: answer.id,
          text: answer.text,
          imageUrl: answer.imageUrl,
          orderIndex: answer.orderIndex,
        }));

        // Randomize answers if enabled
        if (quiz.randomizeAnswers) {
          questionCopy.answers = this.shuffleArray(questionCopy.answers);
        }
      }

      return questionCopy;
    });

    // Randomize questions if enabled
    const finalQuestions = quiz.randomizeQuestions
      ? this.shuffleArray(questionsForStudent)
      : questionsForStudent;

    return {
      ...quiz,
      questions: finalQuestions,
      remainingAttempts,
      totalAttempts: attempts.length,
    };
  }

  /**
   * Start a new quiz attempt
   */
  async startAttempt(
    quizId: number,
    userId: number,
    ipAddress?: string,
    userAgent?: string,
  ): Promise<QuizAttempt> {
    const quiz = await this.findById(quizId);

    // Check if quiz is published
    if (!quiz.isPublished) {
      throw new BadRequestException('Quiz is not available');
    }

    // Check remaining attempts
    const existingAttempts = await this.attemptRepository.find({
      where: { quizId, userId },
    });

    if (existingAttempts.length >= quiz.maxAttempts) {
      throw new ForbiddenException('Maximum attempts reached');
    }

    // Check for incomplete attempts
    const incompleteAttempt = existingAttempts.find(
      (attempt) => attempt.status === AttemptStatus.IN_PROGRESS,
    );

    if (incompleteAttempt) {
      return incompleteAttempt;
    }

    // Create new attempt
    const attempt = this.attemptRepository.create({
      userId,
      quizId,
      attemptNumber: existingAttempts.length + 1,
      status: AttemptStatus.IN_PROGRESS,
      startedAt: new Date(),
      totalPoints: quiz.totalPoints,
      randomizationSeed: quiz.randomizeQuestions ? Math.random() : undefined,
      ipAddress,
      userAgent,
    });

    return this.attemptRepository.save(attempt);
  }

  /**
   * Submit quiz answers and calculate score
   */
  async submitQuiz(
    attemptId: number,
    userId: number,
    submitDto: SubmitQuizDto,
  ): Promise<QuizAttempt> {
    const attempt = await this.attemptRepository.findOne({
      where: { id: attemptId, userId },
      relations: ['quiz', 'quiz.questions', 'quiz.questions.answers'],
    });

    if (!attempt) {
      throw new NotFoundException('Quiz attempt not found');
    }

    if (attempt.status !== AttemptStatus.IN_PROGRESS) {
      throw new BadRequestException('Quiz attempt is not in progress');
    }

    const submittedAt = new Date();
    const timeSpent = Math.floor(
      (submittedAt.getTime() - attempt.startedAt.getTime()) / 1000,
    );

    // Check time limit
    if (attempt.quiz.timeLimit && timeSpent > attempt.quiz.timeLimit) {
      attempt.status = AttemptStatus.TIME_EXPIRED;
      await this.attemptRepository.save(attempt);
      throw new BadRequestException('Time limit exceeded');
    }

    let totalEarnedPoints = 0;
    const responses: QuestionResponse[] = [];

    // Grade each question
    for (const question of attempt.quiz.questions) {
      const userAnswer = submitDto.answers.find(
        (a) => a.questionId === question.id,
      );

      const response = await this.gradeQuestion(
        attempt.id,
        question,
        userAnswer,
      );

      totalEarnedPoints += response.pointsEarned;
      responses.push(response);
    }

    // Calculate final score
    const scorePercentage = (totalEarnedPoints / attempt.totalPoints) * 100;
    const isPassed = scorePercentage >= attempt.quiz.passingScore;

    // Update attempt
    attempt.status = AttemptStatus.COMPLETED;
    attempt.submittedAt = submittedAt;
    attempt.timeSpent = timeSpent;
    attempt.earnedPoints = totalEarnedPoints;
    attempt.scorePercentage = scorePercentage;
    attempt.isPassed = isPassed;
    attempt.responses = responses;

    await this.attemptRepository.save(attempt);

    // Update quiz statistics
    await this.updateQuizStatistics(attempt.quiz.id);

    return attempt;
  }

  /**
   * Grade a single question
   */
  private async gradeQuestion(
    attemptId: number,
    question: Question,
    userAnswer?: any,
  ): Promise<QuestionResponse> {
    const response = this.responseRepository.create({
      attemptId,
      questionId: question.id,
      pointsPossible: question.points,
    });

    if (!userAnswer) {
      // No answer provided
      response.isCorrect = false;
      response.pointsEarned = 0;
      return this.responseRepository.save(response);
    }

    switch (question.type) {
      case QuestionType.MULTIPLE_CHOICE:
      case QuestionType.TRUE_FALSE:
        response.selectedAnswers = userAnswer.selectedAnswers;
        response.isCorrect = this.gradeMultipleChoice(
          question,
          userAnswer.selectedAnswers,
        );
        response.pointsEarned = response.isCorrect ? question.points : 0;
        break;

      case QuestionType.MULTIPLE_SELECT:
        response.selectedAnswers = userAnswer.selectedAnswers;
        const multiSelectResult = this.gradeMultipleSelect(
          question,
          userAnswer.selectedAnswers,
        );
        response.isCorrect = multiSelectResult.isFullyCorrect;
        response.pointsEarned = multiSelectResult.points;
        break;

      case QuestionType.SHORT_ANSWER:
        response.textAnswer = userAnswer.textAnswer;
        response.isCorrect = this.gradeShortAnswer(
          question,
          userAnswer.textAnswer,
        );
        response.pointsEarned = response.isCorrect ? question.points : 0;
        break;

      case QuestionType.ESSAY:
        response.textAnswer = userAnswer.textAnswer;
        response.requiresManualGrading = true;
        response.isCorrect = null;
        response.pointsEarned = 0;
        break;

      case QuestionType.FILL_BLANK:
        response.textAnswer = userAnswer.textAnswer;
        response.isCorrect = this.gradeFillBlank(
          question,
          userAnswer.textAnswer,
        );
        response.pointsEarned = response.isCorrect ? question.points : 0;
        break;

      case QuestionType.MATCHING:
        response.matchingPairs = userAnswer.matchingPairs;
        const matchingResult = this.gradeMatching(
          question,
          userAnswer.matchingPairs,
        );
        response.isCorrect = matchingResult.isFullyCorrect;
        response.pointsEarned = matchingResult.points;
        break;
    }

    return this.responseRepository.save(response);
  }

  /**
   * Grade multiple choice question (single correct answer)
   */
  private gradeMultipleChoice(
    question: Question,
    selectedAnswers: number[],
  ): boolean {
    if (!selectedAnswers || selectedAnswers.length !== 1) {
      return false;
    }

    const correctAnswer = question.answers.find((a) => a.isCorrect);
    return correctAnswer?.id === selectedAnswers[0];
  }

  /**
   * Grade multiple select question (multiple correct answers)
   */
  private gradeMultipleSelect(
    question: Question,
    selectedAnswers: number[],
  ): { isFullyCorrect: boolean; points: number } {
    const correctAnswerIds = question.answers
      .filter((a) => a.isCorrect)
      .map((a) => a.id);

    if (!selectedAnswers || selectedAnswers.length === 0) {
      return { isFullyCorrect: false, points: 0 };
    }

    const selectedSet = new Set(selectedAnswers);
    const correctSet = new Set(correctAnswerIds);

    // Check if fully correct
    const isFullyCorrect =
      selectedSet.size === correctSet.size &&
      [...selectedSet].every((id) => correctSet.has(id));

    if (isFullyCorrect) {
      return { isFullyCorrect: true, points: question.points };
    }

    // Calculate partial credit
    let correctCount = 0;
    let incorrectCount = 0;

    selectedAnswers.forEach((answerId) => {
      if (correctSet.has(answerId)) {
        correctCount++;
      } else {
        incorrectCount++;
      }
    });

    // Partial credit: correct selections minus incorrect selections
    const partialPoints = Math.max(
      0,
      ((correctCount - incorrectCount) / correctSet.size) * question.points,
    );

    return { isFullyCorrect: false, points: partialPoints };
  }

  /**
   * Grade short answer question
   */
  private gradeShortAnswer(question: Question, textAnswer: string): boolean {
    if (!textAnswer) {
      return false;
    }

    const correctAnswers = question.answers
      .filter((a) => a.isCorrect)
      .map((a) => a.text);

    const userAnswer = question.caseSensitive
      ? textAnswer.trim()
      : textAnswer.trim().toLowerCase();

    return correctAnswers.some((correct) => {
      const compareAnswer = question.caseSensitive
        ? correct.trim()
        : correct.trim().toLowerCase();
      return userAnswer === compareAnswer;
    });
  }

  /**
   * Grade fill in the blank question
   */
  private gradeFillBlank(question: Question, textAnswer: string): boolean {
    // Similar to short answer
    return this.gradeShortAnswer(question, textAnswer);
  }

  /**
   * Grade matching question
   */
  private gradeMatching(
    question: Question,
    matchingPairs: Array<{ leftId: string; rightId: string }>,
  ): { isFullyCorrect: boolean; points: number } {
    if (!question.matchingPairs || !matchingPairs) {
      return { isFullyCorrect: false, points: 0 };
    }

    let correctCount = 0;

    matchingPairs.forEach((userPair) => {
      const correctPair = question.matchingPairs!.find(
        (cp) => cp.left === userPair.leftId,
      );
      if (correctPair && correctPair.right === userPair.rightId) {
        correctCount++;
      }
    });

    const totalPairs = question.matchingPairs.length;
    const isFullyCorrect = correctCount === totalPairs;
    const points = (correctCount / totalPairs) * question.points;

    return { isFullyCorrect, points };
  }

  /**
   * Get attempt results
   */
  async getAttemptResults(
    attemptId: number,
    userId: number,
  ): Promise<QuizAttempt> {
    const attempt = await this.attemptRepository.findOne({
      where: { id: attemptId, userId },
      relations: [
        'quiz',
        'responses',
        'responses.question',
        'responses.question.answers',
      ],
    });

    if (!attempt) {
      throw new NotFoundException('Quiz attempt not found');
    }

    if (
      attempt.status === AttemptStatus.IN_PROGRESS ||
      !attempt.quiz.showResultsImmediately
    ) {
      throw new ForbiddenException('Results not available yet');
    }

    return attempt;
  }

  /**
   * Update quiz statistics
   */
  private async updateQuizStatistics(quizId: number): Promise<void> {
    const attempts = await this.attemptRepository.find({
      where: { quizId, status: AttemptStatus.COMPLETED },
    });

    const totalAttempts = attempts.length;
    const averageScore =
      totalAttempts > 0
        ? attempts.reduce((sum, a) => sum + a.scorePercentage, 0) /
          totalAttempts
        : 0;

    await this.quizRepository.update(quizId, {
      attemptCount: totalAttempts,
      averageScore,
    });
  }

  /**
   * Get user's quiz history
   */
  async getUserQuizHistory(userId: number, quizId: number): Promise<QuizAttempt[]> {
    return this.attemptRepository.find({
      where: { userId, quizId },
      order: { createdAt: 'DESC' },
    });
  }

  /**
   * Shuffle array (for randomization)
   */
  private shuffleArray<T>(array: T[]): T[] {
    const shuffled = [...array];
    for (let i = shuffled.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]];
    }
    return shuffled;
  }

  /**
   * Update quiz
   */
  async update(id: number, updateQuizDto: UpdateQuizDto): Promise<Quiz> {
    const quiz = await this.findById(id);
    Object.assign(quiz, updateQuizDto);
    await this.quizRepository.save(quiz);
    return this.findById(id);
  }

  /**
   * Delete quiz
   */
  async remove(id: number): Promise<void> {
    const quiz = await this.findById(id);
    await this.quizRepository.remove(quiz);
  }
}
