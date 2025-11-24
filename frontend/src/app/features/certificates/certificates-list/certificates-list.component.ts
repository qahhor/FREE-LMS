import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { environment } from '@environments/environment';

interface Certificate {
  id: number;
  certificateNumber: string;
  courseTitle: string;
  studentName: string;
  instructorName?: string;
  issuedDate: Date;
  completionDate: Date;
  finalScore?: number;
  totalHours?: number;
  grade?: string;
  pdfUrl: string;
  thumbnailUrl?: string;
  verificationCode: string;
  isValid: boolean;
  viewCount: number;
  downloadCount: number;
}

/**
 * Certificate list and viewer component
 */
@Component({
  selector: 'app-certificates-list',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="certificates-container">
      <div class="header">
        <h1>🎓 My Certificates</h1>
        <p class="subtitle">
          Showcase your achievements and share your success
        </p>
      </div>

      <!-- Loading State -->
      <div *ngIf="loading" class="loading">
        <div class="spinner"></div>
        <p>Loading certificates...</p>
      </div>

      <!-- Empty State -->
      <div *ngIf="!loading && certificates.length === 0" class="empty-state">
        <div class="empty-icon">📜</div>
        <h2>No Certificates Yet</h2>
        <p>Complete a course to earn your first certificate!</p>
      </div>

      <!-- Certificates Grid -->
      <div *ngIf="!loading && certificates.length > 0" class="certificates-grid">
        <div
          *ngFor="let certificate of certificates"
          class="certificate-card"
          [class.selected]="selectedCertificate?.id === certificate.id"
          (click)="selectCertificate(certificate)"
        >
          <div class="certificate-preview">
            <div class="certificate-icon">🏆</div>
            <div class="certificate-badge">VERIFIED</div>
          </div>

          <div class="certificate-info">
            <h3>{{ certificate.courseTitle }}</h3>
            <p class="certificate-number">{{ certificate.certificateNumber }}</p>

            <div class="certificate-meta">
              <div class="meta-item">
                <span class="meta-icon">📅</span>
                <span>{{ formatDate(certificate.issuedDate) }}</span>
              </div>
              <div class="meta-item" *ngIf="certificate.grade">
                <span class="meta-icon">🎯</span>
                <span>Grade: {{ certificate.grade }}</span>
              </div>
              <div class="meta-item" *ngIf="certificate.totalHours">
                <span class="meta-icon">⏱️</span>
                <span>{{ certificate.totalHours.toFixed(1) }}h</span>
              </div>
            </div>

            <div class="certificate-stats">
              <span>👁️ {{ certificate.viewCount }}</span>
              <span>⬇️ {{ certificate.downloadCount }}</span>
            </div>
          </div>

          <div class="certificate-actions">
            <button class="btn-view" (click)="viewCertificate(certificate, $event)">
              👁️ View
            </button>
            <button class="btn-download" (click)="downloadCertificate(certificate, $event)">
              ⬇️ Download
            </button>
            <button class="btn-share" (click)="shareCertificate(certificate, $event)">
              📤 Share
            </button>
          </div>
        </div>
      </div>

      <!-- Certificate Detail Modal -->
      <div *ngIf="selectedCertificate" class="modal-overlay" (click)="closeCertificate()">
        <div class="modal-content certificate-detail" (click)="$event.stopPropagation()">
          <button class="close-btn" (click)="closeCertificate()">×</button>

          <!-- Certificate Design -->
          <div class="certificate-preview-large">
            <div class="certificate-border">
              <div class="certificate-header">
                <div class="logo">🎓</div>
                <h2>Certificate of Completion</h2>
                <div class="divider"></div>
              </div>

              <div class="certificate-body">
                <p class="cert-label">This certifies that</p>
                <h1 class="student-name">{{ selectedCertificate.studentName }}</h1>

                <p class="cert-label">has successfully completed</p>
                <h2 class="course-title">{{ selectedCertificate.courseTitle }}</h2>

                <div class="completion-details">
                  <div class="detail-item">
                    <span class="detail-label">Completed on</span>
                    <span class="detail-value">
                      {{ formatDateFull(selectedCertificate.completionDate) }}
                    </span>
                  </div>

                  <div class="detail-item" *ngIf="selectedCertificate.finalScore">
                    <span class="detail-label">Final Score</span>
                    <span class="detail-value">
                      {{ selectedCertificate.finalScore.toFixed(0) }}%
                    </span>
                  </div>

                  <div class="detail-item" *ngIf="selectedCertificate.grade">
                    <span class="detail-label">Grade</span>
                    <span class="detail-value">{{ selectedCertificate.grade }}</span>
                  </div>

                  <div class="detail-item" *ngIf="selectedCertificate.totalHours">
                    <span class="detail-label">Total Hours</span>
                    <span class="detail-value">
                      {{ selectedCertificate.totalHours.toFixed(1) }}
                    </span>
                  </div>
                </div>

                <div class="signatures" *ngIf="selectedCertificate.instructorName">
                  <div class="signature">
                    <div class="signature-line"></div>
                    <p>{{ selectedCertificate.instructorName }}</p>
                    <p class="signature-title">Course Instructor</p>
                  </div>
                </div>
              </div>

              <div class="certificate-footer">
                <div class="certificate-number-display">
                  Certificate No: {{ selectedCertificate.certificateNumber }}
                </div>
                <div class="verification-code">
                  <span class="verification-label">Verification Code:</span>
                  <span class="verification-value">
                    {{ selectedCertificate.verificationCode.substring(0, 16) }}...
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- Action Buttons -->
          <div class="modal-actions">
            <button class="btn-primary" (click)="downloadCertificate(selectedCertificate, $event)">
              ⬇️ Download PDF
            </button>
            <button class="btn-secondary" (click)="shareCertificate(selectedCertificate, $event)">
              📤 Share
            </button>
            <button class="btn-secondary" (click)="verifyCertificate(selectedCertificate)">
              ✓ Verify
            </button>
          </div>
        </div>
      </div>

      <!-- Share Modal -->
      <div *ngIf="showShareModal" class="modal-overlay" (click)="closeShareModal()">
        <div class="modal-content share-modal" (click)="$event.stopPropagation()">
          <button class="close-btn" (click)="closeShareModal()">×</button>
          <h2>Share Certificate</h2>

          <div class="share-options">
            <button class="share-btn linkedin" (click)="shareOnLinkedIn()">
              <span class="share-icon">💼</span>
              <span>LinkedIn</span>
            </button>
            <button class="share-btn twitter" (click)="shareOnTwitter()">
              <span class="share-icon">🐦</span>
              <span>Twitter</span>
            </button>
            <button class="share-btn facebook" (click)="shareOnFacebook()">
              <span class="share-icon">📘</span>
              <span>Facebook</span>
            </button>
          </div>

          <div class="share-link">
            <label>Verification Link:</label>
            <div class="link-box">
              <input
                type="text"
                readonly
                [value]="verificationLink"
                #linkInput
              />
              <button class="btn-copy" (click)="copyLink(linkInput)">
                {{ copied ? '✓ Copied' : '📋 Copy' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .certificates-container {
      max-width: 1400px;
      margin: 0 auto;
      padding: 40px 20px;
    }

    .header {
      text-align: center;
      margin-bottom: 50px;
    }

    .header h1 {
      font-size: 42px;
      margin-bottom: 10px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }

    .subtitle {
      font-size: 18px;
      color: #666;
    }

    .loading,
    .empty-state {
      text-align: center;
      padding: 80px 20px;
    }

    .spinner {
      width: 50px;
      height: 50px;
      border: 4px solid #f3f3f3;
      border-top: 4px solid #667eea;
      border-radius: 50%;
      animation: spin 1s linear infinite;
      margin: 0 auto 20px;
    }

    @keyframes spin {
      to { transform: rotate(360deg); }
    }

    .empty-icon {
      font-size: 80px;
      margin-bottom: 20px;
    }

    .certificates-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
      gap: 30px;
    }

    .certificate-card {
      background: white;
      border-radius: 16px;
      padding: 20px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      cursor: pointer;
      transition: all 0.3s;
    }

    .certificate-card:hover {
      transform: translateY(-8px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
    }

    .certificate-card.selected {
      border: 3px solid #667eea;
    }

    .certificate-preview {
      position: relative;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 12px;
      padding: 40px;
      text-align: center;
      margin-bottom: 20px;
    }

    .certificate-icon {
      font-size: 60px;
      margin-bottom: 10px;
    }

    .certificate-badge {
      background: rgba(255, 255, 255, 0.2);
      color: white;
      padding: 6px 12px;
      border-radius: 20px;
      font-size: 12px;
      font-weight: bold;
      display: inline-block;
    }

    .certificate-info h3 {
      font-size: 20px;
      margin-bottom: 8px;
      color: #333;
    }

    .certificate-number {
      font-size: 13px;
      color: #999;
      font-family: monospace;
      margin-bottom: 15px;
    }

    .certificate-meta {
      display: flex;
      flex-wrap: wrap;
      gap: 12px;
      margin-bottom: 15px;
    }

    .meta-item {
      display: flex;
      align-items: center;
      gap: 5px;
      font-size: 14px;
      color: #666;
    }

    .meta-icon {
      font-size: 16px;
    }

    .certificate-stats {
      display: flex;
      gap: 15px;
      font-size: 14px;
      color: #999;
      padding-top: 15px;
      border-top: 1px solid #f0f0f0;
    }

    .certificate-actions {
      display: flex;
      gap: 8px;
      margin-top: 15px;
    }

    .certificate-actions button {
      flex: 1;
      padding: 10px;
      border: none;
      border-radius: 8px;
      font-size: 14px;
      cursor: pointer;
      transition: all 0.2s;
    }

    .btn-view {
      background: #667eea;
      color: white;
    }

    .btn-download {
      background: #4CAF50;
      color: white;
    }

    .btn-share {
      background: #2196F3;
      color: white;
    }

    .certificate-actions button:hover {
      opacity: 0.9;
      transform: scale(1.05);
    }

    .modal-overlay {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.7);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 1000;
      padding: 20px;
    }

    .modal-content {
      background: white;
      border-radius: 16px;
      padding: 40px;
      max-width: 900px;
      width: 100%;
      max-height: 90vh;
      overflow-y: auto;
      position: relative;
    }

    .close-btn {
      position: absolute;
      top: 20px;
      right: 20px;
      background: none;
      border: none;
      font-size: 32px;
      cursor: pointer;
      color: #999;
      width: 40px;
      height: 40px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.2s;
    }

    .close-btn:hover {
      background: #f0f0f0;
      color: #333;
    }

    .certificate-preview-large {
      margin-bottom: 30px;
    }

    .certificate-border {
      border: 8px double #667eea;
      padding: 40px;
      background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
    }

    .certificate-header {
      text-align: center;
      margin-bottom: 40px;
    }

    .logo {
      font-size: 64px;
      margin-bottom: 10px;
    }

    .certificate-header h2 {
      font-size: 32px;
      color: #667eea;
      margin-bottom: 20px;
      text-transform: uppercase;
      letter-spacing: 2px;
    }

    .divider {
      width: 200px;
      height: 3px;
      background: linear-gradient(90deg, transparent, #667eea, transparent);
      margin: 0 auto;
    }

    .certificate-body {
      text-align: center;
      padding: 20px;
    }

    .cert-label {
      font-size: 16px;
      color: #666;
      margin: 20px 0 10px;
      font-style: italic;
    }

    .student-name {
      font-size: 48px;
      color: #333;
      margin: 10px 0 30px;
      font-family: 'Georgia', serif;
    }

    .course-title {
      font-size: 28px;
      color: #667eea;
      margin: 10px 0 30px;
    }

    .completion-details {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 20px;
      margin: 40px 0;
      padding: 30px;
      background: rgba(255, 255, 255, 0.5);
      border-radius: 12px;
    }

    .detail-item {
      text-align: center;
    }

    .detail-label {
      display: block;
      font-size: 14px;
      color: #666;
      margin-bottom: 5px;
    }

    .detail-value {
      display: block;
      font-size: 20px;
      font-weight: bold;
      color: #333;
    }

    .signatures {
      display: flex;
      justify-content: center;
      margin-top: 60px;
    }

    .signature {
      text-align: center;
      min-width: 250px;
    }

    .signature-line {
      height: 2px;
      background: #333;
      margin-bottom: 10px;
    }

    .signature p {
      margin: 5px 0;
      color: #333;
    }

    .signature-title {
      font-size: 14px;
      color: #666;
    }

    .certificate-footer {
      text-align: center;
      margin-top: 30px;
      padding-top: 20px;
      border-top: 2px solid rgba(102, 126, 234, 0.3);
    }

    .certificate-number-display {
      font-size: 14px;
      color: #666;
      margin-bottom: 10px;
      font-family: monospace;
    }

    .verification-code {
      font-size: 12px;
      color: #999;
    }

    .verification-label {
      font-weight: bold;
    }

    .verification-value {
      font-family: monospace;
    }

    .modal-actions {
      display: flex;
      gap: 15px;
      justify-content: center;
    }

    .btn-primary,
    .btn-secondary {
      padding: 12px 24px;
      border: none;
      border-radius: 8px;
      font-size: 16px;
      cursor: pointer;
      transition: all 0.2s;
    }

    .btn-primary {
      background: #667eea;
      color: white;
    }

    .btn-secondary {
      background: #f0f0f0;
      color: #333;
    }

    .btn-primary:hover,
    .btn-secondary:hover {
      transform: scale(1.05);
    }

    .share-modal h2 {
      text-align: center;
      margin-bottom: 30px;
    }

    .share-options {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 15px;
      margin-bottom: 30px;
    }

    .share-btn {
      padding: 20px;
      border: 2px solid #e0e0e0;
      border-radius: 12px;
      background: white;
      cursor: pointer;
      transition: all 0.2s;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 10px;
    }

    .share-btn:hover {
      border-color: #667eea;
      transform: translateY(-4px);
    }

    .share-icon {
      font-size: 32px;
    }

    .share-link label {
      display: block;
      margin-bottom: 10px;
      font-weight: 600;
    }

    .link-box {
      display: flex;
      gap: 10px;
    }

    .link-box input {
      flex: 1;
      padding: 12px;
      border: 2px solid #e0e0e0;
      border-radius: 8px;
      font-family: monospace;
      font-size: 14px;
    }

    .btn-copy {
      padding: 12px 20px;
      background: #667eea;
      color: white;
      border: none;
      border-radius: 8px;
      cursor: pointer;
      white-space: nowrap;
    }

    .btn-copy:hover {
      opacity: 0.9;
    }
  `],
})
export class CertificatesListComponent implements OnInit {
  certificates: Certificate[] = [];
  selectedCertificate?: Certificate;
  showShareModal = false;
  verificationLink = '';
  copied = false;
  loading = true;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadCertificates();
  }

  private async loadCertificates(): Promise<void> {
    try {
      this.certificates = await this.http
        .get<Certificate[]>(`${environment.apiUrl}/certificates/my-certificates`)
        .toPromise() as Certificate[];
    } catch (error) {
      console.error('Error loading certificates:', error);
    } finally {
      this.loading = false;
    }
  }

  selectCertificate(certificate: Certificate): void {
    this.selectedCertificate = certificate;
  }

  closeCertificate(): void {
    this.selectedCertificate = undefined;
  }

  async viewCertificate(certificate: Certificate, event: Event): Promise<void> {
    event.stopPropagation();
    this.selectCertificate(certificate);
  }

  async downloadCertificate(certificate: Certificate, event: Event): Promise<void> {
    event.stopPropagation();
    try {
      const response = await this.http
        .get<{ url: string }>(
          `${environment.apiUrl}/certificates/${certificate.id}/download`,
        )
        .toPromise();

      if (response?.url) {
        window.open(response.url, '_blank');
      }
    } catch (error) {
      console.error('Error downloading certificate:', error);
    }
  }

  shareCertificate(certificate: Certificate, event: Event): void {
    event.stopPropagation();
    this.selectedCertificate = certificate;
    this.verificationLink = `${window.location.origin}/verify/${certificate.verificationCode}`;
    this.showShareModal = true;
    this.copied = false;
  }

  closeShareModal(): void {
    this.showShareModal = false;
  }

  shareOnLinkedIn(): void {
    const url = encodeURIComponent(this.verificationLink);
    window.open(
      `https://www.linkedin.com/sharing/share-offsite/?url=${url}`,
      '_blank',
    );
  }

  shareOnTwitter(): void {
    const text = encodeURIComponent(
      `I just earned a certificate for ${this.selectedCertificate?.courseTitle}! 🎓`,
    );
    const url = encodeURIComponent(this.verificationLink);
    window.open(
      `https://twitter.com/intent/tweet?text=${text}&url=${url}`,
      '_blank',
    );
  }

  shareOnFacebook(): void {
    const url = encodeURIComponent(this.verificationLink);
    window.open(
      `https://www.facebook.com/sharer/sharer.php?u=${url}`,
      '_blank',
    );
  }

  copyLink(input: HTMLInputElement): void {
    input.select();
    document.execCommand('copy');
    this.copied = true;
    setTimeout(() => (this.copied = false), 2000);
  }

  async verifyCertificate(certificate: Certificate): Promise<void> {
    window.open(`/verify/${certificate.verificationCode}`, '_blank');
  }

  formatDate(date: Date | string): string {
    return new Date(date).toLocaleDateString('en-US', {
      month: 'short',
      year: 'numeric',
    });
  }

  formatDateFull(date: Date | string): string {
    return new Date(date).toLocaleDateString('en-US', {
      month: 'long',
      day: 'numeric',
      year: 'numeric',
    });
  }
}
