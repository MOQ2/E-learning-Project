import {Component, OnInit} from '@angular/core';
import {CourseInterface, CourseService} from '../Services/Courses/course-service';
import {Router} from '@angular/router';

@Component({
  selector: 'app-courses',
  imports: [],
  templateUrl: './courses.html',
  styleUrl: './courses.css'
})
export class Courses implements OnInit {
  courses: CourseInterface[] = [];

  constructor(private courseService: CourseService, private router: Router) {
  }

  ngOnInit(): void {
    this.courseService.getCourses().subscribe(
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

  goToPayment(course: CourseInterface) {
    this.router.navigate(['/payment'],
      { queryParams: { courseId: course.id, price: course.oneTimePrice } }
    );
  }
}
