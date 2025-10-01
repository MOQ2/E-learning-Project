import { Component, OnInit, OnDestroy } from '@angular/core';
import { SidebarComponent } from '../sidebare/sidebar';
import { NavBar } from '../../nav-bar/nav-bar';
import { Footer } from '../../footer/footer';
import { CourseGridComponent } from '../course-grid/course-grid';
import { CourseService } from '../../../Services/Courses/course-service';
import { CourseFilterParams } from '../../../Dtos/CourseFilterParams';
import { CourseDto } from '../../../Dtos/CourseDto';
import { TagDto } from '../../../Dtos/TagDto';
import { Subject, takeUntil, debounceTime, distinctUntilChanged, switchMap, of, catchError, tap } from 'rxjs';
import { CommonModule } from '@angular/common';
import { NotificationService } from '../../../Services/notification.service';

@Component({
  selector: 'app-explore-courses',
  imports: [SidebarComponent, NavBar, Footer, CourseGridComponent, CommonModule],
  templateUrl: './explore-courses.html',
  styleUrls: ['./explore-courses.css'],
  standalone: true
})
export class ExploreCoursesPage implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  private loadCoursesSubject$ = new Subject<CourseFilterParams>();
  private filterChangeSubject$ = new Subject<void>();

  // Loading states
  isLoading = false;
  error: string | null = null;

  // Course data
  courses: CourseDto[] = [];
  totalPages = 0;
  totalResults = 0;
  currentPage = 1;
  pageSize = 20;

  // Filter data
  categories: TagDto[] = [];

  // Fixed categories from backend enum
  fixedCategories = [
    { name: 'PROGRAMMING', displayName: 'Programming', selected: false },
    { name: 'DATA_SCIENCE', displayName: 'Data Science', selected: false },
    { name: 'MACHINE_LEARNING', displayName: 'Machine Learning', selected: false },
    { name: 'WEB_DEVELOPMENT', displayName: 'Web Development', selected: false },
    { name: 'MOBILE_DEVELOPMENT', displayName: 'Mobile Development', selected: false },
    { name: 'CLOUD_COMPUTING', displayName: 'Cloud Computing', selected: false },
    { name: 'CYBER_SECURITY', displayName: 'Cyber Security', selected: false },
    { name: 'DEVOPS', displayName: 'DevOps', selected: false },
    { name: 'DATABASES', displayName: 'Databases', selected: false },
    { name: 'SOFTWARE_ENGINEERING', displayName: 'Software Engineering', selected: false },
    { name: 'OTHER', displayName: 'Other', selected: false }
  ];

  currentFilters: CourseFilterParams = {
    page: 0,
    size: 9,
    isActive: true
  };

  currentSort: string = 'trending'; // Track current sort

  private lastAppliedFilters: CourseFilterParams = {};

  // Sidebar filter options
  levels = [
    { name: 'Beginner', value: 'biginner', selected: false },
    { name: 'Intermediate', value: 'intermediate', selected: false },
    { name: 'Advanced', value: 'advanced', selected: false }
  ];

  durations = [
    { name: 'Less than 2 hours', minDurationHours: 0, maxDurationHours: 2, selected: false },
    { name: '2-5 hours', minDurationHours: 2, maxDurationHours: 5, selected: false },
    { name: '5-10 hours', minDurationHours: 5, maxDurationHours: 10, selected: false },
    { name: '10-20 hours', minDurationHours: 10, maxDurationHours: 20, selected: false }
  ];

  prices = [
    { name: 'Free', minPrice: 0, maxPrice: 0, selected: false },
    { name: 'Less than $20', minPrice: 0.01, maxPrice: 20, selected: false },
    { name: 'Less than $40', minPrice: 20.01, maxPrice: 40, selected: false },
    { name: 'Less than $60', minPrice: 40.01, maxPrice: 60, selected: false }
  ];

  features: any[] = [];
  onlyDiscounted = false;

  private loadingNotificationId: string | null = null;

  constructor(
    private courseService: CourseService,
    private notificationService: NotificationService
  ) {}

  ngOnInit() {
    this.setupCourseLoading();
    this.setupFilterChangeHandling();
    this.loadInitialData();
    this.loadCourses(); // Initial load
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Set up the main course loading stream with race condition prevention
   */
  private setupCourseLoading() {
    this.loadCoursesSubject$.pipe(
      takeUntil(this.destroy$),
      // Prevent duplicate requests for identical filters
      distinctUntilChanged((prev, curr) => this.areFiltersEqual(prev, curr)),
      // Cancel previous requests automatically
      switchMap(filters => {
        this.isLoading = true;
        this.error = null;

        // Show loading notification
        this.loadingNotificationId = this.notificationService.loading('Loading courses...');

        return this.courseService.getCourses(filters).pipe(
          catchError(error => {
            console.error('Error loading courses:', error);
            return of({
              success: false,
              data: null,
              message: error.message || 'Failed to load courses'
            });
          })
        );
      })
    ).subscribe({
      next: (response: any) => {
        this.isLoading = false;

        // Remove loading notification
        if (this.loadingNotificationId) {
          this.notificationService.remove(this.loadingNotificationId);
          this.loadingNotificationId = null;
        }

        if (response.success && response.data) {
          this.courses = response.data.content || [];
          this.totalPages = response.data.totalPages || 0;
          this.totalResults = response.data.totalElements || 0;
          this.currentPage = (response.data.number || 0) + 1; // Convert to 1-based for UI
          this.error = null;

          // Show success notification
          this.notificationService.success(`Loaded ${this.courses.length} courses successfully!`, 2000);
        } else {
          this.error = response.message || 'Failed to load courses. Please try again.';
          this.courses = [];
          this.totalPages = 0;
          this.totalResults = 0;

          // Show error notification
          if (this.error) {
            this.notificationService.error(this.error);
          }
        }
      },
      error: (error) => {
        this.isLoading = false;

        // Remove loading notification
        if (this.loadingNotificationId) {
          this.notificationService.remove(this.loadingNotificationId);
          this.loadingNotificationId = null;
        }

        this.error = 'An unexpected error occurred. Please try again.';
        if (this.error) {
          this.notificationService.error(this.error);
        }
        console.error('Unexpected error in course loading stream:', error);
      }
    });
  }

  /**
   * Set up debounced filter change handling for better UX
   */
  private setupFilterChangeHandling() {
    this.filterChangeSubject$.pipe(
      takeUntil(this.destroy$),
      debounceTime(300), // Wait 300ms after last filter change
      tap(() => {
        // Reset to first page when applying filters
        this.currentFilters.page = 0;
      })
    ).subscribe(() => {
      this.triggerCourseLoad();
    });
  }

  /**
   * Load initial data (categories and tags)
   */
  private loadInitialData() {
    // Use fixed categories from backend enum instead of fetching tags
    // The categories are already initialized in the fixedCategories array
    // We can still load tags for other purposes if needed
    this.courseService.getCategories()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          if (response.success && response.data) {
            // Store tags separately if needed for features/tags filtering
            this.categories = response.data.map(cat => ({ ...cat, selected: false }));
          }
        },
        error: (error) => {
          console.error('Error loading tags:', error);
        }
      });
  }

  /**
   * Trigger course loading (immediate)
   */
  loadCourses() {
    this.triggerCourseLoad();
  }

  /**
   * Internal method to trigger course loading through the subject
   */
  private triggerCourseLoad() {
    const filtersToApply = { ...this.currentFilters };
    this.lastAppliedFilters = filtersToApply;
    this.loadCoursesSubject$.next(filtersToApply);
  }

  /**
   * Apply filters with debouncing (for filter changes)
   */
  private applyFilters() {
    this.filterChangeSubject$.next();
  }

  /**
   * Apply filters immediately (for pagination)
   */
  private applyFiltersImmediate() {
    this.currentFilters.page = 0;
    this.triggerCourseLoad();
  }

  /**
   * Compare two filter objects to prevent unnecessary requests
   */
  private areFiltersEqual(filters1: CourseFilterParams, filters2: CourseFilterParams): boolean {
    return JSON.stringify(filters1) === JSON.stringify(filters2);
  }

  // Pagination handler
  onPageChange(page: number) {
    this.currentFilters.page = page - 1; // Convert to 0-based for API
    this.triggerCourseLoad(); // Immediate loading for pagination
  }

  // Filter handlers
  onCategoriesChange(categories: any[]) {
    this.categories = categories;
    const selectedCategories = categories
      .filter(category => category.selected)
      .map(category => category.name);

    this.currentFilters.categories = selectedCategories.length > 0 ? selectedCategories : undefined;
    this.applyFilters();
  }

  onLevelsChange(levels: any[]) {
    this.levels = levels;
    const selectedLevels = levels
      .filter(level => level.selected)
      .map(level => level.value);

    this.currentFilters.difficultyLevels = selectedLevels.length > 0 ? selectedLevels : undefined;
    this.applyFilters();
  }

  onDurationsChange(durations: any[]) {
    this.durations = durations;
    const selectedDurations = durations.filter(duration => duration.selected);

    if (selectedDurations.length > 0) {
      // Find the minimum and maximum range from selected durations
      const minHours = Math.min(...selectedDurations.map(d => d.minDurationHours));
      const maxHours = Math.max(...selectedDurations.map(d => d.maxDurationHours));

      this.currentFilters.minDurationHours = minHours;
      this.currentFilters.maxDurationHours = maxHours;
    } else {
      this.currentFilters.minDurationHours = undefined;
      this.currentFilters.maxDurationHours = undefined;
    }

    this.applyFilters();
  }

  onPricesChange(prices: any[]) {
    this.prices = prices;
    const selectedPrices = prices.filter(price => price.selected);

    if (selectedPrices.length > 0) {
      // Check if "Free" is selected
      const freeSelected = selectedPrices.some(p => p.minPrice === 0 && p.maxPrice === 0);

      if (freeSelected && selectedPrices.length === 1) {
        this.currentFilters.isFree = true;
        this.currentFilters.minPrice = undefined;
        this.currentFilters.maxPrice = undefined;
      } else {
        this.currentFilters.isFree = undefined;
        const minPrice = Math.min(...selectedPrices.map(p => p.minPrice));
        const maxPrice = Math.max(...selectedPrices.map(p => p.maxPrice));

        this.currentFilters.minPrice = minPrice;
        this.currentFilters.maxPrice = maxPrice;
      }
    } else {
      this.currentFilters.isFree = undefined;
      this.currentFilters.minPrice = undefined;
      this.currentFilters.maxPrice = undefined;
    }

    this.applyFilters();
  }

  onFeaturesChange(features: any[]) {
    this.features = features;
    const selectedFeatures = features
      .filter(feature => feature.selected)
      .map(feature => feature.name);

    this.currentFilters.tags = selectedFeatures.length > 0 ? selectedFeatures : undefined;
    this.applyFilters();
  }

  onOnlyDiscountedChange(onlyDiscounted: boolean) {
    this.onlyDiscounted = onlyDiscounted;

    this.applyFilters();
  }

  /**
   * Handle search input changes (you can add this method if you have a search input)
   */
  onSearchChange(searchTerm: string) {
    this.currentFilters.name = searchTerm || undefined;
    this.applyFilters();
  }

  /**
   * Clear all filters
   */
  clearAllFilters() {
    // Reset filter objects
    this.fixedCategories = this.fixedCategories.map(cat => ({ ...cat, selected: false }));
    this.categories = this.categories.map(cat => ({ ...cat, selected: false }));
    this.levels = this.levels.map(level => ({ ...level, selected: false }));
    this.durations = this.durations.map(duration => ({ ...duration, selected: false }));
    this.prices = this.prices.map(price => ({ ...price, selected: false }));
    this.features = this.features.map(feature => ({ ...feature, selected: false }));
    this.onlyDiscounted = false;

    // Reset current filters to initial state
    this.currentFilters = {
      page: 0,
      size: 20,
      isActive: true
    };

    this.applyFiltersImmediate();
  }

  /**
   * Retry loading courses (useful for error states)
   */
  retryLoading() {
    this.error = null;
    this.triggerCourseLoad();
  }

  /**
   * Handle sort changes from course grid
   */
  onSortChange(sortType: string) {
    this.currentSort = sortType;

    // Clear existing sort parameters and duration filters
    delete this.currentFilters.sort;

    // Reset duration filters unless it's the under2h filter
    if (sortType !== 'under2h') {
      delete this.currentFilters.minDurationHours;
      delete this.currentFilters.maxDurationHours;
    }

    switch(sortType) {
      case 'trending':
        // Sort by most recent (trending content is usually new content)
        this.currentFilters.sort = 'createdDate,desc';
        this.applyFiltersImmediate();
        break;

      case 'topRated':
        // Sort by average rating (client-side)
        // Backend doesn't have averageRating as a database column
        delete this.currentFilters.sort;
        this.applyFiltersImmediateWithClientSort('rating');
        break;

      case 'newest':
        // Sort by newest first
        this.currentFilters.sort = 'createdDate,desc';
        this.applyFiltersImmediate();
        break;

      case 'under2h':
        // Filter courses under 2 hours and sort by duration
        this.currentFilters.maxDurationHours = 2;
        this.currentFilters.sort = 'estimatedDurationInHours,asc';
        this.applyFiltersImmediate();
        break;
    }
  }

  /**
   * Apply filters and sort results on client side
   */
  private applyFiltersImmediateWithClientSort(sortBy: string) {
    this.currentFilters.page = 0;
    const filtersToApply = { ...this.currentFilters };
    this.lastAppliedFilters = filtersToApply;

    this.isLoading = true;
    this.error = null;
    this.loadingNotificationId = this.notificationService.loading('Loading courses...');

    this.courseService.getCourses(filtersToApply).subscribe({
      next: (response: any) => {
        this.isLoading = false;

        if (this.loadingNotificationId) {
          this.notificationService.remove(this.loadingNotificationId);
          this.loadingNotificationId = null;
        }

        if (response.success && response.data) {
          let courses = response.data.content || [];

          // Apply client-side sorting
          if (sortBy === 'rating') {
            courses = courses.sort((a: CourseDto, b: CourseDto) => {
              const ratingA = a.averageRating ?? 0;
              const ratingB = b.averageRating ?? 0;

              // Sort by rating descending (highest first)
              if (ratingB !== ratingA) {
                return ratingB - ratingA;
              }

              // If ratings are equal, sort by review count descending
              return (b.reviewCount ?? 0) - (a.reviewCount ?? 0);
            });
          }

          this.courses = courses;
          this.totalPages = response.data.totalPages || 0;
          this.totalResults = response.data.totalElements || 0;
          this.currentPage = (response.data.number || 0) + 1;
          this.error = null;

          this.notificationService.success(`Loaded ${this.courses.length} courses successfully!`, 2000);
        } else {
          this.error = response.message || 'Failed to load courses. Please try again.';
          this.courses = [];
          this.totalPages = 0;
          this.totalResults = 0;

          if (this.error) {
            this.notificationService.error(this.error);
          }
        }
      },
      error: (error) => {
        this.isLoading = false;

        if (this.loadingNotificationId) {
          this.notificationService.remove(this.loadingNotificationId);
          this.loadingNotificationId = null;
        }

        this.error = 'An unexpected error occurred. Please try again.';
        if (this.error) {
          this.notificationService.error(this.error);
        }
        console.error('Unexpected error in course loading:', error);
      }
    });
  }
}
