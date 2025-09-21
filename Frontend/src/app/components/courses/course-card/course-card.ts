import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Course } from '../course-grid/course-grid';
import { CommonModule } from '@angular/common';
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

  onEnroll() {
    this.isEnrolled = !this.isEnrolled;
    console.log(`${this.isEnrolled ? 'Enrolled in' : 'Unenrolled from'} ${this.course.title}`);
    this.enrollClick.emit({ course: this.course, enrolled: this.isEnrolled });
  }

  onSave() {
    this.isSaved = !this.isSaved;
    this.saveClick.emit({ course: this.course, saved: this.isSaved });
  }

  generateStars(): string[] {
    const fullStars = Math.floor(this.course.rating);
    const hasHalfStar = this.course.rating % 1 !== 0;
    const stars = [];

    for (let i = 0; i < fullStars; i++) {
      stars.push('⭐');
    }
    if (hasHalfStar) {
      stars.push('<i class="bi bi-star-half"></i>');
    }

    return stars;
  }
}
