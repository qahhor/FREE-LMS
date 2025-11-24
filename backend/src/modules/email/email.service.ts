import { Injectable } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import * as nodemailer from 'nodemailer';

export interface EmailOptions {
  to: string;
  subject: string;
  html: string;
  text?: string;
}

/**
 * Email service for sending notifications
 */
@Injectable()
export class EmailService {
  private transporter: nodemailer.Transporter;

  constructor(private configService: ConfigService) {
    this.transporter = nodemailer.createTransporter({
      host: this.configService.get('EMAIL_HOST', 'smtp.gmail.com'),
      port: this.configService.get('EMAIL_PORT', 587),
      secure: false,
      auth: {
        user: this.configService.get('EMAIL_USER'),
        pass: this.configService.get('EMAIL_PASSWORD'),
      },
    });
  }

  /**
   * Send generic email
   */
  async sendEmail(options: EmailOptions): Promise<void> {
    try {
      await this.transporter.sendMail({
        from: this.configService.get('EMAIL_FROM', '"FREE LMS" <noreply@freelms.com>'),
        ...options,
      });
    } catch (error) {
      console.error('Error sending email:', error);
      throw error;
    }
  }

  /**
   * Send welcome email
   */
  async sendWelcomeEmail(to: string, name: string): Promise<void> {
    const html = this.getWelcomeTemplate(name);
    await this.sendEmail({
      to,
      subject: 'Welcome to FREE LMS! 🎓',
      html,
    });
  }

  /**
   * Send course enrollment confirmation
   */
  async sendEnrollmentEmail(
    to: string,
    userName: string,
    courseTitle: string,
  ): Promise<void> {
    const html = this.getEnrollmentTemplate(userName, courseTitle);
    await this.sendEmail({
      to,
      subject: `You're enrolled in ${courseTitle}!`,
      html,
    });
  }

  /**
   * Send course completion congratulations
   */
  async sendCourseCompletionEmail(
    to: string,
    userName: string,
    courseTitle: string,
    certificateUrl?: string,
  ): Promise<void> {
    const html = this.getCourseCompletionTemplate(userName, courseTitle, certificateUrl);
    await this.sendEmail({
      to,
      subject: `🎉 Congratulations! You completed ${courseTitle}`,
      html,
    });
  }

  /**
   * Send certificate issued notification
   */
  async sendCertificateEmail(
    to: string,
    userName: string,
    courseTitle: string,
    certificateNumber: string,
    certificateUrl: string,
  ): Promise<void> {
    const html = this.getCertificateTemplate(
      userName,
      courseTitle,
      certificateNumber,
      certificateUrl,
    );
    await this.sendEmail({
      to,
      subject: `🏆 Your Certificate for ${courseTitle}`,
      html,
    });
  }

  /**
   * Send badge unlocked notification
   */
  async sendBadgeUnlockedEmail(
    to: string,
    userName: string,
    badgeName: string,
    badgeIcon: string,
    badgeDescription: string,
  ): Promise<void> {
    const html = this.getBadgeUnlockedTemplate(
      userName,
      badgeName,
      badgeIcon,
      badgeDescription,
    );
    await this.sendEmail({
      to,
      subject: `🎖️ You unlocked a new badge: ${badgeName}!`,
      html,
    });
  }

  /**
   * Send password reset email
   */
  async sendPasswordResetEmail(
    to: string,
    resetToken: string,
  ): Promise<void> {
    const resetUrl = `${this.configService.get('FRONTEND_URL')}/reset-password?token=${resetToken}`;
    const html = this.getPasswordResetTemplate(resetUrl);
    await this.sendEmail({
      to,
      subject: 'Reset your FREE LMS password',
      html,
    });
  }

  /**
   * Send reminder email for incomplete courses
   */
  async sendCourseReminderEmail(
    to: string,
    userName: string,
    courseTitle: string,
    progress: number,
  ): Promise<void> {
    const html = this.getCourseReminderTemplate(userName, courseTitle, progress);
    await this.sendEmail({
      to,
      subject: `Continue learning: ${courseTitle}`,
      html,
    });
  }

  // Email Templates

  private getWelcomeTemplate(name: string): string {
    return `
<!DOCTYPE html>
<html>
<head>
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 40px 20px; text-align: center; border-radius: 10px 10px 0 0; }
        .content { background: #ffffff; padding: 30px; border: 1px solid #e0e0e0; }
        .button { display: inline-block; padding: 12px 30px; background: #667eea; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🎓 Welcome to FREE LMS!</h1>
        </div>
        <div class="content">
            <h2>Hi ${name}! 👋</h2>
            <p>Welcome to FREE LMS - your gateway to unlimited learning opportunities!</p>
            <p>We're excited to have you join our community of learners. Here's what you can do:</p>
            <ul>
                <li>📚 Browse our extensive course catalog</li>
                <li>🎯 Track your learning progress</li>
                <li>🏆 Earn badges and certificates</li>
                <li>📈 Climb the leaderboard</li>
            </ul>
            <center>
                <a href="${this.configService.get('FRONTEND_URL')}/courses" class="button">Explore Courses</a>
            </center>
            <p>If you have any questions, feel free to reach out to our support team.</p>
            <p>Happy learning! 🚀</p>
        </div>
        <div class="footer">
            <p>© 2024 FREE LMS. All rights reserved.</p>
            <p>You received this email because you registered on FREE LMS.</p>
        </div>
    </div>
</body>
</html>
    `;
  }

  private getEnrollmentTemplate(userName: string, courseTitle: string): string {
    return `
<!DOCTYPE html>
<html>
<head>
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .header { background: #4CAF50; color: white; padding: 30px 20px; text-align: center; border-radius: 10px 10px 0 0; }
        .content { background: #ffffff; padding: 30px; border: 1px solid #e0e0e0; }
        .button { display: inline-block; padding: 12px 30px; background: #4CAF50; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>✓ Enrollment Confirmed!</h1>
        </div>
        <div class="content">
            <h2>Hi ${userName}!</h2>
            <p>Great news! You've successfully enrolled in:</p>
            <h3>${courseTitle}</h3>
            <p>You can now start learning at your own pace. Don't forget to:</p>
            <ul>
                <li>Complete all lessons to earn your certificate</li>
                <li>Pass quizzes to test your knowledge</li>
                <li>Track your progress in your dashboard</li>
            </ul>
            <center>
                <a href="${this.configService.get('FRONTEND_URL')}/my-courses" class="button">Start Learning</a>
            </center>
        </div>
    </div>
</body>
</html>
    `;
  }

  private getCourseCompletionTemplate(
    userName: string,
    courseTitle: string,
    certificateUrl?: string,
  ): string {
    return `
<!DOCTYPE html>
<html>
<head>
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .header { background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%); color: white; padding: 40px 20px; text-align: center; border-radius: 10px 10px 0 0; }
        .content { background: #ffffff; padding: 30px; border: 1px solid #e0e0e0; }
        .button { display: inline-block; padding: 12px 30px; background: #FF6B6B; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🎉 Congratulations!</h1>
        </div>
        <div class="content">
            <h2>Amazing work, ${userName}!</h2>
            <p>You've successfully completed:</p>
            <h3>${courseTitle}</h3>
            <p>This is a significant achievement! Your dedication and hard work have paid off.</p>
            ${certificateUrl ? `<p>Your certificate is ready! Download it and share your achievement with the world.</p>` : ''}
            <center>
                ${certificateUrl ? `<a href="${certificateUrl}" class="button">Download Certificate</a>` : ''}
            </center>
            <p>Keep up the great work and continue your learning journey!</p>
        </div>
    </div>
</body>
</html>
    `;
  }

  private getCertificateTemplate(
    userName: string,
    courseTitle: string,
    certificateNumber: string,
    certificateUrl: string,
  ): string {
    return `
<!DOCTYPE html>
<html>
<head>
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 40px 20px; text-align: center; border-radius: 10px 10px 0 0; }
        .content { background: #ffffff; padding: 30px; border: 1px solid #e0e0e0; }
        .certificate-box { background: #f5f7fa; padding: 20px; border-radius: 8px; margin: 20px 0; text-align: center; }
        .button { display: inline-block; padding: 12px 30px; background: #667eea; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🏆 Your Certificate is Ready!</h1>
        </div>
        <div class="content">
            <h2>Congratulations, ${userName}!</h2>
            <div class="certificate-box">
                <h3>${courseTitle}</h3>
                <p><strong>Certificate Number:</strong> ${certificateNumber}</p>
            </div>
            <p>Your certificate of completion is now available for download.</p>
            <p>Share it on LinkedIn, add it to your resume, or frame it on your wall!</p>
            <center>
                <a href="${certificateUrl}" class="button">Download Certificate</a>
            </center>
        </div>
    </div>
</body>
</html>
    `;
  }

  private getBadgeUnlockedTemplate(
    userName: string,
    badgeName: string,
    badgeIcon: string,
    badgeDescription: string,
  ): string {
    return `
<!DOCTYPE html>
<html>
<head>
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .header { background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%); color: white; padding: 40px 20px; text-align: center; border-radius: 10px 10px 0 0; }
        .content { background: #ffffff; padding: 30px; border: 1px solid #e0e0e0; }
        .badge-box { background: #f5f7fa; padding: 30px; border-radius: 8px; margin: 20px 0; text-align: center; }
        .badge-icon { font-size: 64px; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🎖️ Badge Unlocked!</h1>
        </div>
        <div class="content">
            <h2>Great job, ${userName}!</h2>
            <div class="badge-box">
                <div class="badge-icon">${badgeIcon}</div>
                <h3>${badgeName}</h3>
                <p>${badgeDescription}</p>
            </div>
            <p>You've unlocked a new achievement! Keep up the excellent work!</p>
        </div>
    </div>
</body>
</html>
    `;
  }

  private getPasswordResetTemplate(resetUrl: string): string {
    return `
<!DOCTYPE html>
<html>
<head>
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .content { background: #ffffff; padding: 30px; border: 1px solid #e0e0e0; }
        .button { display: inline-block; padding: 12px 30px; background: #667eea; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
    </style>
</head>
<body>
    <div class="container">
        <div class="content">
            <h2>Password Reset Request</h2>
            <p>You requested to reset your password. Click the button below to proceed:</p>
            <center>
                <a href="${resetUrl}" class="button">Reset Password</a>
            </center>
            <p>If you didn't request this, please ignore this email.</p>
            <p>This link will expire in 1 hour.</p>
        </div>
    </div>
</body>
</html>
    `;
  }

  private getCourseReminderTemplate(
    userName: string,
    courseTitle: string,
    progress: number,
  ): string {
    return `
<!DOCTYPE html>
<html>
<head>
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .content { background: #ffffff; padding: 30px; border: 1px solid #e0e0e0; }
        .progress-bar { background: #e0e0e0; height: 20px; border-radius: 10px; overflow: hidden; margin: 20px 0; }
        .progress-fill { background: #4CAF50; height: 100%; }
        .button { display: inline-block; padding: 12px 30px; background: #667eea; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
    </style>
</head>
<body>
    <div class="container">
        <div class="content">
            <h2>Hi ${userName}!</h2>
            <p>We noticed you haven't finished <strong>${courseTitle}</strong> yet.</p>
            <p>You're ${progress.toFixed(0)}% through the course. Keep going!</p>
            <div class="progress-bar">
                <div class="progress-fill" style="width: ${progress}%"></div>
            </div>
            <center>
                <a href="${this.configService.get('FRONTEND_URL')}/my-courses" class="button">Continue Learning</a>
            </center>
        </div>
    </div>
</body>
</html>
    `;
  }
}
