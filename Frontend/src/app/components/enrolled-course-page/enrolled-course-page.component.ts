import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CourseService } from '../../Services/Courses/course-service';
import { UserService } from '../../Services/User/user-service';
import { NotificationService } from '../../Services/notification.service';
import { StarRatingComponent } from '../star-rating/star-rating.component';
import { FormsModule } from '@angular/forms';

interface CourseProgress {
  completedLessons: number;
  totalLessons: number;
  percentage: number;
  timeSpent: number; // in minutes
  certificateEligible: boolean;
}

interface ModuleProgress {
  moduleId: number;
  completed: boolean;
  lessonsCompleted: number;
  totalLessons: number;
  percentage: number;
}

@Component({
  selector: 'app-enrolled-course-page',
  standalone: true,
  imports: [CommonModule, StarRatingComponent, FormsModule, RouterLink],
  templateUrl: './enrolled-course-page.component.html',
  styleUrls: ['./enrolled-course-page.component.css']
})
export class EnrolledCoursePageComponent implements OnInit {
  courseId: number | null = null;
  userId: number | null = null;

  // Course data
  course: any = null;
  modules: any[] = [];

  // Progress tracking
  courseProgress: CourseProgress = {
    completedLessons: 0,
    totalLessons: 0,
    percentage: 0,
    timeSpent: 0,
    certificateEligible: false
  };
  moduleProgress: Map<number, ModuleProgress> = new Map();

  // Access information
  hasAccess = false;
  accessExpiresAt: Date | null = null;
  accessType: string = '';

  // UI State
  loading = true;
  error: string | null = null;
  activeTab: 'overview' | 'curriculum' | 'resources' | 'discussion' = 'overview';
  expandedModules: Set<number> = new Set();

  // Quizzes & Assessments
  availableQuizzes: any[] = [];
  quizScores: Map<number, number> = new Map();

  // Announcements & Updates
  announcements: any[] = [];

  // Course resources/attachments
  courseResources: any[] = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private courseService: CourseService,
    private userService: UserService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    // Get courseId from route
    this.route.paramMap.subscribe(params => {
      const id = params.get('courseId') || params.get('id');
      if (id) {
        this.courseId = Number(id);
        this.loadCourseData();
      }
    });

    // Get current user
    const user = this.userService.getUser();
    if (user) {
      this.userId = user.user_id;
    }
  }

  async loadCourseData(): Promise<void> {
    if (!this.courseId) return;

    this.loading = true;
    this.error = null;

    try {
      // Check if user has access to this course
      if (this.userId) {
        await this.checkCourseAccess();
      }

      // Load course details
      const courseResponse: any = await this.courseService.getCourse(this.courseId).toPromise();
      this.course = courseResponse?.data || courseResponse;

      // Extract modules
      this.extractModules();

      // Load lessons for each module
      await this.loadModuleLessons();

      // Load course resources/attachments
      await this.loadCourseResources();

      // Load progress data (mock for now - you can implement backend API)
      this.calculateProgress();

      // Load quizzes
      await this.loadQuizzes();

      // Load mock announcements
      this.loadAnnouncements();

    } catch (err: any) {
      console.error('Error loading course data:', err);
      this.error = err?.message || 'Failed to load course data';
      this.notificationService.error('Failed to load course data');
    } finally {
      this.loading = false;
    }
  }

  async checkCourseAccess(): Promise<void> {
    if (!this.userId || !this.courseId) return;

    try {
      // Call backend API to check access
      const response: any = await this.courseService.checkUserCourseAccess(
        this.userId,
        this.courseId
      ).toPromise();

      this.hasAccess = response?.hasAccess || response?.data || false;

      // Get access details
      if (this.hasAccess) {
        const accessDetails: any = await this.courseService.getUserCourseAccessDetails(
          this.userId,
          this.courseId
        ).toPromise();

        if (accessDetails?.data) {
          this.accessType = accessDetails.data.accessType || 'DIRECT';
          if (accessDetails.data.accessUntil) {
            this.accessExpiresAt = new Date(accessDetails.data.accessUntil);
          }
        }
      }

    } catch (err) {
      console.warn('Could not verify course access:', err);
      // Allow viewing anyway - backend will restrict sensitive operations
      this.hasAccess = true;
    }
  }

  extractModules(): void {
    if (!this.course) return;

    // Normalize different module property names
    const modulesArray = this.course.modules ||
                        this.course.courseModules ||
                        this.course.courseModulesList ||
                        [];

    this.modules = modulesArray.map((m: any) => {
      // Handle wrapper objects (CourseModuleDto has moduleOrder and module)
      if (m && m.module) {
        return {
          ...m.module,
          id: m.module.moduleId,
          name: m.module.moduleName,
          description: m.module.moduleDescription,
          estimatedDuration: m.module.estimatedDuration,
          numberOfVideos: m.module.numberOfvideos,
          order: m.moduleOrder,
          videos: [], // Will be loaded separately
          _wrapper: m
        };
      }
      return {
        ...m,
        videos: [] // Will be loaded separately
      };
    }).sort((a: any, b: any) => (a.order || 0) - (b.order || 0));

    // Expand first module by default
    if (this.modules.length > 0 && this.modules[0].id) {
      this.expandedModules.add(this.modules[0].id);
    }
  }

  async loadModuleLessons(): Promise<void> {
    if (!this.modules || this.modules.length === 0) return;

    try {
      // Load lessons for each module
      const lessonPromises = this.modules.map(async (module: any) => {
        if (module.id) {
          try {
            const lessons = await this.courseService.getModuleLessons(module.id).toPromise();
            module.videos = lessons || [];
            module.lessons = lessons || [];
          } catch (err) {
            console.warn(`Could not load lessons for module ${module.id}:`, err);
            module.videos = [];
            module.lessons = [];
          }
        }
      });

      await Promise.all(lessonPromises);
    } catch (err) {
      console.warn('Error loading module lessons:', err);
    }
  }

  async loadCourseResources(): Promise<void> {
    if (!this.course) return;

    // The course resources might come from attachments or a dedicated resources field
    this.courseResources = this.course.resources || this.course.attachments || [];

    // If there are attachments at course level or in modules, collect them
    if (this.modules && this.modules.length > 0) {
      const allResources: any[] = [...this.courseResources];

      this.modules.forEach((module: any) => {
        if (module.attachments && Array.isArray(module.attachments)) {
          allResources.push(...module.attachments.map((att: any) => ({
            ...att,
            moduleName: module.name || module.moduleName
          })));
        }

        // Also check lessons for attachments
        const lessons = module.videos || module.lessons || [];
        lessons.forEach((lesson: any) => {
          if (lesson.attachments && Array.isArray(lesson.attachments)) {
            allResources.push(...lesson.attachments.map((att: any) => ({
              ...att,
              lessonName: lesson.videoName || lesson.name || lesson.title,
              moduleName: module.name || module.moduleName
            })));
          }
        });
      });

      this.courseResources = allResources;
    }
  }

  async loadQuizzes(): Promise<void> {
    if (!this.courseId) return;

    try {
      const quizzes: any = await this.courseService.getQuizzes(this.courseId).toPromise();
      this.availableQuizzes = quizzes || [];
    } catch (err) {
      console.warn('Could not load quizzes:', err);
    }
  }

  calculateProgress(): void {
    let totalLessons = 0;
    let completedLessons = 0;

    this.modules.forEach(module => {
      const lessons = module.videos || module.lessons || [];
      const moduleTotalLessons = lessons.length;
      const moduleCompletedLessons = lessons.filter((l: any) => l.completed).length;

      totalLessons += moduleTotalLessons;
      completedLessons += moduleCompletedLessons;

      // Store module progress
      if (module.id) {
        this.moduleProgress.set(module.id, {
          moduleId: module.id,
          completed: moduleCompletedLessons === moduleTotalLessons && moduleTotalLessons > 0,
          lessonsCompleted: moduleCompletedLessons,
          totalLessons: moduleTotalLessons,
          percentage: moduleTotalLessons > 0 ? (moduleCompletedLessons / moduleTotalLessons) * 100 : 0
        });
      }
    });

    this.courseProgress.totalLessons = totalLessons;
    this.courseProgress.completedLessons = completedLessons;
    this.courseProgress.percentage = totalLessons > 0 ? (completedLessons / totalLessons) * 100 : 0;
    this.courseProgress.certificateEligible = this.courseProgress.percentage >= 80;

    // Mock time spent
    this.courseProgress.timeSpent = Math.floor(completedLessons * 15); // 15 mins per lesson avg
  }

  loadAnnouncements(): void {
    // Mock announcements - replace with real API call
    this.announcements = [
      {
        id: 1,
        title: 'Welcome to the course!',
        content: 'We\'re excited to have you here. Check out the curriculum and start learning.',
        date: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000),
        type: 'info'
      },
      {
        id: 2,
        title: 'New lesson added',
        content: 'Module 3 has been updated with additional content.',
        date: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000),
        type: 'update'
      }
    ];
  }

  // UI Methods
  setActiveTab(tab: 'overview' | 'curriculum' | 'resources' | 'discussion'): void {
    this.activeTab = tab;
  }

  toggleModule(moduleId: number): void {
    if (this.expandedModules.has(moduleId)) {
      this.expandedModules.delete(moduleId);
    } else {
      this.expandedModules.add(moduleId);
    }
  }

  isModuleExpanded(moduleId: number): boolean {
    return this.expandedModules.has(moduleId);
  }

  getModuleProgress(moduleId: number): ModuleProgress | undefined {
    return this.moduleProgress.get(moduleId);
  }

  // Navigation Methods
  startLearning(): void {
    if (!this.courseId) return;

    // Find first incomplete lesson
    for (const module of this.modules) {
      const lessons = module.videos || module.lessons || [];
      const firstIncomplete = lessons.find((l: any) => !l.completed);

      if (firstIncomplete) {
        this.goToLesson(firstIncomplete.id || firstIncomplete.videoId);
        return;
      }
    }

    // If all complete or no lessons, go to first lesson
    const firstModule = this.modules[0];
    if (firstModule) {
      const lessons = firstModule.videos || firstModule.lessons || [];
      if (lessons.length > 0) {
        this.goToLesson(lessons[0].id || lessons[0].videoId);
      }
    }
  }

  goToLesson(lessonId: number): void {
    if (!this.courseId) return;
    this.router.navigate(['/course', this.courseId, 'learn'], {
      queryParams: { lessonId }
    });
  }

  viewModule(module: any): void {
    const lessons = module.videos || module.lessons || [];
    if (lessons.length > 0) {
      this.goToLesson(lessons[0].id || lessons[0].videoId);
    }
  }

  openQuiz(quiz: any): void {
    if (!quiz || !quiz.id) return;
    // Navigate to quiz page or open modal
    this.notificationService.info('Quiz feature coming soon!');
  }

  downloadCertificate(): void {
    if (!this.courseProgress.certificateEligible) {
      this.notificationService.warning('Complete at least 80% of the course to earn your certificate');
      return;
    }

    this.notificationService.success('Certificate download feature coming soon!');
  }

  downloadResource(resource: any): void {
    this.notificationService.info('Resource download feature coming soon!');
  }

  formatDuration(minutes: number): string {
    if (minutes < 60) return `${minutes}m`;
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return mins > 0 ? `${hours}h ${mins}m` : `${hours}h`;
  }

  formatDate(date: Date): string {
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    const days = Math.floor(diff / (1000 * 60 * 60 * 24));

    if (days === 0) return 'Today';
    if (days === 1) return 'Yesterday';
    if (days < 7) return `${days} days ago`;
    if (days < 30) return `${Math.floor(days / 7)} weeks ago`;

    return date.toLocaleDateString();
  }

  getDaysUntilExpiry(): number | null {
    if (!this.accessExpiresAt) return null;

    const now = new Date();
    const diff = this.accessExpiresAt.getTime() - now.getTime();
    return Math.ceil(diff / (1000 * 60 * 60 * 24));
  }

  trackByModuleId(index: number, module: any): any {
    return module?.id || index;
  }

  trackByLessonId(index: number, lesson: any): any {
    return lesson?.id || lesson?.videoId || index;
  }

  trackByQuizId(index: number, quiz: any): any {
    return quiz?.id || index;
  }
}
