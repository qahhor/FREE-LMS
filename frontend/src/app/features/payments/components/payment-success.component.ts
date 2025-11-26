import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { PaymentService } from '../services/payment.service';
import { Payment } from '../models/payment.models';

@Component({
  selector: 'app-payment-success',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="payment-success">
      <div class="container">
        <div *ngIf="loading()" class="loading">
          <div class="spinner"></div>
          <p>Проверка платежа...</p>
        </div>

        <div *ngIf="!loading() && payment()" class="success-card">
          <div class="success-animation">
            <div class="checkmark-circle">
              <div class="checkmark">✓</div>
            </div>
          </div>

          <h1>Оплата успешно завершена!</h1>
          <p class="subtitle">
            Спасибо за подписку! Ваш платеж обработан.
          </p>

          <div class="payment-details">
            <div class="detail-row">
              <span class="label">Номер транзакции:</span>
              <span class="value">{{ payment()!.transactionId || payment()!.id }}</span>
            </div>
            <div class="detail-row">
              <span class="label">Сумма:</span>
              <span class="value amount">
                {{ payment()!.amount }} {{ getCurrencySymbol(payment()!.currency) }}
              </span>
            </div>
            <div class="detail-row">
              <span class="label">Дата:</span>
              <span class="value">{{ formatDate(payment()!.createdAt) }}</span>
            </div>
            <div class="detail-row">
              <span class="label">Способ оплаты:</span>
              <span class="value">{{ getGatewayLabel(payment()!.gateway) }}</span>
            </div>
            <div class="detail-row">
              <span class="label">Статус:</span>
              <span class="value status">{{ getStatusLabel(payment()!.status) }}</span>
            </div>
          </div>

          <div class="next-steps">
            <h3>Что дальше?</h3>
            <ul>
              <li>✓ Ваша подписка активирована</li>
              <li>✓ Подтверждение отправлено на email</li>
              <li>✓ Квитанция доступна в личном кабинете</li>
              <li>✓ Начните использовать все возможности</li>
            </ul>
          </div>

          <div class="actions">
            <button class="btn-primary" (click)="goToDashboard()">
              Перейти в панель управления
            </button>
            <button class="btn-secondary" (click)="downloadReceipt()">
              📄 Скачать квитанцию
            </button>
          </div>

          <div class="share-section">
            <p>Поделитесь с коллегами:</p>
            <div class="social-buttons">
              <button class="social-btn telegram" (click)="shareToTelegram()">
                Telegram
              </button>
              <button class="social-btn whatsapp" (click)="shareToWhatsApp()">
                WhatsApp
              </button>
              <button class="social-btn copy" (click)="copyLink()">
                📋 Копировать ссылку
              </button>
            </div>
          </div>
        </div>

        <div *ngIf="!loading() && !payment()" class="error-card">
          <div class="error-icon">⚠️</div>
          <h2>Не удалось загрузить информацию о платеже</h2>
          <p>Пожалуйста, проверьте свою почту или свяжитесь с поддержкой</p>
          <button class="btn-primary" (click)="goToHome()">
            На главную
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .payment-success {
      padding: 60px 20px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      min-height: 100vh;
      display: flex;
      align-items: center;
    }

    .container {
      max-width: 700px;
      margin: 0 auto;
      width: 100%;
    }

    .loading {
      text-align: center;
      color: white;
    }

    .spinner {
      width: 60px;
      height: 60px;
      border: 4px solid rgba(255,255,255,0.3);
      border-top-color: white;
      border-radius: 50%;
      animation: spin 1s linear infinite;
      margin: 0 auto 20px;
    }

    @keyframes spin {
      to { transform: rotate(360deg); }
    }

    .loading p {
      font-size: 1.125rem;
      opacity: 0.9;
    }

    .success-card, .error-card {
      background: white;
      border-radius: 24px;
      padding: 60px 40px;
      box-shadow: 0 20px 60px rgba(0,0,0,0.3);
      text-align: center;
    }

    .success-animation {
      margin-bottom: 30px;
    }

    .checkmark-circle {
      width: 120px;
      height: 120px;
      margin: 0 auto;
      background: linear-gradient(135deg, #4caf50 0%, #66bb6a 100%);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      animation: scaleIn 0.5s ease-out;
    }

    @keyframes scaleIn {
      from {
        transform: scale(0);
        opacity: 0;
      }
      to {
        transform: scale(1);
        opacity: 1;
      }
    }

    .checkmark {
      font-size: 4rem;
      color: white;
      font-weight: 700;
    }

    h1 {
      margin: 0 0 12px 0;
      color: #333;
      font-size: 2rem;
    }

    .subtitle {
      color: #666;
      font-size: 1.125rem;
      margin: 0 0 40px 0;
    }

    .payment-details {
      background: #f8f9fa;
      padding: 30px;
      border-radius: 16px;
      margin-bottom: 40px;
      text-align: left;
    }

    .detail-row {
      display: flex;
      justify-content: space-between;
      padding: 12px 0;
      border-bottom: 1px solid #e0e0e0;
    }

    .detail-row:last-child {
      border-bottom: none;
    }

    .label {
      color: #999;
      font-weight: 500;
    }

    .value {
      color: #333;
      font-weight: 600;
    }

    .value.amount {
      color: #4caf50;
      font-size: 1.125rem;
    }

    .value.status {
      color: #4caf50;
      text-transform: capitalize;
    }

    .next-steps {
      background: #e8f5e9;
      padding: 30px;
      border-radius: 16px;
      margin-bottom: 30px;
      text-align: left;
    }

    .next-steps h3 {
      margin: 0 0 20px 0;
      color: #2e7d32;
    }

    .next-steps ul {
      list-style: none;
      padding: 0;
      margin: 0;
    }

    .next-steps li {
      padding: 8px 0;
      color: #2e7d32;
      font-weight: 500;
    }

    .actions {
      display: flex;
      flex-direction: column;
      gap: 12px;
      margin-bottom: 40px;
    }

    button {
      padding: 16px 32px;
      border: none;
      border-radius: 12px;
      font-weight: 600;
      font-size: 1rem;
      cursor: pointer;
      transition: all 0.3s;
    }

    .btn-primary {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
    }

    .btn-primary:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
    }

    .btn-secondary {
      background: transparent;
      border: 2px solid #667eea;
      color: #667eea;
    }

    .btn-secondary:hover {
      background: #f0f4ff;
    }

    .share-section {
      padding-top: 30px;
      border-top: 1px solid #e0e0e0;
    }

    .share-section p {
      color: #666;
      margin: 0 0 16px 0;
    }

    .social-buttons {
      display: flex;
      gap: 12px;
      justify-content: center;
    }

    .social-btn {
      padding: 12px 24px;
      border-radius: 8px;
      font-weight: 600;
      font-size: 0.875rem;
      transition: transform 0.2s;
    }

    .social-btn:hover {
      transform: scale(1.05);
    }

    .social-btn.telegram {
      background: #0088cc;
      color: white;
    }

    .social-btn.whatsapp {
      background: #25d366;
      color: white;
    }

    .social-btn.copy {
      background: #f8f9fa;
      color: #667eea;
      border: 1px solid #ddd;
    }

    .error-card {
      padding: 80px 40px;
    }

    .error-icon {
      font-size: 5rem;
      margin-bottom: 24px;
    }

    .error-card h2 {
      color: #333;
      margin: 0 0 12px 0;
    }

    .error-card p {
      color: #666;
      margin: 0 0 30px 0;
    }

    @media (max-width: 768px) {
      .payment-success {
        padding: 40px 20px;
      }

      .success-card, .error-card {
        padding: 40px 24px;
      }

      h1 {
        font-size: 1.5rem;
      }

      .social-buttons {
        flex-direction: column;
      }
    }
  `]
})
export class PaymentSuccessComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private paymentService = inject(PaymentService);

  payment = signal<Payment | null>(null);
  loading = signal(true);

  ngOnInit() {
    const paymentId = this.route.snapshot.queryParams['paymentId'];
    if (paymentId) {
      this.loadPayment(paymentId);
    } else {
      this.loading.set(false);
    }
  }

  loadPayment(id: number) {
    this.paymentService.getPayment(id).subscribe({
      next: (payment) => {
        this.payment.set(payment);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }

  getCurrencySymbol(currency: string): string {
    const symbols: any = {
      'usd': '$',
      'uzs': 'сум',
      'eur': '€',
      'rub': '₽'
    };
    return symbols[currency] || currency;
  }

  getGatewayLabel(gateway: string): string {
    const labels: any = {
      'stripe': 'Stripe (Карта)',
      'payme': 'Payme',
      'click': 'Click'
    };
    return labels[gateway] || gateway;
  }

  getStatusLabel(status: string): string {
    const labels: any = {
      'completed': 'Завершено',
      'pending': 'Ожидание',
      'processing': 'Обработка',
      'failed': 'Ошибка'
    };
    return labels[status] || status;
  }

  formatDate(date: Date): string {
    return new Date(date).toLocaleString('ru-RU');
  }

  goToDashboard() {
    this.router.navigate(['/subscriptions/dashboard']);
  }

  downloadReceipt() {
    // TODO: Implement receipt download
    alert('Квитанция будет скачана');
  }

  shareToTelegram() {
    const text = encodeURIComponent('Я подписался на Smartup LMS! Присоединяйтесь!');
    window.open(`https://t.me/share/url?url=${window.location.origin}&text=${text}`, '_blank');
  }

  shareToWhatsApp() {
    const text = encodeURIComponent('Я подписался на Smartup LMS! Присоединяйтесь!');
    window.open(`https://wa.me/?text=${text} ${window.location.origin}`, '_blank');
  }

  copyLink() {
    navigator.clipboard.writeText(window.location.origin);
    alert('Ссылка скопирована!');
  }

  goToHome() {
    this.router.navigate(['/']);
  }
}
