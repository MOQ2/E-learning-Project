import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MyCoursesService } from '../../Services/MyCourses/my-courses.service';
import {
  MyCoursesResponseDto,
  TeacherCourseDto,
  TeacherStatsDto
} from '../../Dtos/MyCourseDtos';
import { NavBar } from '../nav-bar/nav-bar';

@Component({
  selector: 'app-my-courses-page',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, NavBar],
  templateUrl: './my-courses-page.html',
  styleUrl: './my-courses-page.css'
})
export class MyCoursesPage implements OnInit {
  teacherId: number = 1; // TODO: Get from auth service
  loading: boolean = true;
  error: string | null = null;

  // Dashboard data
  stats: TeacherStatsDto | null = null;
  courses: TeacherCourseDto[] = [];
  recentlyUpdated: TeacherCourseDto[] = [];
  topPerforming: TeacherCourseDto[] = [];

  // Filter and sort options
  filterStatus: 'all' | 'published' | 'draft' | 'active' = 'all';
  sortBy: 'recent' | 'enrollments' | 'name' | 'revenue' = 'recent';
  searchQuery: string = '';
  showSortMenu: boolean = false;
  showFilterMenu: boolean = false;

  constructor(
    private myCoursesService: MyCoursesService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.loading = true;
    this.error = null;

    this.myCoursesService.getMyCoursesDashboard().subscribe({
      next: (data: MyCoursesResponseDto) => {
        this.stats = data.stats;
        this.courses = data.courses;
        this.recentlyUpdated = data.recentlyUpdatedCourses;
        this.topPerforming = data.topPerformingCourses;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading dashboard:', err);
        this.error = 'Failed to load your courses dashboard. Please try again.';
        this.loading = false;
      }
    });
  }

  get filteredCourses(): TeacherCourseDto[] {
    let courses = [...this.courses];

    // Filter by status
    if (this.filterStatus === 'published') {
      courses = courses.filter(c => c.isPublished);
    } else if (this.filterStatus === 'draft') {
      courses = courses.filter(c => !c.isPublished);
    } else if (this.filterStatus === 'active') {
      courses = courses.filter(c => c.isActive && c.isPublished);
    }

    // Filter by search query
    if (this.searchQuery.trim()) {
      const query = this.searchQuery.toLowerCase();
      courses = courses.filter(c =>
        c.courseName.toLowerCase().includes(query) ||
        c.description?.toLowerCase().includes(query) ||
        c.category?.toLowerCase().includes(query)
      );
    }

    // Sort
    switch (this.sortBy) {
      case 'recent':
        courses.sort((a, b) =>
          new Date(b.updatedAt || b.createdAt).getTime() -
          new Date(a.updatedAt || a.createdAt).getTime()
        );
        break;
      case 'enrollments':
        courses.sort((a, b) => b.totalEnrollments - a.totalEnrollments);
        break;
      case 'revenue':
        courses.sort((a, b) => (b.totalRevenue || 0) - (a.totalRevenue || 0));
        break;
      case 'name':
        courses.sort((a, b) => a.courseName.localeCompare(b.courseName));
        break;
    }

    return courses;
  }

  get topRecentlyUpdated(): TeacherCourseDto[] {
    return this.recentlyUpdated.slice(0, 3);
  }

  get topPerformingList(): TeacherCourseDto[] {
    return this.topPerforming.slice(0, 3);
  }

  viewCourse(course: TeacherCourseDto): void {
    this.router.navigate(['/course', course.courseId, 'enrolled']);
  }

  editCourse(course: TeacherCourseDto): void {
    this.router.navigate(['/courses', 'editor', course.courseId]);
  }

  getStatusBadgeClass(status: string | undefined): string {
    if (!status) return 'status-badge-draft';
    switch (status.toLowerCase()) {
      case 'published':
        return 'status-badge-published';
      case 'draft':
        return 'status-badge-draft';
      case 'archived':
        return 'status-badge-archived';
      default:
        return 'status-badge-draft';
    }
  }

  formatDate(date: string | undefined): string {
    if (!date) return 'N/A';
    const d = new Date(date);
    return d.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
  }

  formatDuration(minutes: number | undefined): string {
    if (!minutes) return '0m';
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    if (hours === 0) return `${mins}m`;
    if (mins === 0) return `${hours}h`;
    return `${hours}h ${mins}m`;
  }

  formatCurrency(amount: number | undefined): string {
    if (!amount) return '$0';
    return `$${amount.toFixed(2)}`;
  }

  toggleSortMenu(): void {
    this.showSortMenu = !this.showSortMenu;
    if (this.showSortMenu) {
      this.showFilterMenu = false;
    }
  }

  toggleFilterMenu(): void {
    this.showFilterMenu = !this.showFilterMenu;
    if (this.showFilterMenu) {
      this.showSortMenu = false;
    }
  }

  getSortLabel(): string {
    switch (this.sortBy) {
      case 'recent':
        return 'Most Recent';
      case 'enrollments':
        return 'Most Enrollments';
      case 'revenue':
        return 'Highest Revenue';
      case 'name':
        return 'Name (A-Z)';
      default:
        return 'Sort By';
    }
  }

  onSearchChange(): void {
    // Debouncing could be added here if needed
  }

  getThumbnailUrl(thumbnailUrl: string | undefined): string {
    if (!thumbnailUrl) {
      return 'assets/default-course.jpg';
    }
    
    return `${thumbnailUrl}/download`;
  }

  createNewCourse(): void {
    this.router.navigate(['/courses', 'editor', 'new']);
  }
}
