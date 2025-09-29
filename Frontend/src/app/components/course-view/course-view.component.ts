import { Component, Input, Output, EventEmitter, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { CourseService } from '../../Services/Courses/course-service';

@Component({
  selector: 'app-course-view',
  standalone: true,
  imports: [CommonModule],
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
  } | null = null;

  constructor(private http: HttpClient, private courseService: CourseService) {}

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

  private loadCourse(id: number) {
    this.loading = true;
    this.error = null;

    // Prefer CourseService.getCourse if available, otherwise fallback to HttpClient
    const svc: any = this.courseService as any;
    const obs = (svc && typeof svc.getCourse === 'function')
      ? svc.getCourse(id)
      : this.http.get<any>(`http://localhost:5000/api/courses/${id}`);

    obs.subscribe({
      next: (res: any) => {
        // backend might wrap response in { success, message, data }
        const payload = res && res.data ? res.data : res;
        this.courseData = this.normalize(payload);
        this.loading = false;
      },
      error: (err: any) => {
        this.error = err?.message || 'Failed to load course';
        this.loading = false;
      }
    });
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
    this.subscribe.emit({ plan: 'one-time', price: this.courseData.oneTimePrice });
  }

  onSelectPlan(plan: string, price?: number) {
    this.subscribe.emit({ plan, price });
  }

  onPreview() {
    this.previewLesson.emit();
  }
}
