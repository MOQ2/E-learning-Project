import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CourseDto as Course } from '../../../Dtos/CourseDto';


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
    console.log(`${this.isEnrolled ? 'Enrolled in' : 'Unenrolled from'} ${this.course.name}`);
    this.enrollClick.emit({ course: this.course, enrolled: this.isEnrolled });
  }

  onSave() {
    this.isSaved = !this.isSaved;
    this.saveClick.emit({ course: this.course, saved: this.isSaved });
  }

  generateStars(): string {
    return this.course.rating ? '⭐ ' + this.course.rating.toFixed(1) : 'No ratings yet';
  }
}
