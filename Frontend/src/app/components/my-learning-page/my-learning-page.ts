import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MyLearningService } from '../../Services/MyLearning/my-learning.service';
import {
  MyLearningResponseDto,
  EnrolledCourseDto,
  MyLearningStatsDto
} from '../../Dtos/MyLearningDtos';
import { NavBar } from '../nav-bar/nav-bar';

@Component({
  selector: 'app-my-learning-page',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, NavBar],
  templateUrl: './my-learning-page.html',
  styleUrl: './my-learning-page.css'
})
export class MyLearningPage implements OnInit {
  loading: boolean = true;
  error: string | null = null;

  // Dashboard data
  stats: MyLearningStatsDto | null = null;
  enrolledCourses: EnrolledCourseDto[] = [];
  continueLearning: EnrolledCourseDto[] = [];
  upcomingDeadlines: EnrolledCourseDto[] = [];

  // Filter and sort options
  selectedTab: 'all' | 'in-progress' | 'completed' = 'all';
  filterStatus: 'all' | 'active' | 'completed' = 'all';
  sortBy: 'recent' | 'progress' | 'name' = 'recent';
  searchQuery: string = '';
  showSortMenu: boolean = false;
  showFilterMenu: boolean = false;

  constructor(
    private myLearningService: MyLearningService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.loading = true;
    this.error = null;

    this.myLearningService.getMyLearningDashboard().subscribe({
      next: (data: MyLearningResponseDto) => {
        this.stats = data.stats;
        this.enrolledCourses = data.enrolledCourses;
        this.continueLearning = data.continueLearning;
        this.upcomingDeadlines = data.upcomingDeadlines;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading dashboard:', err);
        this.error = 'Failed to load your learning dashboard. Please try again.';
        this.loading = false;
      }
    });
  }

  get filteredCourses(): EnrolledCourseDto[] {
    let courses = [...this.enrolledCourses];

    // Filter by status
    if (this.filterStatus === 'active') {
      courses = courses.filter(c => c.progressPercentage > 0 && c.progressPercentage < 100);
    } else if (this.filterStatus === 'completed') {
      courses = courses.filter(c => c.progressPercentage >= 100);
    }

    // Filter by search query
    if (this.searchQuery.trim()) {
      const query = this.searchQuery.toLowerCase();
      courses = courses.filter(c =>
        c.courseName.toLowerCase().includes(query) ||
        c.description?.toLowerCase().includes(query) ||
        c.instructor?.toLowerCase().includes(query) ||
        c.category?.toLowerCase().includes(query)
      );
    }

    // Sort
    switch (this.sortBy) {
      case 'recent':
        courses.sort((a, b) =>
          new Date(b.lastAccessedDate || b.enrolledDate).getTime() -
          new Date(a.lastAccessedDate || a.enrolledDate).getTime()
        );
        break;
      case 'progress':
        courses.sort((a, b) => b.progressPercentage - a.progressPercentage);
        break;
      case 'name':
        courses.sort((a, b) => a.courseName.localeCompare(b.courseName));
        break;
    }

    return courses;
  }

  get topContinueLearning(): EnrolledCourseDto[] {
    return this.continueLearning.slice(0, 3);
  }

  get completedCoursesList(): EnrolledCourseDto[] {
    return this.enrolledCourses
      .filter(course => course.progressPercentage >= 100)
      .sort(
        (a, b) =>
          new Date(b.lastAccessedDate || b.enrolledDate).getTime() -
          new Date(a.lastAccessedDate || a.enrolledDate).getTime()
      )
      .slice(0, 3);
  }

  goToCourse(course: EnrolledCourseDto): void {
    this.router.navigate(['/course', course.courseId, 'enrolled']);
  }

  continueCourse(course: EnrolledCourseDto): void {
    this.router.navigate(['/course', course.courseId, 'learn']);
  }

  getProgressBarColor(progress: number): string {
    if (progress < 30) return '#ef4444';
    if (progress < 70) return '#f59e0b';
    return '#10b981';
  }

  getDaysRemainingColor(days: number | undefined): string {
    if (!days) return '#6b7280';
    if (days < 7) return '#ef4444';
    if (days < 30) return '#f59e0b';
    return '#10b981';
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
      case 'progress':
        return 'Progress';
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
    // Add /download to the thumbnail URL to get the image correctly
    return `${thumbnailUrl}/download`;
  }

  navigateToCourse(courseId: number): void {
    this.router.navigate(['/course', courseId, 'enrolled']);
  }

  resumeCourse(course: EnrolledCourseDto): void {
    // Navigate to the lesson page to continue learning
    // If there's a current module and lesson, navigate to that specific lesson
    if (course.currentModule && course.currentLesson) {
      // Navigate to the specific lesson within the course
      this.router.navigate(['/course', course.courseId, 'lesson'], {
        queryParams: {
          module: course.currentModule,
          lesson: course.currentLesson
        }
      });
    } else {
      // If no current lesson, navigate to the enrolled course page (which will show the first incomplete lesson)
      this.router.navigate(['/course', course.courseId, 'enrolled']);
    }
  }

  browseCourses(): void {
    this.router.navigate(['/courses']);
  }
}
