import {Component} from '@angular/core';
import {CourseInterface, CourseService} from '../Services/Courses/course-service';

@Component({
  selector: 'app-courses',
  imports: [],
  templateUrl: './courses.html',
  styleUrl: './courses.css'
})
export class Courses {
  course: CourseInterface[] = [];

  constructor(private courseService: CourseService) {
  }

  ngOnInit(): void {
    this.courseService.getCourses().subscribe(
      {
        next: (res) => {
          this.course = res;
        },
        error: (err) => {
          console.log(err);
        }
      }
    )
  }
}
