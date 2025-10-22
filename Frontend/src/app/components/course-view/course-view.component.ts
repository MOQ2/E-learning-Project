import { Component, Input, Output, EventEmitter, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { CourseService } from '../../Services/Courses/course-service';
import { Router, RouterModule } from '@angular/router';
import { StarRatingComponent } from '../star-rating/star-rating.component';
import { NotificationService } from '../../Services/notification.service';

@Component({
  selector: 'app-course-view',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, StarRatingComponent],
  templateUrl: './course-view.component.html',
  styleUrls: ['./course-view.component.css']
})
export class CourseViewComponent implements OnInit, OnChanges {
  @Input() courseId?: number | null;
  @Input() course?: any | null; // optional preloaded course data

  @Output() subscribe = new EventEmitter<{ plan: string; price?: number }>();
  @Output() previewLesson = new EventEmitter<void>();

  loading = false;
  error: string | null = null;
  reviews: Array<any> = [];
  // whether to show all reviews or only a short peek
  showAllReviews = false;
  // pagination tracking (public so template can read)
  reviewsPage = 0;
  reviewsPageSize = 3;
  reviewsTotalPages = 0;
  // total elements reported by server (used to decide whether to show "show all" toggle)
  reviewsTotalElements = 0;
  // UI state flags
  isSubmitting = false;
  isReloadingReviews = false;


  reviewText = '';
  reviewRating: number | null = null;
  reviewAnonymous = false;

  // define a minimal type so Angular template type checker can resolve properties
  courseData: {
    id?: number | null;
    name?: string;
    description?: string;
    oneTimePrice?: number;
    currency?: string | null;
    currencyCode?: string | null;
    category?: string | null;
    estimatedDurationInHours?: number;
    status?: string | null;
    difficultyLevel?: string | null;
    isActive?: boolean;
    modules?: Array<any>;
    whatYouWillLearn?: string[];
    resources?: string[];
    instructor?: string | null;
    lastUpdated?: string | null;
    thumbnailUrl?: string | null;
    plans?: {
      monthly?: number | null;
      threeMonths?: number | null;
      sixMonths?: number | null;
      annual?: number | null;
    };
    certificate?: boolean | null;
    instructorTitle?: string | null;
    instructorAvatar?: string | null;
    language?: string | null;
    subtitles?: string[] | null;
    enrolledCount?: number;
    averageRating?: number;
    reviewCount?: number;
    tags?: Array<any>;
    allowsSubscription?: boolean;
    hasSubscriptionPlans?: boolean;
  } | null = null;

  constructor(
    private http: HttpClient,
    private courseService: CourseService,
    private router: Router,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    if (!this.course && this.courseId) {
      this.loadCourse(this.courseId);
    } else if (this.course) {
      this.courseData = this.normalize(this.course);
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['course'] && this.course) {
      this.courseData = this.normalize(this.course);
    }
    if (changes['courseId'] && this.courseId) {
      this.loadCourse(this.courseId);
    }
  }

  public loadCourse(id: number) {
    this.loading = true;
    this.error = null;

    const loadingNotification = this.notificationService.loading('Loading course details...');

    this.courseService.getCourse(id).subscribe({
      next: (res: any) => {
        const payload = res && res.data ? res.data : res;
        this.courseData = this.normalize(payload);
        // load reviews separately
        this.loadReviews(id);
        this.loading = false;

        this.notificationService.remove(loadingNotification);
        this.notificationService.success('Course loaded successfully!', 2000);
      },
      error: (err: any) => {
        this.error = err?.error?.message || err?.message || 'Failed to load course';
        this.loading = false;

        this.notificationService.remove(loadingNotification);
        if (this.error) {
          this.notificationService.error(this.error);
        }
      }
    });
  }

  private loadReviews(courseId: number) {
    this.reviewsPage = 0;
    this.reviewsPageSize = 3;
    this.reviewsTotalPages = 0;
    this.reviews = [];
    this.isReloadingReviews = true;
    this.courseService.getReviews(courseId, this.reviewsPage, this.reviewsPageSize).subscribe({
      next: (res: any) => {
        const payload = res && res.data ? res.data : res;
        // expected shape: { content: [...], page, size, totalElements, totalPages }
        if (payload && payload.content) {
          this.reviews = payload.content || [];
          this.reviewsPage = payload.page || 0;
          this.reviewsPageSize = payload.size || this.reviewsPageSize;
          this.reviewsTotalPages = payload.totalPages || 0;
          this.reviewsTotalElements = payload.totalElements || 0;
        } else if (Array.isArray(payload)) {
          this.reviews = payload;
          this.reviewsTotalElements = this.reviews.length;
          this.reviewsTotalPages = Math.ceil(this.reviewsTotalElements / this.reviewsPageSize);
        }
      },
      error: () => {
        // ignore reviews load errors for now
        this.reviews = [];
      },
      complete: () => {
        this.isReloadingReviews = false;
      }
    });
  }

  submitReview() {
    if (!this.courseData || !this.courseData.id) return;

    if (!this.reviewText || !this.reviewRating) {
      this.notificationService.error('Please provide both rating and review text');
      return;
    }

    const payload: any = { userId: 1, feedbackText: this.reviewText, rating: this.reviewRating, isAnonymous: this.reviewAnonymous };
    // optimistic update: show the review immediately and update counts
    const tempReview = {
      id: `temp-${Date.now()}`,
      userId: payload.userId,
      feedbackText: payload.feedbackText,
      rating: payload.rating,
      isAnonymous: payload.isAnonymous,
      createdAt: new Date().toISOString(),
      _optimistic: true
    };
    // prepend optimistic review and update totals locally
    this.reviews.unshift(tempReview);
    this.reviewsTotalElements = (this.reviewsTotalElements || 0) + 1;
    this.courseData!.reviewCount = (this.courseData!.reviewCount || 0) + 1;
    // show expanded view when user posts
    this.showAllReviews = true;

    this.isSubmitting = true;

    const submittingNotification = this.notificationService.loading('Submitting your review...');
    // fire POST, then reload authoritative data in background
    this.courseService.postReview(this.courseData.id, payload).subscribe({
      next: (res: any) => {
        this.notificationService.remove(submittingNotification);
        this.notificationService.success('Review submitted successfully!', 3000);

        // we don't rely on the response to update UI — reload will reconcile
        // trigger a background reload of reviews to reconcile optimistic state
        if (this.courseData && this.courseData.id) {
          // keep isReloadingReviews true until reload completes
          this.isReloadingReviews = true;
          this.courseService.getReviews(this.courseData.id, 0, this.reviewsPageSize).subscribe({
            next: (r: any) => {
              const payload = r && r.data ? r.data : r;
              if (payload && payload.content) {
                this.reviews = payload.content || [];
                this.reviewsPage = payload.page || 0;
                this.reviewsPageSize = payload.size || this.reviewsPageSize;
                this.reviewsTotalPages = payload.totalPages || 0;
                this.reviewsTotalElements = payload.totalElements || 0;
              }
            },
            error: () => {},
            complete: () => {
              this.isReloadingReviews = false;
            }
          });
        }
      },
      error: (err: any) => {
        console.error('Failed to submit review', err);
        this.notificationService.remove(submittingNotification);
        this.notificationService.error('Failed to submit review. Please try again.');

        // remove the optimistic review on error and decrement counts
        const idx = this.reviews.findIndex(r => r && r.id === tempReview.id);
        if (idx >= 0) this.reviews.splice(idx, 1);
        this.reviewsTotalElements = Math.max(0, (this.reviewsTotalElements || 1) - 1);
        this.courseData!.reviewCount = Math.max(0, (this.courseData!.reviewCount || 1) - 1);
      },
      complete: () => {
        this.isSubmitting = false;
      }
    });
    // clear form inputs immediately (UX decision)
    this.reviewText = '';
    this.reviewRating = null;
    this.reviewAnonymous = false;
  }

  // load just the next page and append (used by "Load more")
  loadMoreReviews() {
    if (!this.courseData || !this.courseData.id) return;
    // if already loading, ignore
    if (this.isReloadingReviews) return;
    const nextPage = this.reviewsPage + 1;
    if (nextPage >= this.reviewsTotalPages) return;
    this.isReloadingReviews = true;
    this.courseService.getReviews(this.courseData.id, nextPage, this.reviewsPageSize).subscribe({
      next: (res: any) => {
        const payload = res && res.data ? res.data : res;
        if (payload && payload.content) {
          this.reviews = this.reviews.concat(payload.content || []);
          this.reviewsPage = payload.page || nextPage;
          this.reviewsTotalPages = payload.totalPages || this.reviewsTotalPages;
          this.reviewsTotalElements = payload.totalElements || this.reviewsTotalElements;
          // after loading more, mark expanded
          this.showAllReviews = true;
        }
      },
      error: () => {},
      complete: () => {
        this.isReloadingReviews = false;
      }
    });
  }

  collapseReviews() {
    this.showAllReviews = false;
  }

  // number of reviews that will be loaded on next "Load more" click
  get nextLoadCount(): number {
    const remaining = Math.max(0, (this.reviewsTotalElements || 0) - (this.reviews?.length || 0));
    return Math.min(this.reviewsPageSize || 0, remaining);
  }

  // helper to toggle between peek and full list
  toggleReviews() {
    this.showAllReviews = !this.showAllReviews;
    if (this.showAllReviews) {
      // if we already loaded all pages, nothing to do
      if (this.reviewsPage + 1 >= this.reviewsTotalPages) return;
      // fetch remaining pages sequentially (small number expected)
      const toFetch: Array<Promise<any>> = [];
      for (let p = this.reviewsPage + 1; p < this.reviewsTotalPages; p++) {
        toFetch.push(this.courseService.getReviews(this.courseData!.id!, p, this.reviewsPageSize).toPromise());
      }
      // perform sequential append
      (async () => {
        for (let p = this.reviewsPage + 1; p < this.reviewsTotalPages; p++) {
          try {
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            const r: any = await this.courseService.getReviews(this.courseData!.id!, p, this.reviewsPageSize).toPromise();
            const payload = r && r.data ? r.data : r;
            if (payload && payload.content) {
              this.reviews = this.reviews.concat(payload.content || []);
            }
          } catch (e) {
            // ignore errors on subsequent pages
          }
        }
      })();
    }
  }

  // getter used by template to render either a peek or full set
  get displayedReviews() {
    if (!this.reviews) return [];
    return this.showAllReviews ? this.reviews : this.reviews.slice(0, 3);
  }

  private normalize(raw: any) {
    if (!raw) return null;

    console.log('[CourseView] Raw backend data:', raw);

    // Extract category from tags or category field
    const category = raw.category || (raw.tags && raw.tags.length > 0 ? (raw.tags[0].name || raw.tags[0]) : null);

    const currencyCode = raw.currency || 'USD';
    const oneTimePrice = this.parsePrice(raw.oneTimePrice ?? raw.price);
    const plans = {
      monthly: this.parsePrice(raw.subscriptionPriceMonthly ?? raw.monthlyPrice ?? raw.monthly),
      threeMonths: this.parsePrice(raw.subscriptionPrice3Months ?? raw.threeMonthsPrice ?? raw['3months']),
      sixMonths: this.parsePrice(raw.subscriptionPrice6Months ?? raw.sixMonthsPrice ?? raw.sixMonths),
      annual: this.parsePrice(raw.subscriptionPrice12Months ?? raw.annualPrice ?? raw.yearly ?? raw.twelveMonths)
    };
    const hasSubscriptionPlans = Object.values(plans).some(value => (value ?? 0) > 0);

    const normalized = {
      id: raw.id || raw.courseId || null,
      name: raw.name || 'Untitled course',
      description: raw.description || '',
      oneTimePrice: oneTimePrice ?? 0,
      currency: this.formatCurrency(currencyCode),
      currencyCode,
      category: category,
      estimatedDurationInHours: raw.estimatedDurationInHours || raw.estimatedDrationInHours || raw.estimatedDuration || 0,
      status: raw.status || null,
      difficultyLevel: raw.difficultyLevel || raw.level || null,
      isActive: raw.isActive ?? true,
      modules: (raw.modules || [])
        .map((m: any, index: number) => ({
          order: m.moduleOrder ?? (index + 1),
          id: m.module?.moduleId ?? m.moduleId ?? m.id ?? null,
          name: m.module?.moduleName ?? m.name ?? 'Module',
          description: m.module?.moduleDescription ?? m.description ?? '',
          estimatedDuration: m.module?.estimatedDuration ?? m.estimatedDuration ?? 0,
          numberOfVideos: m.module?.numberOfvideos ?? m.numberOfVideos ?? 0,
          active: m.module?.active ?? m.active ?? true,
          previewAvailable: m.module?.previewAvailable ?? m.previewAvailable ?? false
        }))
        .sort((a: any, b: any) => a.order - b.order), // Sort modules by their order
      whatYouWillLearn: raw.whatYouWillLearn || raw.objectives || [],
      resources: raw.resources || [],
      instructor: raw.instructor || raw.createdByName || 'Instructor',
      lastUpdated: raw.updatedAt || raw.lastUpdated || new Date().toISOString(),
      thumbnailUrl: this.buildThumbnailUrl(raw.thumbnail),
      // map subscription plans/pricing from backend DTO names
      plans,
      allowsSubscription: raw.allowsSubscription ?? hasSubscriptionPlans,
      hasSubscriptionPlans,
      enrolledCount: raw.enrolledCount ?? raw.enrollmentsCount ?? 0,
      averageRating: raw.averageRating ?? raw.avgRating ?? 0,
      reviewCount: raw.reviewCount ?? raw.reviewsCount ?? 0,
      tags: raw.tags || [],
      certificate: raw.certificate ?? true,
      instructorTitle: raw.instructorTitle || null,
      instructorAvatar: raw.instructorAvatar || null,
      language: raw.language || 'English',
      subtitles: raw.subtitles || []
    };

    console.log('[CourseView] Normalized data:', {
      enrolledCount: normalized.enrolledCount,
      averageRating: normalized.averageRating,
      reviewCount: normalized.reviewCount
    });

    return normalized;
  }

  private formatCurrency(curr: string): string {
    const currencyMap: { [key: string]: string } = {
      'USD': '$',
      'EUR': '€',
      'GBP': '£',
      'ILS': '₪',
      'JOD': 'JD'
    };
    const key = (typeof curr === 'string' ? curr : '').toUpperCase();
    return currencyMap[key] || curr;
  }

  private parsePrice(value: any): number | null {
    if (value === null || value === undefined) {
      return null;
    }

    if (typeof value === 'number') {
      return Number.isFinite(value) ? value : null;
    }

    if (typeof value === 'string') {
      const trimmed = value.trim();
      if (!trimmed) {
        return null;
      }
      const numeric = Number(trimmed);
      return Number.isNaN(numeric) ? null : numeric;
    }

    if (typeof value === 'object' && value !== null) {
      if ('value' in value) {
        return this.parsePrice((value as { value: any }).value);
      }
      if ('amount' in value) {
        return this.parsePrice((value as { amount: any }).amount);
      }
    }

    return null;
  }

  private buildThumbnailUrl(thumbnail: any) {
    if (!thumbnail) return 'https://placehold.co/150x120/f8fafc/b099ad?text=Course+Image';
    if (typeof thumbnail === 'string' && thumbnail.startsWith('http')) return thumbnail;
    // assume thumbnail is an attachment id
    return `http://localhost:5000/api/attachments/${thumbnail}/download`;
  }

  onSubscribeOneTime() {
    if (!this.courseData) return;
    // emit for backward compatibility and navigate to payment page
    this.subscribe.emit({ plan: 'one-time', price: this.courseData.oneTimePrice });
    this.router.navigate(['/payment'], { queryParams: { courseId: this.courseData.id, plan: 'one-time', price: this.courseData.oneTimePrice } });
  }

  onSelectPlan(plan: string, price?: number | null) {
    const normalizedPrice = price ?? undefined;
    const payload: { plan: string; price?: number } = { plan };
    if (normalizedPrice !== undefined) {
      payload.price = normalizedPrice;
    }

    this.subscribe.emit(payload);
    this.router.navigate([
      '/payment'
    ], {
      queryParams: {
        courseId: this.courseData?.id,
        plan,
        price: normalizedPrice
      }
    });
  }

  onPreview() {
    this.previewLesson.emit();
  }
}
