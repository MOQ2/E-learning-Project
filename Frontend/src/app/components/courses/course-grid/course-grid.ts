import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CourseCardComponent } from '../course-card/course-card';
import { CommonModule } from '@angular/common';
import { CourseDto as Course } from '../../../Dtos/CourseDto';

@Component({
  selector: 'app-course-grid',
  templateUrl: './course-grid.html',
  styleUrls: ['./course-grid.css'],
  imports: [CourseCardComponent , CommonModule],
  standalone: true
})
export class CourseGridComponent {
  @Output() currentPageChange = new EventEmitter<number>();
  @Output() enrollClick = new EventEmitter<{ course: Course, enrolled: boolean }>();
  @Output() saveClick = new EventEmitter<{ course: Course, saved: boolean }>();


  @Input() totalPages: number = 5;
  @Input() totalResults: number = 124;
  @Input() courses: Course[] = [];

  private _currentPage = 1;

  @Input()
  set currentPage(value: number) {
    if (this._currentPage !== value) {
      this._currentPage = value;
      this.currentPageChange.emit(this._currentPage);
    }
  }
  get currentPage(): number {
    return this._currentPage;
  }


  topFilters = [
    { name: 'Trending', active: true, icon: '' },
    { name: 'Top Rated', active: false, icon: 'icons/badge-svgrepo-com.svg' },
    { name: 'New', active: false, icon: 'icons/flame-svgrepo-com.svg' },
    { name: 'Under 2h', active: false, icon: 'icons/clock-three-svgrepo-com.svg' }
  ];



  onSurpriseMe() {
    console.log('Surprise me clicked');
  }

  onRefineFilters() {
    console.log('Refine filters clicked');
  }

  toggleTopFilter(filter: any) {
    this.topFilters.forEach(f => f.active = false);
    filter.active = true;
  }

  onMoreFilters() {
    console.log('More filters clicked');
  }



  goToPage(page: number) {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      this.currentPageChange.emit(this.currentPage);
    }
  }

  goToPageAndScroll(page: number) {
    this.goToPage(page);
    window.scrollTo({ top: 300, behavior: 'smooth' });
  }

  getPages(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
    }

  onCourseEnrollClick(event: { course: Course, enrolled: boolean }) {
      this.enrollClick.emit(event);
  }

  onCourseSaveClick(event: { course: Course, saved: boolean }) {
    this.saveClick.emit(event);
  }


  trackByCourseId(index: number, course: Course): number {
    return course.id;
  }


  getVisiblePages(): number[] {
    const pages: number[] = [];
    const start = Math.max(1, this.currentPage - 2);
    const end = Math.min(this.totalPages, this.currentPage + 2);

    for (let i = start; i <= end; i++) {
      pages.push(i);
    }

    return pages;
  }



}
