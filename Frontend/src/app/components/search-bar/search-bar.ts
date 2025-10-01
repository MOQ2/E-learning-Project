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

    switch(filterType) {
      case 'all':
        this.searchResults = [...this.allResults];
        break;
      case 'courses':
        // Show all courses (default behavior)
        this.searchResults = [...this.allResults];
        break;
      case 'instructors':
        // Filter by unique instructors - keep one course per instructor
        const instructorMap = new Map<string, CourseSearchResultDto>();
        this.allResults.forEach(course => {
          if (!instructorMap.has(course.instructor)) {
            instructorMap.set(course.instructor, course);
          }
        });
        this.searchResults = Array.from(instructorMap.values());
        break;
      case 'short':
        // Filter courses less than 2 hours
        this.searchResults = this.allResults.filter(course =>
          course.estimatedDurationInHours && course.estimatedDurationInHours < 2
        );
        break;
      case 'popular':
        // Sort by popularity (we can use lessonCount as a proxy for now)
        this.searchResults = [...this.allResults].sort((a, b) =>
          (b.lessonCount || 0) - (a.lessonCount || 0)
        );
        break;
      default:
        this.searchResults = [...this.allResults];
    }

    this.selectedIndex = -1;
  }

  setFilter(filterType: string) {
    this.applyFilter(filterType);
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
    if (this.isOpen && !target.closest('.searchbar-container')) {
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
}
