import {Component, OnDestroy, OnInit} from '@angular/core';
import {CourseInterface, CourseService} from '../Services/Courses/course-service';
import {Router} from '@angular/router';
import {Subject, takeUntil} from 'rxjs';

@Component({
  selector: 'app-courses',
  imports: [],
  templateUrl: './courses.html',
  styleUrl: './courses.css'
})
export class Courses implements OnInit , OnDestroy{
  courses: CourseInterface[] = [];
private destroy$ = new Subject<void>();
  constructor(private courseService: CourseService, private router: Router) {
  }

  ngOnInit(): void {
    this.courseService.getCourses().pipe(takeUntil(this.destroy$))
      .subscribe(
      {
        next: (res) => {
          this.courses = res;
        },
        error: (err) => {
          console.log(err);
        }
      }
    )
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  goToPayment(course: CourseInterface) {
    this.router.navigate(['/payment'],
      { queryParams: { courseId: course.id, price: course.oneTimePrice } }
    );
  }
}
