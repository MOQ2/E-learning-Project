import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { NavBar } from '../nav-bar/nav-bar';
import { CourseViewComponent } from '../course-view/course-view.component';

@Component({
  selector: 'app-course-page',
  standalone: true,
  imports: [CommonModule, NavBar, CourseViewComponent],
  templateUrl: './course-page.html',
  styleUrls: ['./course-page.css']
})
export class CoursePage {
  courseId: number | null = null;

  constructor(private route: ActivatedRoute) {
    // support route param /course/:id and query param ?courseId=
    const idFromParam = this.route.snapshot.paramMap.get('id');
    const idFromQuery = this.route.snapshot.queryParamMap.get('courseId');
    const parsed = idFromParam ?? idFromQuery ?? null;
    this.courseId = parsed ? Number(parsed) : null;
  }

  onSubscribe(event: { plan: string; price?: number }) {
    // placeholder: forward to real payment flow or emit app-level event
    console.log('Subscribe clicked', event);
  }

  onPreview() {
    console.log('Preview requested');
  }
}
