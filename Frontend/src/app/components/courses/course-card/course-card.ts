import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
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

  constructor(private router: Router) {}

  onCardClick(event: Event) {
    // Navigate to course view page
    this.router.navigate(['/course', this.course.id]);
  }

  onEnroll(event: Event) {
    event.stopPropagation(); // Prevent card click
    this.isEnrolled = !this.isEnrolled;
    console.log(`${this.isEnrolled ? 'Enrolled in' : 'Unenrolled from'} ${this.course.name}`);
    this.enrollClick.emit({ course: this.course, enrolled: this.isEnrolled });
  }

  onSave(event: Event) {
    event.stopPropagation(); // Prevent card click
    this.isSaved = !this.isSaved;
    this.saveClick.emit({ course: this.course, saved: this.isSaved });
  }

  generateStars(): string {
    return this.course.rating ? '⭐ ' + this.course.rating.toFixed(1) : 'No ratings yet';
  }
}
