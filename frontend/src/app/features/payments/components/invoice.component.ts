import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PaymentService } from '../services/payment.service';
import { Invoice } from '../models/payment.models';

@Component({
  selector: 'app-invoice',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="invoices-page">
      <div class="container">
        <div class="header">
          <h1>Счета и квитанции</h1>
          <p class="subtitle">Просматривайте и скачивайте ваши счета</p>
        </div>

        <div *ngIf="loading()" class="loading">
          <div class="spinner"></div>
          <p>Загрузка счетов...</p>
        </div>

        <div *ngIf="!loading() && invoices().length === 0" class="empty-state">
          <div class="empty-icon">📄</div>
          <h2>Нет счетов</h2>
          <p>Здесь будут отображаться ваши счета после оплаты</p>
        </div>

        <div *ngIf="!loading() && invoices().length > 0" class="invoices-list">
          <div *ngFor="let invoice of invoices()" class="invoice-card">
            <div class="invoice-header">
              <div class="invoice-number">
                <span class="label">Счет №</span>
                <span class="number">{{ invoice.invoiceNumber }}</span>
              </div>
              <span class="status-badge" [class]="invoice.status">
                {{ getStatusLabel(invoice.status) }}
              </span>
            </div>

            <div class="invoice-body">
              <div class="invoice-info">
                <div class="info-row">
                  <span class="info-label">Дата выставления:</span>
                  <span class="info-value">{{ formatDate(invoice.issueDate) }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label">Срок оплаты:</span>
                  <span class="info-value">{{ formatDate(invoice.dueDate) }}</span>
                </div>
                <div *ngIf="invoice.paidAt" class="info-row">
                  <span class="info-label">Оплачен:</span>
                  <span class="info-value">{{ formatDate(invoice.paidAt) }}</span>
                </div>
              </div>

              <div class="invoice-items">
                <table>
                  <thead>
                    <tr>
                      <th>Описание</th>
                      <th>Кол-во</th>
                      <th>Цена</th>
                      <th>Итого</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr *ngFor="let item of invoice.items">
                      <td>{{ item.description }}</td>
                      <td>{{ item.quantity }}</td>
                      <td>{{ formatAmount(item.unitPrice, invoice.currency) }}</td>
                      <td>{{ formatAmount(item.total, invoice.currency) }}</td>
                    </tr>
                  </tbody>
                  <tfoot>
                    <tr class="total-row">
                      <td colspan="3">Итого:</td>
                      <td class="total-amount">
                        {{ formatAmount(invoice.amount, invoice.currency) }}
                      </td>
                    </tr>
                  </tfoot>
                </table>
              </div>
            </div>

            <div class="invoice-actions">
              <button class="btn-icon" (click)="viewInvoice(invoice)" title="Просмотр">
                👁️ Просмотр
              </button>
              <button class="btn-icon" (click)="downloadInvoice(invoice)" title="Скачать PDF">
                📥 Скачать PDF
              </button>
              <button class="btn-icon" (click)="printInvoice(invoice)" title="Печать">
                🖨️ Печать
              </button>
            </div>
          </div>
        </div>

        <!-- Invoice Preview Modal -->
        <div *ngIf="showPreviewModal && selectedInvoice" class="modal-overlay" (click)="showPreviewModal = false">
          <div class="modal invoice-modal" (click)="$event.stopPropagation()">
            <div class="modal-header">
              <h3>Предпросмотр счета</h3>
              <button class="btn-close" (click)="showPreviewModal = false">×</button>
            </div>
            <div class="invoice-preview">
              <div class="preview-header">
                <div class="company-info">
                  <h2>Smartup LMS</h2>
                  <p>Платформа онлайн-обучения</p>
                </div>
                <div class="invoice-meta">
                  <h3>Счет № {{ selectedInvoice.invoiceNumber }}</h3>
                  <p>Дата: {{ formatDate(selectedInvoice.issueDate) }}</p>
                </div>
              </div>

              <div class="preview-body">
                <div class="section">
                  <h4>Получатель:</h4>
                  <p>Ваше имя</p>
                  <p>Email: your@email.com</p>
                </div>

                <div class="section">
                  <table class="preview-table">
                    <thead>
                      <tr>
                        <th>Описание</th>
                        <th>Кол-во</th>
                        <th>Цена</th>
                        <th>Итого</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr *ngFor="let item of selectedInvoice.items">
                        <td>{{ item.description }}</td>
                        <td>{{ item.quantity }}</td>
                        <td>{{ formatAmount(item.unitPrice, selectedInvoice.currency) }}</td>
                        <td>{{ formatAmount(item.total, selectedInvoice.currency) }}</td>
                      </tr>
                    </tbody>
                  </table>
                </div>

                <div class="preview-total">
                  <div class="total-line">
                    <span>Итого:</span>
                    <span class="amount">{{ formatAmount(selectedInvoice.amount, selectedInvoice.currency) }}</span>
                  </div>
                </div>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn-secondary" (click)="downloadInvoice(selectedInvoice)">
                📥 Скачать PDF
              </button>
              <button class="btn-primary" (click)="printInvoice(selectedInvoice)">
                🖨️ Печать
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .invoices-page { padding: 40px 20px; background: #f5f7fa; min-height: 100vh; }
    .container { max-width: 1200px; margin: 0 auto; }
    .header { margin-bottom: 40px; }
    h1 { font-size: 2rem; color: #333; margin: 0 0 8px 0; }
    .subtitle { color: #666; margin: 0; }

    .loading { text-align: center; padding: 100px 20px; }
    .spinner { width: 60px; height: 60px; border: 4px solid #f3f3f3; border-top-color: #667eea; border-radius: 50%; animation: spin 1s linear infinite; margin: 0 auto 20px; }
    @keyframes spin { to { transform: rotate(360deg); } }

    .empty-state { text-align: center; padding: 100px 20px; background: white; border-radius: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
    .empty-icon { font-size: 5rem; margin-bottom: 20px; }
    .empty-state h2 { color: #333; margin: 0 0 8px 0; }
    .empty-state p { color: #666; margin: 0; }

    .invoices-list { display: flex; flex-direction: column; gap: 20px; }
    .invoice-card { background: white; border-radius: 16px; padding: 30px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }

    .invoice-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; padding-bottom: 24px; border-bottom: 2px solid #f0f0f0; }
    .invoice-number { }
    .label { display: block; color: #999; font-size: 0.875rem; margin-bottom: 4px; }
    .number { font-size: 1.5rem; font-weight: 700; color: #333; }

    .status-badge { padding: 8px 20px; border-radius: 20px; font-size: 0.875rem; font-weight: 600; }
    .status-badge.paid { background: #e8f5e9; color: #2e7d32; }
    .status-badge.sent { background: #e3f2fd; color: #1976d2; }
    .status-badge.overdue { background: #ffebee; color: #c62828; }
    .status-badge.draft { background: #f5f5f5; color: #666; }

    .invoice-body { margin-bottom: 24px; }
    .invoice-info { margin-bottom: 24px; }
    .info-row { display: flex; justify-content: space-between; padding: 12px 0; border-bottom: 1px solid #f0f0f0; }
    .info-label { color: #999; }
    .info-value { color: #333; font-weight: 600; }

    .invoice-items { overflow-x: auto; }
    table { width: 100%; border-collapse: collapse; }
    thead { background: #f8f9fa; }
    th { padding: 12px; text-align: left; font-weight: 600; color: #555; border-bottom: 2px solid #e0e0e0; }
    td { padding: 12px; border-bottom: 1px solid #f0f0f0; color: #666; }
    tfoot { background: #f8f9fa; }
    .total-row { font-weight: 700; }
    .total-amount { color: #667eea; font-size: 1.25rem; }

    .invoice-actions { display: flex; gap: 12px; }
    .btn-icon { flex: 1; padding: 12px 24px; background: transparent; border: 2px solid #667eea; color: #667eea; border-radius: 8px; font-weight: 600; cursor: pointer; transition: all 0.3s; }
    .btn-icon:hover { background: #f0f4ff; }

    .modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
    .modal { background: white; border-radius: 16px; max-width: 900px; width: 90%; max-height: 90vh; overflow-y: auto; }
    .modal-header { padding: 24px; border-bottom: 1px solid #f0f0f0; display: flex; justify-content: space-between; align-items: center; }
    .btn-close { background: none; border: none; font-size: 2rem; color: #999; cursor: pointer; padding: 0; width: 32px; height: 32px; }
    .modal-footer { padding: 24px; border-top: 1px solid #f0f0f0; display: flex; gap: 12px; justify-content: flex-end; }

    .invoice-preview { padding: 40px; }
    .preview-header { display: flex; justify-content: space-between; margin-bottom: 40px; padding-bottom: 24px; border-bottom: 2px solid #e0e0e0; }
    .company-info h2 { margin: 0 0 4px 0; color: #667eea; }
    .company-info p { margin: 0; color: #666; }
    .invoice-meta { text-align: right; }
    .invoice-meta h3 { margin: 0 0 4px 0; color: #333; }
    .invoice-meta p { margin: 0; color: #666; }

    .preview-body { }
    .section { margin-bottom: 30px; }
    .section h4 { margin: 0 0 12px 0; color: #333; }
    .section p { margin: 4px 0; color: #666; }

    .preview-table { width: 100%; margin-top: 20px; }
    .preview-total { margin-top: 30px; padding-top: 20px; border-top: 2px solid #e0e0e0; }
    .total-line { display: flex; justify-content: space-between; align-items: center; }
    .total-line span { font-size: 1.25rem; font-weight: 700; }
    .total-line .amount { color: #667eea; font-size: 1.75rem; }

    button { transition: all 0.3s; }
    .btn-primary { padding: 12px 32px; background: #667eea; color: white; border: none; border-radius: 8px; font-weight: 600; cursor: pointer; }
    .btn-primary:hover { background: #5568d3; }
    .btn-secondary { padding: 12px 32px; background: transparent; border: 2px solid #667eea; color: #667eea; border-radius: 8px; font-weight: 600; cursor: pointer; }
    .btn-secondary:hover { background: #f0f4ff; }

    @media (max-width: 768px) {
      .invoice-actions { flex-direction: column; }
    }
  `]
})
export class InvoiceComponent implements OnInit {
  private paymentService = inject(PaymentService);

  invoices = signal<Invoice[]>([]);
  loading = signal(true);
  showPreviewModal = false;
  selectedInvoice: Invoice | null = null;

  ngOnInit() {
    this.loadInvoices();
  }

  loadInvoices() {
    this.loading.set(true);
    this.paymentService.getInvoices().subscribe({
      next: (data) => {
        this.invoices.set(data.invoices);
        this.loading.set(false);
      }
    });
  }

  viewInvoice(invoice: Invoice) {
    this.selectedInvoice = invoice;
    this.showPreviewModal = true;
  }

  downloadInvoice(invoice: Invoice) {
    this.paymentService.downloadInvoice(invoice.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `invoice-${invoice.invoiceNumber}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: () => alert('Ошибка при скачивании счета')
    });
  }

  printInvoice(invoice: Invoice) {
    window.print();
  }

  getStatusLabel(status: string): string {
    const labels: any = {
      'paid': 'Оплачен',
      'sent': 'Отправлен',
      'draft': 'Черновик',
      'overdue': 'Просрочен',
      'cancelled': 'Отменен'
    };
    return labels[status] || status;
  }

  formatDate(date: Date): string {
    return new Date(date).toLocaleDateString('ru-RU');
  }

  formatAmount(amount: number, currency: string): string {
    const symbols: any = {
      'usd': '$',
      'uzs': 'сум',
      'eur': '€',
      'rub': '₽'
    };
    return `${amount.toLocaleString('ru-RU')} ${symbols[currency] || currency}`;
  }
}
