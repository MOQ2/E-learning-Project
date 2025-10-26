import { Component, HostListener, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CourseService } from '../../Services/Courses/course-service';
import { CourseSearchResultDto } from '../../Dtos/CourseSearchResultDto';
import { Router } from '@angular/router';
import { debounceTime, Subject, distinctUntilChanged } from 'rxjs';

@Component({
  selector: 'app-search-bar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './search-bar.html',
  styleUrls: ['./search-bar.css']
})
export class SearchBarComponent implements OnInit {
  isOpen = false;
  searchQuery = '';
  searchResults: CourseSearchResultDto[] = [];
  allResults: CourseSearchResultDto[] = []; // Store all results for filtering
  isLoading = false;
  selectedIndex = -1;
  private searchSubject = new Subject<string>();

  // Filter states
  activeFilter: string = 'all'; // 'all', 'courses', 'instructors', 'short', 'popular'

  quickFilters = [
    { label: 'All results', value: 'all' },
    { label: 'Courses', value: 'courses' },
    { label: 'Instructors', value: 'instructors' },
    { label: '< 2 hours', value: 'short' },
    { label: 'Popular', value: 'popular' }
  ];

  constructor(
    private courseService: CourseService,
    private router: Router
  ) {}

  ngOnInit() {
    console.log('SearchBarComponent initialized');
    // Debounce search input
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(query => {
      this.performSearch(query);
    });
  }

  openSearch() {
    console.log('Search opened!', 'isOpen before:', this.isOpen);
    this.isOpen = true;
    console.log('Search opened!', 'isOpen after:', this.isOpen);
    setTimeout(() => {
      const input = document.querySelector('.search-input') as HTMLInputElement;
      if (input) {
        input.focus();
        console.log('Input focused');
      } else {
        console.log('Input not found!');
      }
    }, 100);
  }

  closeSearch() {
    this.isOpen = false;
    this.searchQuery = '';
    this.searchResults = [];
    this.allResults = [];
    this.selectedIndex = -1;
    this.activeFilter = 'all'; // Reset filter when closing
  }

  onSearchInput(event: Event) {
    const query = (event.target as HTMLInputElement).value;
    this.searchQuery = query;

    if (query.trim().length === 0) {
      this.searchResults = [];
      this.isLoading = false;
      return;
    }

    this.isLoading = true;
    this.searchSubject.next(query);
  }

  performSearch(query: string) {
    if (!query.trim()) {
      this.searchResults = [];
      this.allResults = [];
      this.isLoading = false;
      return;
    }

    this.courseService.searchCourses(query).subscribe({
      next: (results) => {
        this.allResults = results;
        this.applyFilter(this.activeFilter); // Apply current filter to results
        this.isLoading = false;
        this.selectedIndex = -1;
      },
      error: (err) => {
        console.error('Search error:', err);
        this.isLoading = false;
        this.searchResults = [];
        this.allResults = [];
      }
    });
  }

  navigateToCourse(courseId: number) {
    this.router.navigate(['/course', courseId]);
    this.closeSearch();
  }

  applyFilter(filterType: string) {
    this.activeFilter = filterType;

    const baseResults = Array.isArray(this.allResults) ? [...this.allResults] : [];

    switch(filterType) {
      case 'all':
        this.searchResults = baseResults;
        break;
      case 'courses':
        // Highlight results that represent course matches (exclude pure instructor hits if provided)
        const courseCandidates = baseResults.filter(result => !this.isInstructorMatch(result));
        this.searchResults = courseCandidates.length ? courseCandidates : baseResults;
        break;
      case 'instructors':
        const instructorMatches = baseResults.filter(result => this.isInstructorMatch(result));
        const fallbackInstructorPool = instructorMatches.length
          ? instructorMatches
          : baseResults.filter(result => (result.instructor ?? '').trim().length > 0);

        this.searchResults = fallbackInstructorPool.sort((a, b) => {
          const instructorCompare = (a.instructor || '').localeCompare(b.instructor || '');
          if (instructorCompare !== 0) {
            return instructorCompare;
          }

          return (a.name || '').localeCompare(b.name || '');
        });
        break;
      case 'short':
        // Filter courses less than 2 hours
        const shortCourses = baseResults.filter(course =>
          typeof course.estimatedDurationInHours === 'number' && course.estimatedDurationInHours > 0 && course.estimatedDurationInHours < 2
        );
        this.searchResults = shortCourses;
        break;
      case 'popular':
        // Sort by popularity (we can use lessonCount as a proxy for now)
        this.searchResults = baseResults.sort((a, b) =>
          (b.lessonCount || 0) - (a.lessonCount || 0)
        );
        break;
      default:
        this.searchResults = baseResults;
    }

    this.selectedIndex = -1;
  }

  setFilter(filterType: string) {
    this.applyFilter(filterType);
  }

  getCourseSubtitle(course: CourseSearchResultDto): string {
    const parts: string[] = ['Course'];
    if (course.lessonCount) {
      parts.push(`${course.lessonCount} ${course.lessonCount === 1 ? 'lesson' : 'lessons'}`);
    }
    if (course.instructor) {
      parts.push(`by ${course.instructor}`);
    }
    return parts.join(' • ');
  }

  getActionLabel(): string {
    return 'Open';
  }

  @HostListener('document:keydown.escape')
  onEscapePress() {
    if (this.isOpen) {
      this.closeSearch();
    }
  }

  @HostListener('document:keydown', ['$event'])
  onKeyDown(event: KeyboardEvent) {
    if (!this.isOpen) return;

    if (event.key === 'ArrowDown') {
      event.preventDefault();
      this.selectedIndex = Math.min(this.selectedIndex + 1, this.searchResults.length - 1);
    } else if (event.key === 'ArrowUp') {
      event.preventDefault();
      this.selectedIndex = Math.max(this.selectedIndex - 1, -1);
    } else if (event.key === 'Enter' && this.selectedIndex >= 0 && this.selectedIndex < this.searchResults.length) {
      event.preventDefault();
      this.navigateToCourse(this.searchResults[this.selectedIndex].id);
    }
  }

  @HostListener('document:click', ['$event'])
  onClickOutside(event: MouseEvent) {
    const target = event.target as HTMLElement;
    // Don't close if clicking the search button
    if (target.closest('.search-trigger-btn')) {
      return;
    }
    if (this.isOpen && !target.closest('.search-box')) {
      this.closeSearch();
    }
  }

  getMatchTypeLabel(matchType: string): string {
    const labels: { [key: string]: string } = {
      'title_exact': 'Exact Match',
      'title': 'Title Match',
      'tag': 'Tag Match',
      'category': 'Category Match',
      'description': 'Description Match',
      'other': 'Match'
    };
    return labels[matchType] || 'Match';
  }

  formatDuration(hours: number): string {
    if (hours < 1) {
      return `${Math.round(hours * 60)} mins`;
    }
    return `${hours} ${hours === 1 ? 'hour' : 'hours'}`;
  }

  private isInstructorMatch(result: CourseSearchResultDto): boolean {
    const matchType = (result.matchType ?? '').toLowerCase();
    if (matchType.includes('instructor') || matchType.includes('teacher') || matchType.includes('author')) {
      return true;
    }

    const instructorName = (result.instructor ?? '').trim().toLowerCase();
    if (!instructorName) {
      return false;
    }

    const normalizedQuery = this.searchQuery.trim().toLowerCase();
    return normalizedQuery.length > 0 && instructorName.includes(normalizedQuery);
  }
}
