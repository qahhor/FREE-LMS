import {
  Component,
  Input,
  Output,
  EventEmitter,
  OnInit,
  OnDestroy,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '@environments/environment';
import { interval, Subscription } from 'rxjs';

interface Quiz {
  id: number;
  title: string;
  description?: string;
  timeLimit?: number;
  passingScore: number;
  maxAttempts: number;
  totalQuestions: number;
  totalPoints: number;
  remainingAttempts: number;
  questions: Question[];
}

interface Question {
  id: number;
  type: QuestionType;
  question: string;
  explanation?: string;
  points: number;
  imageUrl?: string;
  videoUrl?: string;
  answers?: Answer[];
  matchingPairs?: Array<{ left: string; right: string }>;
}

interface Answer {
  id: number;
  text: string;
  imageUrl?: string;
}

type QuestionType =
  | 'multiple_choice'
  | 'multiple_select'
  | 'true_false'
  | 'short_answer'
  | 'essay'
  | 'fill_blank'
  | 'matching';

interface QuizAttempt {
  id: number;
  attemptNumber: number;
  status: string;
  startedAt: string;
  submittedAt?: string;
  scorePercentage: number;
  isPassed: boolean;
  earnedPoints: number;
  totalPoints: number;
}

interface UserAnswer {
  questionId: number;
  selectedAnswers?: number[];
  textAnswer?: string;
  matchingPairs?: Array<{ leftId: string; rightId: string }>;
}

/**
 * Interactive quiz taking component
 * Supports multiple question types, timer, and progress tracking
 */
@Component({
  selector: 'app-quiz-taking',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="quiz-container" *ngIf="quiz">
      <!-- Quiz Header -->
      <div class="quiz-header">
        <h1>{{ quiz.title }}</h1>
        <p *ngIf="quiz.description">{{ quiz.description }}</p>

        <div class="quiz-info">
          <span class="info-item">
            📊 {{ quiz.totalQuestions }} Questions
          </span>
          <span class="info-item">
            🎯 Passing: {{ quiz.passingScore }}%
          </span>
          <span class="info-item" *ngIf="quiz.timeLimit">
            ⏱️ Time: {{ formatTime(quiz.timeLimit) }}
          </span>
          <span class="info-item">
            🔄 Attempts: {{ quiz.remainingAttempts }} left
          </span>
        </div>
      </div>

      <!-- Quiz Not Started -->
      <div *ngIf="!attempt && !showResults" class="quiz-start">
        <div class="start-card">
          <h2>Ready to start?</h2>
          <p>This quiz contains {{ quiz.totalQuestions }} questions worth {{ quiz.totalPoints }} points.</p>
          <p *ngIf="quiz.timeLimit">You have {{ formatTime(quiz.timeLimit) }} to complete it.</p>
          <p>You need {{ quiz.passingScore }}% to pass.</p>
          <button class="btn-primary" (click)="startQuiz()" [disabled]="loading">
            {{ loading ? 'Starting...' : 'Start Quiz' }}
          </button>
        </div>
      </div>

      <!-- Quiz In Progress -->
      <div *ngIf="attempt && !showResults" class="quiz-content">
        <!-- Timer -->
        <div class="quiz-timer" *ngIf="quiz.timeLimit">
          <div class="timer" [class.warning]="timeRemaining < 60">
            ⏱️ {{ formatTime(timeRemaining) }}
          </div>
          <div class="progress-bar">
            <div
              class="progress-fill"
              [style.width.%]="(timeRemaining / quiz.timeLimit) * 100"
            ></div>
          </div>
        </div>

        <!-- Question Navigation -->
        <div class="question-nav">
          <button
            *ngFor="let q of quiz.questions; let i = index"
            class="nav-btn"
            [class.active]="currentQuestionIndex === i"
            [class.answered]="isQuestionAnswered(q.id)"
            (click)="goToQuestion(i)"
          >
            {{ i + 1 }}
          </button>
        </div>

        <!-- Current Question -->
        <div class="question-container" *ngIf="currentQuestion">
          <div class="question-header">
            <span class="question-number">
              Question {{ currentQuestionIndex + 1 }} of {{ quiz.questions.length }}
            </span>
            <span class="question-points">{{ currentQuestion.points }} points</span>
          </div>

          <div class="question-content">
            <h3 [innerHTML]="currentQuestion.question"></h3>

            <img
              *ngIf="currentQuestion.imageUrl"
              [src]="currentQuestion.imageUrl"
              alt="Question image"
              class="question-image"
            />

            <!-- Multiple Choice -->
            <div
              *ngIf="currentQuestion.type === 'multiple_choice' || currentQuestion.type === 'true_false'"
              class="answer-options"
            >
              <div
                *ngFor="let answer of currentQuestion.answers"
                class="answer-option"
                [class.selected]="isAnswerSelected(currentQuestion.id, answer.id)"
                (click)="selectSingleAnswer(currentQuestion.id, answer.id)"
              >
                <input
                  type="radio"
                  [name]="'q' + currentQuestion.id"
                  [value]="answer.id"
                  [checked]="isAnswerSelected(currentQuestion.id, answer.id)"
                  (change)="selectSingleAnswer(currentQuestion.id, answer.id)"
                />
                <label>
                  <span class="answer-text">{{ answer.text }}</span>
                  <img
                    *ngIf="answer.imageUrl"
                    [src]="answer.imageUrl"
                    alt="Answer image"
                    class="answer-image"
                  />
                </label>
              </div>
            </div>

            <!-- Multiple Select -->
            <div
              *ngIf="currentQuestion.type === 'multiple_select'"
              class="answer-options"
            >
              <div
                *ngFor="let answer of currentQuestion.answers"
                class="answer-option"
                [class.selected]="isAnswerSelected(currentQuestion.id, answer.id)"
              >
                <input
                  type="checkbox"
                  [value]="answer.id"
                  [checked]="isAnswerSelected(currentQuestion.id, answer.id)"
                  (change)="toggleAnswer(currentQuestion.id, answer.id)"
                />
                <label>{{ answer.text }}</label>
              </div>
              <p class="hint">Select all that apply</p>
            </div>

            <!-- Short Answer / Fill Blank -->
            <div
              *ngIf="
                currentQuestion.type === 'short_answer' ||
                currentQuestion.type === 'fill_blank'
              "
            >
              <input
                type="text"
                class="text-input"
                [value]="getTextAnswer(currentQuestion.id)"
                (input)="setTextAnswer(currentQuestion.id, $event)"
                placeholder="Type your answer here..."
              />
            </div>

            <!-- Essay -->
            <div *ngIf="currentQuestion.type === 'essay'">
              <textarea
                class="essay-input"
                rows="10"
                [value]="getTextAnswer(currentQuestion.id)"
                (input)="setTextAnswer(currentQuestion.id, $event)"
                placeholder="Type your essay here..."
              ></textarea>
            </div>

            <!-- Matching -->
            <div *ngIf="currentQuestion.type === 'matching'" class="matching-container">
              <div
                *ngFor="let pair of currentQuestion.matchingPairs; let i = index"
                class="matching-row"
              >
                <div class="left-item">{{ pair.left }}</div>
                <select
                  class="matching-select"
                  [value]="getMatchingPair(currentQuestion.id, pair.left)"
                  (change)="setMatchingPair(currentQuestion.id, pair.left, $event)"
                >
                  <option value="">-- Select --</option>
                  <option
                    *ngFor="let p of currentQuestion.matchingPairs"
                    [value]="p.right"
                  >
                    {{ p.right }}
                  </option>
                </select>
              </div>
            </div>
          </div>

          <!-- Navigation Buttons -->
          <div class="question-actions">
            <button
              class="btn-secondary"
              (click)="previousQuestion()"
              [disabled]="currentQuestionIndex === 0"
            >
              ← Previous
            </button>

            <button
              *ngIf="currentQuestionIndex < quiz.questions.length - 1"
              class="btn-primary"
              (click)="nextQuestion()"
            >
              Next →
            </button>

            <button
              *ngIf="currentQuestionIndex === quiz.questions.length - 1"
              class="btn-success"
              (click)="confirmSubmit()"
            >
              Submit Quiz
            </button>
          </div>
        </div>
      </div>

      <!-- Submit Confirmation -->
      <div *ngIf="showSubmitConfirm" class="modal-overlay" (click)="showSubmitConfirm = false">
        <div class="modal-content" (click)="$event.stopPropagation()">
          <h2>Submit Quiz?</h2>
          <p>
            You have answered {{ answeredCount }} out of {{ quiz.totalQuestions }}
            questions.
          </p>
          <p *ngIf="answeredCount < quiz.totalQuestions" class="warning">
            ⚠️ You have {{ quiz.totalQuestions - answeredCount }} unanswered questions!
          </p>
          <div class="modal-actions">
            <button class="btn-secondary" (click)="showSubmitConfirm = false">
              Cancel
            </button>
            <button class="btn-primary" (click)="submitQuiz()" [disabled]="loading">
              {{ loading ? 'Submitting...' : 'Confirm Submit' }}
            </button>
          </div>
        </div>
      </div>

      <!-- Results -->
      <div *ngIf="showResults && results" class="quiz-results">
        <div class="results-card" [class.passed]="results.isPassed">
          <div class="results-icon">
            {{ results.isPassed ? '🎉' : '📚' }}
          </div>

          <h2>
            {{ results.isPassed ? 'Congratulations!' : 'Keep Learning!' }}
          </h2>

          <div class="score-display">
            <div class="score-circle">
              <svg viewBox="0 0 100 100">
                <circle cx="50" cy="50" r="45" class="score-bg" />
                <circle
                  cx="50"
                  cy="50"
                  r="45"
                  class="score-fill"
                  [style.stroke-dashoffset]="
                    283 - (283 * results.scorePercentage) / 100
                  "
                />
              </svg>
              <div class="score-text">{{ results.scorePercentage.toFixed(1) }}%</div>
            </div>
          </div>

          <div class="results-details">
            <div class="detail-item">
              <span class="label">Score:</span>
              <span class="value">
                {{ results.earnedPoints }} / {{ results.totalPoints }} points
              </span>
            </div>
            <div class="detail-item">
              <span class="label">Passing Score:</span>
              <span class="value">{{ quiz.passingScore }}%</span>
            </div>
            <div class="detail-item">
              <span class="label">Status:</span>
              <span class="value" [class.passed]="results.isPassed">
                {{ results.isPassed ? 'PASSED ✓' : 'FAILED ✗' }}
              </span>
            </div>
          </div>

          <div class="results-actions">
            <button class="btn-secondary" (click)="reviewAnswers()">
              📋 Review Answers
            </button>
            <button
              *ngIf="!results.isPassed && quiz.remainingAttempts > 0"
              class="btn-primary"
              (click)="retryQuiz()"
            >
              🔄 Try Again
            </button>
            <button class="btn-primary" (click)="closeQuiz()">
              ✓ Done
            </button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .quiz-container {
      max-width: 1000px;
      margin: 0 auto;
      padding: 20px;
    }

    .quiz-header {
      text-align: center;
      margin-bottom: 30px;
    }

    .quiz-header h1 {
      font-size: 32px;
      margin-bottom: 10px;
    }

    .quiz-info {
      display: flex;
      justify-content: center;
      gap: 20px;
      flex-wrap: wrap;
      margin-top: 15px;
    }

    .info-item {
      background: #f5f5f5;
      padding: 8px 16px;
      border-radius: 20px;
      font-size: 14px;
    }

    .quiz-start {
      display: flex;
      justify-content: center;
      padding: 50px 0;
    }

    .start-card {
      background: white;
      border-radius: 12px;
      padding: 40px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      text-align: center;
      max-width: 500px;
    }

    .start-card h2 {
      margin-bottom: 20px;
    }

    .start-card p {
      margin-bottom: 10px;
      color: #666;
    }

    .quiz-timer {
      background: white;
      border-radius: 8px;
      padding: 15px;
      margin-bottom: 20px;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    }

    .timer {
      font-size: 24px;
      font-weight: bold;
      text-align: center;
      margin-bottom: 10px;
    }

    .timer.warning {
      color: #d32f2f;
      animation: pulse 1s infinite;
    }

    @keyframes pulse {
      0%, 100% { opacity: 1; }
      50% { opacity: 0.5; }
    }

    .progress-bar {
      height: 6px;
      background: #e0e0e0;
      border-radius: 3px;
      overflow: hidden;
    }

    .progress-fill {
      height: 100%;
      background: #2196F3;
      transition: width 1s linear;
    }

    .question-nav {
      display: flex;
      gap: 10px;
      flex-wrap: wrap;
      margin-bottom: 20px;
      justify-content: center;
    }

    .nav-btn {
      width: 40px;
      height: 40px;
      border: 2px solid #ddd;
      background: white;
      border-radius: 50%;
      cursor: pointer;
      font-weight: bold;
      transition: all 0.2s;
    }

    .nav-btn:hover {
      border-color: #2196F3;
    }

    .nav-btn.active {
      background: #2196F3;
      color: white;
      border-color: #2196F3;
    }

    .nav-btn.answered {
      background: #4CAF50;
      color: white;
      border-color: #4CAF50;
    }

    .question-container {
      background: white;
      border-radius: 12px;
      padding: 30px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }

    .question-header {
      display: flex;
      justify-content: space-between;
      margin-bottom: 20px;
      padding-bottom: 15px;
      border-bottom: 2px solid #f5f5f5;
    }

    .question-number {
      font-weight: bold;
      color: #2196F3;
    }

    .question-points {
      color: #666;
    }

    .question-content h3 {
      font-size: 20px;
      margin-bottom: 20px;
      line-height: 1.5;
    }

    .question-image {
      max-width: 100%;
      height: auto;
      border-radius: 8px;
      margin: 20px 0;
    }

    .answer-options {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }

    .answer-option {
      border: 2px solid #e0e0e0;
      border-radius: 8px;
      padding: 15px;
      cursor: pointer;
      transition: all 0.2s;
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .answer-option:hover {
      border-color: #2196F3;
      background: #f5f9ff;
    }

    .answer-option.selected {
      border-color: #2196F3;
      background: #e3f2fd;
    }

    .answer-option label {
      flex: 1;
      cursor: pointer;
    }

    .answer-image {
      max-width: 200px;
      height: auto;
      border-radius: 4px;
      margin-top: 10px;
    }

    .hint {
      color: #666;
      font-size: 14px;
      font-style: italic;
      margin-top: 10px;
    }

    .text-input,
    .essay-input {
      width: 100%;
      padding: 12px;
      border: 2px solid #e0e0e0;
      border-radius: 8px;
      font-size: 16px;
      font-family: inherit;
      transition: border-color 0.2s;
    }

    .text-input:focus,
    .essay-input:focus {
      outline: none;
      border-color: #2196F3;
    }

    .matching-container {
      display: flex;
      flex-direction: column;
      gap: 15px;
    }

    .matching-row {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 15px;
      align-items: center;
    }

    .left-item {
      padding: 12px;
      background: #f5f5f5;
      border-radius: 8px;
      font-weight: 500;
    }

    .matching-select {
      padding: 12px;
      border: 2px solid #e0e0e0;
      border-radius: 8px;
      font-size: 16px;
      cursor: pointer;
    }

    .question-actions {
      display: flex;
      justify-content: space-between;
      gap: 15px;
      margin-top: 30px;
    }

    .btn-primary,
    .btn-secondary,
    .btn-success {
      padding: 12px 24px;
      border: none;
      border-radius: 8px;
      font-size: 16px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.2s;
    }

    .btn-primary {
      background: #2196F3;
      color: white;
    }

    .btn-primary:hover:not(:disabled) {
      background: #1976D2;
    }

    .btn-secondary {
      background: #f5f5f5;
      color: #333;
    }

    .btn-secondary:hover:not(:disabled) {
      background: #e0e0e0;
    }

    .btn-success {
      background: #4CAF50;
      color: white;
    }

    .btn-success:hover:not(:disabled) {
      background: #45a049;
    }

    .btn-primary:disabled,
    .btn-secondary:disabled,
    .btn-success:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }

    .modal-overlay {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.5);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 1000;
    }

    .modal-content {
      background: white;
      border-radius: 12px;
      padding: 30px;
      max-width: 500px;
      width: 90%;
    }

    .modal-content h2 {
      margin-bottom: 15px;
    }

    .modal-content .warning {
      color: #d32f2f;
      font-weight: bold;
    }

    .modal-actions {
      display: flex;
      gap: 15px;
      justify-content: flex-end;
      margin-top: 20px;
    }

    .quiz-results {
      display: flex;
      justify-content: center;
      padding: 50px 0;
    }

    .results-card {
      background: white;
      border-radius: 12px;
      padding: 40px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      text-align: center;
      max-width: 600px;
      width: 100%;
    }

    .results-card.passed {
      border-top: 4px solid #4CAF50;
    }

    .results-icon {
      font-size: 64px;
      margin-bottom: 20px;
    }

    .score-display {
      display: flex;
      justify-content: center;
      margin: 30px 0;
    }

    .score-circle {
      position: relative;
      width: 200px;
      height: 200px;
    }

    .score-circle svg {
      transform: rotate(-90deg);
    }

    .score-bg {
      fill: none;
      stroke: #e0e0e0;
      stroke-width: 8;
    }

    .score-fill {
      fill: none;
      stroke: #4CAF50;
      stroke-width: 8;
      stroke-linecap: round;
      stroke-dasharray: 283;
      transition: stroke-dashoffset 1s ease;
    }

    .score-text {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      font-size: 48px;
      font-weight: bold;
      color: #4CAF50;
    }

    .results-details {
      background: #f5f5f5;
      border-radius: 8px;
      padding: 20px;
      margin: 20px 0;
    }

    .detail-item {
      display: flex;
      justify-content: space-between;
      padding: 10px 0;
      border-bottom: 1px solid #e0e0e0;
    }

    .detail-item:last-child {
      border-bottom: none;
    }

    .detail-item .label {
      font-weight: 600;
      color: #666;
    }

    .detail-item .value.passed {
      color: #4CAF50;
      font-weight: bold;
    }

    .results-actions {
      display: flex;
      gap: 15px;
      justify-content: center;
      margin-top: 30px;
      flex-wrap: wrap;
    }
  `],
})
export class QuizTakingComponent implements OnInit, OnDestroy {
  @Input() quizId!: number;
  @Output() completed = new EventEmitter<void>();

  quiz?: Quiz;
  attempt?: QuizAttempt;
  results?: QuizAttempt;

  currentQuestionIndex = 0;
  userAnswers: UserAnswer[] = [];

  timeRemaining = 0;
  private timerSubscription?: Subscription;

  loading = false;
  showSubmitConfirm = false;
  showResults = false;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadQuiz();
  }

  ngOnDestroy(): void {
    this.stopTimer();
  }

  private async loadQuiz(): Promise<void> {
    try {
      this.quiz = await this.http
        .get<Quiz>(`${environment.apiUrl}/quizzes/${this.quizId}`)
        .toPromise() as Quiz;
    } catch (error) {
      console.error('Error loading quiz:', error);
    }
  }

  async startQuiz(): Promise<void> {
    this.loading = true;
    try {
      this.attempt = await this.http
        .post<QuizAttempt>(
          `${environment.apiUrl}/quizzes/${this.quizId}/start`,
          {},
        )
        .toPromise() as QuizAttempt;

      if (this.quiz?.timeLimit) {
        this.timeRemaining = this.quiz.timeLimit;
        this.startTimer();
      }
    } catch (error) {
      console.error('Error starting quiz:', error);
      alert('Failed to start quiz. Please try again.');
    } finally {
      this.loading = false;
    }
  }

  private startTimer(): void {
    this.timerSubscription = interval(1000).subscribe(() => {
      this.timeRemaining--;
      if (this.timeRemaining <= 0) {
        this.timeExpired();
      }
    });
  }

  private stopTimer(): void {
    this.timerSubscription?.unsubscribe();
  }

  private timeExpired(): void {
    this.stopTimer();
    alert('Time is up! Submitting your quiz...');
    this.submitQuiz();
  }

  get currentQuestion(): Question | undefined {
    return this.quiz?.questions[this.currentQuestionIndex];
  }

  get answeredCount(): number {
    return this.userAnswers.filter((a) => {
      return (
        (a.selectedAnswers && a.selectedAnswers.length > 0) ||
        (a.textAnswer && a.textAnswer.trim() !== '') ||
        (a.matchingPairs && a.matchingPairs.length > 0)
      );
    }).length;
  }

  isQuestionAnswered(questionId: number): boolean {
    const answer = this.userAnswers.find((a) => a.questionId === questionId);
    return !!(
      (answer?.selectedAnswers && answer.selectedAnswers.length > 0) ||
      (answer?.textAnswer && answer.textAnswer.trim() !== '') ||
      (answer?.matchingPairs && answer.matchingPairs.length > 0)
    );
  }

  isAnswerSelected(questionId: number, answerId: number): boolean {
    const answer = this.userAnswers.find((a) => a.questionId === questionId);
    return answer?.selectedAnswers?.includes(answerId) || false;
  }

  selectSingleAnswer(questionId: number, answerId: number): void {
    const existingIndex = this.userAnswers.findIndex(
      (a) => a.questionId === questionId,
    );

    if (existingIndex >= 0) {
      this.userAnswers[existingIndex].selectedAnswers = [answerId];
    } else {
      this.userAnswers.push({ questionId, selectedAnswers: [answerId] });
    }
  }

  toggleAnswer(questionId: number, answerId: number): void {
    const existingIndex = this.userAnswers.findIndex(
      (a) => a.questionId === questionId,
    );

    if (existingIndex >= 0) {
      const answers = this.userAnswers[existingIndex].selectedAnswers || [];
      const answerIndex = answers.indexOf(answerId);

      if (answerIndex >= 0) {
        answers.splice(answerIndex, 1);
      } else {
        answers.push(answerId);
      }

      this.userAnswers[existingIndex].selectedAnswers = answers;
    } else {
      this.userAnswers.push({ questionId, selectedAnswers: [answerId] });
    }
  }

  getTextAnswer(questionId: number): string {
    return (
      this.userAnswers.find((a) => a.questionId === questionId)?.textAnswer ||
      ''
    );
  }

  setTextAnswer(questionId: number, event: any): void {
    const text = event.target.value;
    const existingIndex = this.userAnswers.findIndex(
      (a) => a.questionId === questionId,
    );

    if (existingIndex >= 0) {
      this.userAnswers[existingIndex].textAnswer = text;
    } else {
      this.userAnswers.push({ questionId, textAnswer: text });
    }
  }

  getMatchingPair(questionId: number, leftId: string): string {
    const answer = this.userAnswers.find((a) => a.questionId === questionId);
    return (
      answer?.matchingPairs?.find((p) => p.leftId === leftId)?.rightId || ''
    );
  }

  setMatchingPair(questionId: number, leftId: string, event: any): void {
    const rightId = event.target.value;
    const existingIndex = this.userAnswers.findIndex(
      (a) => a.questionId === questionId,
    );

    if (existingIndex >= 0) {
      const pairs = this.userAnswers[existingIndex].matchingPairs || [];
      const pairIndex = pairs.findIndex((p) => p.leftId === leftId);

      if (pairIndex >= 0) {
        pairs[pairIndex].rightId = rightId;
      } else {
        pairs.push({ leftId, rightId });
      }

      this.userAnswers[existingIndex].matchingPairs = pairs;
    } else {
      this.userAnswers.push({
        questionId,
        matchingPairs: [{ leftId, rightId }],
      });
    }
  }

  goToQuestion(index: number): void {
    this.currentQuestionIndex = index;
  }

  previousQuestion(): void {
    if (this.currentQuestionIndex > 0) {
      this.currentQuestionIndex--;
    }
  }

  nextQuestion(): void {
    if (this.quiz && this.currentQuestionIndex < this.quiz.questions.length - 1) {
      this.currentQuestionIndex++;
    }
  }

  confirmSubmit(): void {
    this.showSubmitConfirm = true;
  }

  async submitQuiz(): Promise<void> {
    this.showSubmitConfirm = false;
    this.loading = true;
    this.stopTimer();

    try {
      this.results = await this.http
        .post<QuizAttempt>(
          `${environment.apiUrl}/quizzes/attempts/${this.attempt!.id}/submit`,
          { answers: this.userAnswers },
        )
        .toPromise() as QuizAttempt;

      this.showResults = true;
    } catch (error) {
      console.error('Error submitting quiz:', error);
      alert('Failed to submit quiz. Please try again.');
    } finally {
      this.loading = false;
    }
  }

  reviewAnswers(): void {
    // TODO: Implement answer review
    alert('Review feature coming soon!');
  }

  retryQuiz(): void {
    this.attempt = undefined;
    this.results = undefined;
    this.showResults = false;
    this.userAnswers = [];
    this.currentQuestionIndex = 0;
    this.loadQuiz();
  }

  closeQuiz(): void {
    this.completed.emit();
  }

  formatTime(seconds: number): string {
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    const s = Math.floor(seconds % 60);

    if (h > 0) {
      return `${h}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
    }
    return `${m}:${s.toString().padStart(2, '0')}`;
  }
}
