import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { CourseDto as Course } from '../../../Dtos/CourseDto';
import { environment } from '../../../../environments/environment';


@Component({
  selector: 'app-course-card',
  templateUrl: './course-card.html',
  styleUrls: ['./course-card.css'],
  standalone: true,
  imports: [CommonModule]
})
export class CourseCardComponent {
  @Input() course!: Course;
  @Input() isEnrolled = false;
  @Input() isSaved = false;


  @Output() enrollClick = new EventEmitter<{ course: Course, enrolled: boolean }>();
  @Output() saveClick = new EventEmitter<{ course: Course, saved: boolean }>();

  private readonly fallbackImage = '/mortarboard_1940690.png';

  constructor(private router: Router) {}

  onCardClick(event: Event) {
    // Navigate to course view page
    this.router.navigate(['/course', this.course.id]);
  }

  onEnroll(event: Event) {
    event.stopPropagation(); // Prevent card click
    this.isEnrolled = !this.isEnrolled;
    this.enrollClick.emit({ course: this.course, enrolled: this.isEnrolled });
  }

  onSave(event: Event) {
    event.stopPropagation(); // Prevent card click
    this.isSaved = !this.isSaved;
    this.saveClick.emit({ course: this.course, saved: this.isSaved });
  }

  getThumbnailSrc(): string {
    if (!this.course) {
      return this.fallbackImage;
    }
    if (this.course.thumbnailUrl) {
      return this.course.thumbnailUrl;
    }
    const rawThumbnail = this.course.thumbnail;
    const thumbnailId = typeof rawThumbnail === 'number' ? rawThumbnail : Number(rawThumbnail);
    if (!Number.isNaN(thumbnailId) && thumbnailId > 0) {
      return `${environment.apiUrl}/api/attachments/${thumbnailId}/download`;
    }
    return this.fallbackImage;
  }

  hasRating(): boolean {
    const value = this.course?.averageRating;
    return value != null && value > 0;
  }

  getRatingValue(): string {
    if (!this.hasRating()) {
      return 'No reviews yet';
    }
    return (this.course?.averageRating ?? 0).toFixed(1);
  }

  getReviewLabel(): string {
    const count = this.course?.reviewCount ?? 0;
    if (count <= 0) {
      return 'Be the first to review';
    }
    if (count === 1) {
      return '1 review';
    }
    return `${this.formatCount(count)} reviews`;
  }

  getLearnerLabel(): string {
    const learners = this.course?.enrolledCount ?? 0;
    if (learners <= 0) {
      return 'New course';
    }
    return `${this.formatCount(learners)} learners`;
  }

  formatCategory(category?: string | null): string {
    if (!category) {
      return '';
    }
    return category
      .toLowerCase()
      .split('_')
      .map(segment => segment.charAt(0).toUpperCase() + segment.slice(1))
      .join(' ');
  }

  formatPrice(value?: number | null, currency?: string | null): string {
    if (value == null || value === 0) {
      return 'Free';
    }
    const currencyCode = (currency ?? 'USD').toUpperCase();
    try {
      return new Intl.NumberFormat(undefined, {
        style: 'currency',
        currency: currencyCode,
        maximumFractionDigits: 2
      }).format(value);
    } catch (err) {
      return `${currencyCode} ${value.toFixed(2)}`;
    }
  }

  private formatCount(value: number): string {
    if (value >= 1_000_000) {
      return `${(value / 1_000_000).toFixed(1).replace(/\.0$/, '')}M`;
    }
    if (value >= 1_000) {
      return `${(value / 1_000).toFixed(1).replace(/\.0$/, '')}K`;
    }
    return value.toString();
  }
}
