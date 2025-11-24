import { Test, TestingModule } from '@nestjs/testing';
import { getRepositoryToken } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { QuizService } from './quiz.service';
import { Quiz, QuizDifficulty } from './entities/quiz.entity';
import { Question, QuestionType } from './entities/question.entity';
import { Answer } from './entities/answer.entity';
import { QuizAttempt, AttemptStatus } from './entities/quiz-attempt.entity';
import { QuestionResponse } from './entities/question-response.entity';
import { NotFoundException, ForbiddenException } from '@nestjs/common';

describe('QuizService', () => {
  let service: QuizService;
  let quizRepository: Repository<Quiz>;
  let questionRepository: Repository<Question>;
  let answerRepository: Repository<Answer>;
  let attemptRepository: Repository<QuizAttempt>;
  let responseRepository: Repository<QuestionResponse>;

  const mockQuizRepository = {
    create: jest.fn(),
    save: jest.fn(),
    findOne: jest.fn(),
    find: jest.fn(),
    update: jest.fn(),
    remove: jest.fn(),
  };

  const mockQuestionRepository = {
    create: jest.fn(),
    save: jest.fn(),
    find: jest.fn(),
  };

  const mockAnswerRepository = {
    create: jest.fn(),
    save: jest.fn(),
  };

  const mockAttemptRepository = {
    create: jest.fn(),
    save: jest.fn(),
    findOne: jest.fn(),
    find: jest.fn(),
  };

  const mockResponseRepository = {
    create: jest.fn(),
    save: jest.fn(),
  };

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [
        QuizService,
        {
          provide: getRepositoryToken(Quiz),
          useValue: mockQuizRepository,
        },
        {
          provide: getRepositoryToken(Question),
          useValue: mockQuestionRepository,
        },
        {
          provide: getRepositoryToken(Answer),
          useValue: mockAnswerRepository,
        },
        {
          provide: getRepositoryToken(QuizAttempt),
          useValue: mockAttemptRepository,
        },
        {
          provide: getRepositoryToken(QuestionResponse),
          useValue: mockResponseRepository,
        },
      ],
    }).compile();

    service = module.get<QuizService>(QuizService);
    quizRepository = module.get(getRepositoryToken(Quiz));
    questionRepository = module.get(getRepositoryToken(Question));
    answerRepository = module.get(getRepositoryToken(Answer));
    attemptRepository = module.get(getRepositoryToken(QuizAttempt));
    responseRepository = module.get(getRepositoryToken(QuestionResponse));
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  describe('create', () => {
    it('should create a quiz successfully', async () => {
      const createQuizDto = {
        title: 'Test Quiz',
        description: 'A test quiz',
        passingScore: 70,
        maxAttempts: 3,
        difficulty: QuizDifficulty.MEDIUM,
        questions: [
          {
            type: QuestionType.MULTIPLE_CHOICE,
            question: 'What is 2+2?',
            points: 1,
            answers: [
              { text: '3', isCorrect: false },
              { text: '4', isCorrect: true },
            ],
          },
        ],
      };

      const savedQuiz = { id: 1, ...createQuizDto };
      mockQuizRepository.create.mockReturnValue(savedQuiz);
      mockQuizRepository.save.mockResolvedValue(savedQuiz);
      mockQuizRepository.findOne.mockResolvedValue({
        ...savedQuiz,
        questions: [],
      });

      const result = await service.create(createQuizDto);

      expect(mockQuizRepository.create).toHaveBeenCalled();
      expect(mockQuizRepository.save).toHaveBeenCalled();
      expect(result).toBeDefined();
    });
  });

  describe('findById', () => {
    it('should find a quiz by id', async () => {
      const quiz = {
        id: 1,
        title: 'Test Quiz',
        questions: [{ id: 1, orderIndex: 0 }],
      };

      mockQuizRepository.findOne.mockResolvedValue(quiz);

      const result = await service.findById(1);

      expect(result).toEqual(quiz);
      expect(mockQuizRepository.findOne).toHaveBeenCalledWith({
        where: { id: 1 },
        relations: ['questions'],
      });
    });

    it('should throw NotFoundException if quiz not found', async () => {
      mockQuizRepository.findOne.mockResolvedValue(null);

      await expect(service.findById(999)).rejects.toThrow(NotFoundException);
    });
  });

  describe('startAttempt', () => {
    it('should start a new quiz attempt', async () => {
      const quiz = {
        id: 1,
        title: 'Test Quiz',
        isPublished: true,
        maxAttempts: 3,
        totalPoints: 10,
        questions: [],
      };

      const attempt = {
        id: 1,
        userId: 1,
        quizId: 1,
        attemptNumber: 1,
        status: AttemptStatus.IN_PROGRESS,
      };

      mockQuizRepository.findOne.mockResolvedValue(quiz);
      mockAttemptRepository.find.mockResolvedValue([]);
      mockAttemptRepository.create.mockReturnValue(attempt);
      mockAttemptRepository.save.mockResolvedValue(attempt);

      const result = await service.startAttempt(1, 1);

      expect(result).toEqual(attempt);
      expect(mockAttemptRepository.create).toHaveBeenCalled();
    });

    it('should throw ForbiddenException if max attempts reached', async () => {
      const quiz = {
        id: 1,
        isPublished: true,
        maxAttempts: 2,
        questions: [],
      };

      const existingAttempts = [{ id: 1 }, { id: 2 }];

      mockQuizRepository.findOne.mockResolvedValue(quiz);
      mockAttemptRepository.find.mockResolvedValue(existingAttempts);

      await expect(service.startAttempt(1, 1)).rejects.toThrow(
        ForbiddenException,
      );
    });
  });

  describe('submitQuiz', () => {
    it('should submit quiz and calculate score', async () => {
      const attempt = {
        id: 1,
        userId: 1,
        quizId: 1,
        status: AttemptStatus.IN_PROGRESS,
        startedAt: new Date(),
        totalPoints: 2,
        quiz: {
          id: 1,
          passingScore: 70,
          timeLimit: null,
          questions: [
            {
              id: 1,
              type: QuestionType.MULTIPLE_CHOICE,
              points: 1,
              answers: [
                { id: 1, text: 'A', isCorrect: false },
                { id: 2, text: 'B', isCorrect: true },
              ],
            },
            {
              id: 2,
              type: QuestionType.MULTIPLE_CHOICE,
              points: 1,
              answers: [
                { id: 3, text: 'C', isCorrect: true },
                { id: 4, text: 'D', isCorrect: false },
              ],
            },
          ],
        },
      };

      const submitDto = {
        answers: [
          { questionId: 1, selectedAnswers: [2] }, // Correct
          { questionId: 2, selectedAnswers: [3] }, // Correct
        ],
      };

      mockAttemptRepository.findOne.mockResolvedValue(attempt);
      mockResponseRepository.create.mockImplementation((data) => data);
      mockResponseRepository.save.mockImplementation((data) => data);
      mockAttemptRepository.save.mockImplementation((data) => data);
      mockAttemptRepository.find.mockResolvedValue([]);

      const result = await service.submitQuiz(1, 1, submitDto);

      expect(result.status).toBe(AttemptStatus.COMPLETED);
      expect(result.earnedPoints).toBe(2);
      expect(result.scorePercentage).toBe(100);
      expect(result.isPassed).toBe(true);
    });

    it('should handle incorrect answers', async () => {
      const attempt = {
        id: 1,
        userId: 1,
        quizId: 1,
        status: AttemptStatus.IN_PROGRESS,
        startedAt: new Date(),
        totalPoints: 1,
        quiz: {
          id: 1,
          passingScore: 70,
          timeLimit: null,
          questions: [
            {
              id: 1,
              type: QuestionType.MULTIPLE_CHOICE,
              points: 1,
              answers: [
                { id: 1, text: 'A', isCorrect: false },
                { id: 2, text: 'B', isCorrect: true },
              ],
            },
          ],
        },
      };

      const submitDto = {
        answers: [
          { questionId: 1, selectedAnswers: [1] }, // Incorrect
        ],
      };

      mockAttemptRepository.findOne.mockResolvedValue(attempt);
      mockResponseRepository.create.mockImplementation((data) => data);
      mockResponseRepository.save.mockImplementation((data) => data);
      mockAttemptRepository.save.mockImplementation((data) => data);
      mockAttemptRepository.find.mockResolvedValue([]);

      const result = await service.submitQuiz(1, 1, submitDto);

      expect(result.earnedPoints).toBe(0);
      expect(result.scorePercentage).toBe(0);
      expect(result.isPassed).toBe(false);
    });
  });

  describe('getQuizForStudent', () => {
    it('should remove correct answer information for students', async () => {
      const quiz = {
        id: 1,
        title: 'Test Quiz',
        maxAttempts: 3,
        randomizeAnswers: false,
        randomizeQuestions: false,
        questions: [
          {
            id: 1,
            type: QuestionType.MULTIPLE_CHOICE,
            question: 'Test?',
            answers: [
              { id: 1, text: 'A', isCorrect: false, orderIndex: 0 },
              { id: 2, text: 'B', isCorrect: true, orderIndex: 1 },
            ],
            orderIndex: 0,
          },
        ],
      };

      mockQuizRepository.findOne.mockResolvedValue(quiz);
      mockAttemptRepository.find.mockResolvedValue([]);

      const result = await service.getQuizForStudent(1, 1);

      expect(result.questions[0].answers[0]).not.toHaveProperty('isCorrect');
      expect(result.remainingAttempts).toBe(3);
    });

    it('should throw ForbiddenException if no attempts remaining', async () => {
      const quiz = {
        id: 1,
        maxAttempts: 1,
        questions: [],
      };

      const existingAttempts = [{ id: 1 }];

      mockQuizRepository.findOne.mockResolvedValue(quiz);
      mockAttemptRepository.find.mockResolvedValue(existingAttempts);

      await expect(service.getQuizForStudent(1, 1)).rejects.toThrow(
        ForbiddenException,
      );
    });
  });

  describe('getUserQuizHistory', () => {
    it('should return user quiz attempt history', async () => {
      const attempts = [
        { id: 1, attemptNumber: 1, scorePercentage: 80 },
        { id: 2, attemptNumber: 2, scorePercentage: 90 },
      ];

      mockAttemptRepository.find.mockResolvedValue(attempts);

      const result = await service.getUserQuizHistory(1, 1);

      expect(result).toEqual(attempts);
      expect(mockAttemptRepository.find).toHaveBeenCalledWith({
        where: { userId: 1, quizId: 1 },
        order: { createdAt: 'DESC' },
      });
    });
  });
});
