import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface Notification {
  id: string;
  message: string;
  type: 'success' | 'error' | 'info' | 'loading';
  duration?: number;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private notificationsSubject = new BehaviorSubject<Notification[]>([]);
  public notifications$ = this.notificationsSubject.asObservable();

  show(message: string, type: 'success' | 'error' | 'info' | 'loading' = 'info', duration: number = 3000) {
    const id = Math.random().toString(36).substr(2, 9);
    const notification: Notification = { id, message, type, duration };

    const currentNotifications = this.notificationsSubject.value;
    this.notificationsSubject.next([...currentNotifications, notification]);

    if (type !== 'loading' && duration > 0) {
      setTimeout(() => this.remove(id), duration);
    }

    return id;
  }

  success(message: string, duration: number = 3000) {
    return this.show(message, 'success', duration);
  }

  error(message: string, duration: number = 4000) {
    return this.show(message, 'error', duration);
  }

  info(message: string, duration: number = 3000) {
    return this.show(message, 'info', duration);
  }

  warning(message: string, duration: number = 3500) {
    return this.show(message, 'info', duration);
  }

  loading(message: string) {
    return this.show(message, 'loading', 0);
  }

  remove(id: string) {
    const currentNotifications = this.notificationsSubject.value;
    this.notificationsSubject.next(currentNotifications.filter(n => n.id !== id));
  }

  clear() {
    this.notificationsSubject.next([]);
  }
}
