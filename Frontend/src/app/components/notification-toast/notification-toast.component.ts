import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService, Notification } from '../../Services/notification.service';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-notification-toast',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="notification-container">
      <div
        *ngFor="let notification of notifications$ | async"
        class="notification-toast"
        [ngClass]="'notification-' + notification.type"
        [@slideIn]>
        <div class="notification-icon">
          <span *ngIf="notification.type === 'success'">✓</span>
          <span *ngIf="notification.type === 'error'">✕</span>
          <span *ngIf="notification.type === 'info'">ⓘ</span>
          <span *ngIf="notification.type === 'loading'" class="spinner"></span>
        </div>
        <div class="notification-message">{{ notification.message }}</div>
        <button
          *ngIf="notification.type !== 'loading'"
          class="notification-close"
          (click)="close(notification.id)">
          ×
        </button>
      </div>
    </div>
  `,
  styles: [`
    .notification-container {
      position: fixed;
      top: 80px;
      right: 20px;
      z-index: 9999;
      display: flex;
      flex-direction: column;
      gap: 12px;
      max-width: 400px;
    }

    .notification-toast {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 16px 20px;
      border-radius: 12px;
      box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
      backdrop-filter: blur(10px);
      animation: slideInRight 0.3s cubic-bezier(0.68, -0.55, 0.265, 1.55);
      min-width: 300px;
      position: relative;
      overflow: hidden;
    }

    @keyframes slideInRight {
      from {
        transform: translateX(400px);
        opacity: 0;
      }
      to {
        transform: translateX(0);
        opacity: 1;
      }
    }

    .notification-toast::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      bottom: 0;
      width: 4px;
    }

    .notification-success {
      background: linear-gradient(135deg, #d4edda 0%, #c3e6cb 100%);
      color: #155724;
      border: 1px solid #c3e6cb;
    }

    .notification-success::before {
      background: #28a745;
    }

    .notification-error {
      background: linear-gradient(135deg, #f8d7da 0%, #f5c6cb 100%);
      color: #721c24;
      border: 1px solid #f5c6cb;
    }

    .notification-error::before {
      background: #dc3545;
    }

    .notification-info {
      background: linear-gradient(135deg, #d1ecf1 0%, #bee5eb 100%);
      color: #0c5460;
      border: 1px solid #bee5eb;
    }

    .notification-info::before {
      background: #17a2b8;
    }

    .notification-loading {
      background: linear-gradient(135deg, #e7f3ff 0%, #cfe2ff 100%);
      color: #004085;
      border: 1px solid #b8daff;
    }

    .notification-loading::before {
      background: #007bff;
    }

    .notification-icon {
      font-size: 20px;
      font-weight: bold;
      flex-shrink: 0;
      width: 24px;
      height: 24px;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .spinner {
      width: 16px;
      height: 16px;
      border: 2px solid rgba(0, 64, 133, 0.3);
      border-top-color: #004085;
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
    }

    @keyframes spin {
      to { transform: rotate(360deg); }
    }

    .notification-message {
      flex: 1;
      font-size: 14px;
      font-weight: 500;
      line-height: 1.4;
    }

    .notification-close {
      background: none;
      border: none;
      font-size: 24px;
      cursor: pointer;
      opacity: 0.6;
      transition: opacity 0.2s;
      padding: 0;
      width: 24px;
      height: 24px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }

    .notification-close:hover {
      opacity: 1;
    }

    @media (max-width: 640px) {
      .notification-container {
        right: 10px;
        left: 10px;
        max-width: none;
      }

      .notification-toast {
        min-width: auto;
      }
    }
  `]
})
export class NotificationToastComponent implements OnInit {
  notifications$: Observable<Notification[]>;

  constructor(private notificationService: NotificationService) {
    this.notifications$ = this.notificationService.notifications$;
  }

  ngOnInit(): void {}

  close(id: string) {
    this.notificationService.remove(id);
  }
}
