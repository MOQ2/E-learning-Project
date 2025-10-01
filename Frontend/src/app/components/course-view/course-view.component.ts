import { Component, Input, Output, EventEmitter, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { CourseService } from '../../Services/Courses/course-service';
import { Router } from '@angular/router';
import { StarRatingComponent } from '../star-rating/star-rating.component';
import { NotificationService } from '../../Services/notification.service';

@Component({
  selector: 'app-course-view',
  standalone: true,
  imports: [CommonModule, FormsModule, StarRatingComponent],
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
    currency?: string;
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
    plans?: any;
  certificate?: boolean | null;
  instructorTitle?: string | null;
    instructorAvatar?: string | null;
    language?: string | null;
    subtitles?: string[] | null;
    enrolledCount?: number;
    averageRating?: number;
    reviewCount?: number;
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

    return {
      id: raw.id || raw.courseId || null,
      name: raw.name || 'Untitled course',
      description: raw.description || '',
      oneTimePrice: raw.oneTimePrice || raw.price || 0,
      currency: raw.currency || '$',
      category: raw.category || (raw.tags && raw.tags.length ? raw.tags[0] : null),
      estimatedDurationInHours: raw.estimatedDurationInHours || raw.estimatedDrationInHours || raw.estimatedDuration || 0,
      status: raw.status || null,
      difficultyLevel: raw.difficultyLevel || raw.level || null,
      isActive: raw.isActive ?? true,
      modules: (raw.modules || []).map((m: any) => ({
        order: m.moduleOrder ?? null,
        id: m.module?.moduleId ?? m.moduleId ?? m.id ?? null,
        name: m.module?.moduleName ?? m.name ?? 'Module',
        description: m.module?.moduleDescription ?? m.description ?? '',
        estimatedDuration: m.module?.estimatedDuration ?? m.estimatedDuration ?? 0,
        numberOfVideos: m.module?.numberOfvideos ?? 0,
        active: m.module?.active ?? true
      })),
      whatYouWillLearn: raw.whatYouWillLearn || raw.objectives || [],
      resources: raw.resources || ['Downloadable notebooks', 'Datasets and cheat sheets', 'Quizzes and certificate', 'Community Q&A'],
      instructor: raw.instructor || raw.createdByName || null,
      lastUpdated: raw.updatedAt || raw.lastUpdated || null,
      thumbnailUrl: this.buildThumbnailUrl(raw.thumbnail)
      ,
      // map subscription plans/pricing from backend DTO names (and fallbacks)
      plans: raw.plans || raw.pricing || raw.subscription || {
        monthly: raw.subscriptionPriceMonthly ?? raw.monthlyPrice ?? raw.monthly ?? null,
        threeMonths: raw.subscriptionPrice3Months ?? raw.threeMonthsPrice ?? raw['3months'] ?? null,
        sixMonths: raw.subscriptionPrice6Months ?? raw.sixMonthsPrice ?? raw.sixMonths ?? null,
        annual: raw.annualPrice ?? raw.yearly ?? null
      },
      allowsSubscription: raw.allowsSubscription ?? raw.allows_subscriptions ?? true,
      enrolledCount: raw.enrolledCount ?? raw.enrollmentsCount ?? 0,
      averageRating: raw.averageRating ?? raw.avgRating ?? 0,
      reviewCount: raw.reviewCount ?? raw.reviewsCount ?? 0
    };
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

  onSelectPlan(plan: string, price?: number) {
    this.subscribe.emit({ plan, price });
    this.router.navigate(['/payment'], { queryParams: { courseId: this.courseData?.id, plan, price } });
  }

  onPreview() {
    this.previewLesson.emit();
  }
}
