import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { Subject, combineLatest, takeUntil } from 'rxjs';
import { NavBar } from '../nav-bar/nav-bar';
import { CourseViewComponent } from '../course-view/course-view.component';

@Component({
  selector: 'app-course-page',
  standalone: true,
  imports: [CommonModule, NavBar, CourseViewComponent],
  templateUrl: './course-page.html',
  styleUrls: ['./course-page.css']
})
export class CoursePage implements OnInit, OnDestroy {
  courseId: number | null = null;
  private readonly destroy$ = new Subject<void>();

  constructor(private route: ActivatedRoute) {
    // initial snapshot for very first render
    this.courseId = this.extractCourseId(
      this.route.snapshot.paramMap.get('id') ?? this.route.snapshot.paramMap.get('courseId'),
      this.route.snapshot.queryParamMap.get('courseId')
    );
  }

  ngOnInit(): void {
    combineLatest([this.route.paramMap, this.route.queryParamMap])
      .pipe(takeUntil(this.destroy$))
      .subscribe(([params, queryParams]) => {
        const idFromParams = params.get('id') ?? params.get('courseId');
        const idFromQuery = queryParams.get('courseId');
        this.courseId = this.extractCourseId(idFromParams, idFromQuery);
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onSubscribe(event: { plan: string; price?: number }) {
    // placeholder: forward to real payment flow or emit app-level event
    console.log('Subscribe clicked', event);
  }

  onPreview() {
    console.log('Preview requested');
  }

  private extractCourseId(paramId: string | null, queryId: string | null): number | null {
    const source = paramId ?? queryId;
    if (!source) {
      return null;
    }

    const parsed = Number(source);
    return Number.isFinite(parsed) && parsed > 0 ? parsed : null;
  }
}
