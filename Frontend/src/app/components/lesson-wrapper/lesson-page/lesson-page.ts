import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { CourseService } from '../../../Services/Courses/course-service';
import { UserService } from '../../../Services/User/user-service';
import { UserVideoService } from '../../../Services/UserVideo/user-video.service';
import { VideoProgressService } from '../../../Services/VideoProgress/video-progress.service';
import { HttpClientModule } from '@angular/common/http';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { Subscription } from 'rxjs';

// local imports for Plyr and hls.js
import Hls from 'hls.js';

@Component({
  selector: 'app-lesson-page',
  standalone: true,
  imports: [CommonModule, HttpClientModule],
  templateUrl: './lesson-page.html',
  styleUrls: ['./lesson-page.css']
})
export class LessonPage implements OnInit, OnDestroy {
  moduleId: number | null = null;
  lessonId: number | null = null;
  lessons: any[] = [];
  lesson: any = null;
  // Course title shown in breadcrumb/hero (optional)
  courseTitle: string | null = null;
  // currently selected module (object from fullModules)
  selectedModule: any = null;
  selectedModuleTitle: string | null = null;
  // control visibility of the left sidebar
  showSidebar = true;
  // simple toggle state for notes UI (not shown here, kept for binding)
  showNotes = false;
  videoSrc: string | null | undefined = null;
  loading = false;
  // separate flag for lesson-specific loading to avoid toggling module-level loader
  loadingLesson = false;
  // index (1-based) of current lesson and total count for header controls
  lessonIndex = 0;
  lessonCount = 0;
  error: string | null = null;
  explanationHtml: SafeHtml | null = null;
  whatYouWillLearnList: string[] = [];
  prerequisitesList: string[] = [];
  attachments: Array<{ id?: number; fileName?: string; fileDownloadUrl?: string; fileSize?: string }> = [];
  // quizzes for the lesson's course
  quizzes: Array<any> = [];
  selectedQuiz: any = null;
  // UI state for attachments/notes/transcript tabs
  attachmentsTab: 'attachments' | 'quizzes' = 'attachments';
  attachmentsHidden = false;
  // module collapse state by module id/name
  moduleCollapsed: { [key: string]: boolean } = {};
  // when available, hold the full course modules list (modules -> videos)
  fullModules: Array<any> = [];
  // cached grouped modules used by the sidebar to avoid recreating arrays on each change
  groupedModules: { title: string; items: any[]; key: string }[] = [];

  // User and watched videos tracking
  userId: number | null = null;
  watchedVideoIds: Set<number> = new Set();
  courseId: number | null = null;

  // Cleanup function for video progress tracking
  private cleanupProgressTracking: (() => void) | null = null;
  private queryParamSub: Subscription | null = null;
  private skipNextQueryParamHandling = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private courseService: CourseService,
    private sanitizer: DomSanitizer,
    private userService: UserService,
    private userVideoService: UserVideoService,
    private videoProgressService: VideoProgressService
  ) {}

  ngOnInit(): void {
    const modParam = this.route.snapshot.paramMap.get('moduleId');
    const lessonParam = this.route.snapshot.paramMap.get('lessonId');
    const courseParam = this.route.snapshot.paramMap.get('courseId');
    const queryLessonParam = this.route.snapshot.queryParamMap.get('lessonId');
    this.moduleId = modParam ? Number(modParam) : null;
    this.lessonId = lessonParam ? Number(lessonParam) : null;
    this.courseId = courseParam ? Number(courseParam) : null;

    if (!this.lessonId && queryLessonParam) {
      const parsed = Number(queryLessonParam);
      this.lessonId = Number.isFinite(parsed) ? parsed : null;
    }

    this.queryParamSub = this.route.queryParamMap.subscribe(params => {
      if (this.skipNextQueryParamHandling) {
        this.skipNextQueryParamHandling = false;
        return;
      }

      const qpLesson = params.get('lessonId');
      if (!qpLesson) {
        return;
      }

      const parsedLessonId = Number(qpLesson);
      if (!Number.isFinite(parsedLessonId)) {
        return;
      }

      if (this.lessonId !== parsedLessonId) {
        this.lessonId = parsedLessonId;
        this.selectLesson(parsedLessonId, true);
      }
    });
    console.log('[LessonPage] ngOnInit route params:', { moduleId: this.moduleId, lessonId: this.lessonId, courseId: courseParam });

    // Get current user
    const user = this.userService.getUser();
    if (user) {
      this.userId = user.user_id;
    }

    // If we have a courseId route param prefer loading full course (modules + lessons)
    if (courseParam) {
      const cid = Number(courseParam);
      this.loadCourseModules(cid).then(() => {
        // Load watched videos
        if (this.userId) {
          this.loadWatchedVideos().then(() => {
            this.markCompletedLessons();
          });
        }

        // if lesson specified, select it, otherwise pick first lesson in first module
        if (this.lessonId) {
          this.selectLesson(this.lessonId);
        } else {
          const firstModule = this.fullModules && this.fullModules.length ? this.fullModules[0] : null;
          const first = firstModule && firstModule.videos && firstModule.videos.length ? (firstModule.videos[0].id || firstModule.videos[0].videoId) : null;
          if (first) this.selectLesson(first as number);
        }
      });
      return;
    }

    // otherwise fall back to loading a single module when moduleId is present
    if (this.moduleId) {
      this.loadModule(this.moduleId).then(() => {
        // when module response includes courseId, also attempt to load the full course modules
        try {
          const mayModule = (this as any).lessons && (this as any).lessons; // keep compatibility
        } catch (e) {}
        if (!this.lessonId) {
          // default to first lesson
          const first = this.lessons.length ? this.lessons[0].id || this.lessons[0].videoId || this.lessons[0].id : null;
          if (first) {
            this.selectLesson(first as number);
          }
        } else {
          this.selectLesson(this.lessonId);
        }
      });
    } else if (this.lessonId) {
      this.selectLesson(this.lessonId);
    }
  }

  private async loadModule(id: number) {
    this.loading = true;
    this.error = null;
    try {
      const mod = await this.courseService.getModule(id).toPromise();
      // backend DetailedModuleDto contains videos: List<VideoDto>
      this.lessons = mod?.videos || [];
      this.lessonCount = this.lessons.length;
      // set selected module when loading a single module route
      this.selectedModule = mod;
      this.selectedModuleTitle = mod && (mod.moduleName || mod.moduleTitle || mod.name) || null;
      try {
        const key = (mod && (mod.id || mod.moduleId || mod.moduleName) || '').toString().replace(/\s+/g, '_');
        this.moduleCollapsed[key] = false;
      } catch (e) {}
      // if module includes courseId try to load full course modules
      if (mod && mod.courseId) {
        await this.loadCourseModules(mod.courseId);
      } else {
        // Only build grouped modules from single module if we don't have a courseId
        // (i.e., we won't be loading the full course modules)
        this.groupedModules = this.buildGroupedFromLessons(this.lessons, mod);
      }
    } catch (err: any) {
      this.error = err?.message || 'Failed to load module';
    } finally {
      this.loading = false;
    }
  }

  // load the full course (modules and lessons) to show entire course outline in sidebar
  private async loadCourseModules(courseId: number) {
    this.loading = true;
    this.error = null;
    try {
      const res: any = await this.courseService.getCourse(courseId).toPromise();
      // backend returns ApiResponse with data being the course DTO which typically includes modules
      console.log('[LessonPage] loadCourseModules response:', res);
      const course = res && res.data ? res.data : res;
  // set course title for header
  this.courseTitle = course && (course.name || course.title) || null;
      console.log('[LessonPage] resolved course object:', course);
      // normalize different possible property names for modules
      let modulesArray: any[] = [];
      if (course && Array.isArray(course.modules)) modulesArray = course.modules;
      else if (course && Array.isArray(course.courseModules)) modulesArray = course.courseModules;
      else if (course && Array.isArray(course.courseModulesList)) modulesArray = course.courseModulesList;

      // unwrap wrapper objects (some backends return { module: {...}, moduleOrder } entries)
      this.fullModules = modulesArray.map((m: any) => {
        if (m && m.module) return { ...m.module, _wrapper: m };
        return m;
      });

      // If the modules don't already include videos/lessons, fetch each module's details to obtain its videos
      await Promise.all(this.fullModules.map(async (m: any, idx: number) => {
        try {
          const hasVideos = Array.isArray(m.videos) && m.videos.length > 0;
          const hasLessons = Array.isArray(m.lessons) && m.lessons.length > 0;
          if (!hasVideos && !hasLessons) {
            // try to resolve module id from common fields
            const moduleId = m.moduleId || m.id || m.moduleIdString || (m._wrapper && m._wrapper.module && m._wrapper.module.moduleId);
            if (moduleId) {
              const modDetail: any = await this.courseService.getModule(Number(moduleId)).toPromise();
              // backend returns module details with videos list
              if (modDetail && Array.isArray(modDetail.videos)) {
                m.videos = modDetail.videos;
              } else if (modDetail && Array.isArray(modDetail.lessons)) {
                m.lessons = modDetail.lessons;
              } else {
                m.videos = [];
              }
            } else {
              m.videos = [];
            }
          }
        } catch (e) {
          m.videos = m.videos || [];
        }
      }));

      console.log('[LessonPage] populated fullModules length=', this.fullModules.length);
      console.log('[LessonPage] fullModules data:', this.fullModules);
      // build and cache grouped modules for stable rendering
      this.groupedModules = this.buildGroupedFromFullModules(this.fullModules);
      console.log('[LessonPage] groupedModules length=', this.groupedModules.length);
      console.log('[LessonPage] groupedModules data:', this.groupedModules);
    } catch (err: any) {
      console.error('[LessonPage] Error loading course modules:', err);
      // gracefully ignore and keep fullModules empty
      this.fullModules = [];
    } finally {
      this.loading = false;
    }
  }

  async selectLesson(id: number, skipRouteUpdate = false) {
    const numericId = Number(id);
    if (!Number.isFinite(numericId)) {
      return;
    }
    this.lessonId = numericId;
    this.loadingLesson = true;
    this.error = null;
    // Do NOT clear `lesson` or `videoSrc` immediately — keep current content visible
    // while the new lesson data and video are fetched to avoid layout shifts.
    try {
      // If we have the full course modules loaded, try to locate which module contains this lesson
      if (this.fullModules && this.fullModules.length) {
        const found = this.findModuleForLesson(numericId);
        if (found && found.module) {
          this.selectedModule = found.module;
          // normalize module videos/lessons into this.lessons so prev/next work
          const moduleVideos = Array.isArray(found.module.videos) ? found.module.videos : (Array.isArray(found.module.lessons) ? found.module.lessons : []);
          this.lessons = moduleVideos || [];
          this.lessonCount = this.lessons.length;
          this.lessonIndex = (typeof found.index === 'number' ? found.index + 1 : this.lessonIndex || 1);
          // ensure module is expanded
          const key = (found.module.id || found.module.moduleId || found.module.title || found.module.name || '').toString().replace(/\s+/g, '_');
          this.moduleCollapsed[key] = false;
          this.selectedModuleTitle = found.module.title || found.module.name || found.module.moduleName || null;
        }
      }

  const lesson = await this.courseService.getLesson(numericId).toPromise();
      // Once fetched, replace the displayed lesson
      this.lesson = lesson;

      // process explanation HTML safely
      const rawExplanation = lesson?.explanation || lesson?.description || '';
      // Some backends may include HTML tags; sanitize and store
      this.explanationHtml = this.sanitizer.bypassSecurityTrustHtml(rawExplanation);

      // process whatWeWillLearn: may be array or comma-separated string
      const what = lesson?.whatWeWillLearn || lesson?.whatYouWillLearn || '';
      // load quizzes for the selected lesson's course (if any)
      try {
        await this.loadQuizzesForLesson();
      } catch (e) {
        console.warn('Failed to load quizzes for lesson', e);
      }
      if (Array.isArray(what)) {
        this.whatYouWillLearnList = what.map((s: any) => String(s).trim()).filter(Boolean);
      } else if (typeof what === 'string') {
        this.whatYouWillLearnList = String(what).split(/[,\n;]/).map(s => s.trim()).filter(Boolean);
      }

      // prerequisites: may be string or array
      const prereq = lesson?.prerequisites || '';
      if (Array.isArray(prereq)) {
        this.prerequisitesList = prereq.map((s: any) => String(s).trim()).filter(Boolean);
      } else if (typeof prereq === 'string') {
        this.prerequisitesList = String(prereq).split(/[,\n;]/).map(s => s.trim()).filter(Boolean);
      }

      // attachments: backend may provide attachments: List<AttachmentDto>
      this.attachments = (lesson?.attachments || []).map((a: any) => ({
        id: a?.id,
        fileName: a?.fileName || a?.name || a?.originalFileName || 'attachment',
        fileDownloadUrl: a?.fileDownloadUrl || (a?.id ? `${this.courseService['api']}/api/attachments/${a.id}/download` : undefined)
      }));

      // request presigned URL for video: use video id field
  const videoId = lesson?.id || lesson?.videoId || numericId;
      if (videoId) {
  const url = await this.courseService.getVideoUrl(videoId).toPromise();
        // swap to new source only after it's available
        this.videoSrc = url;
        // initialize player after the source is set
        setTimeout(() => this.loadPlyr(), 0);
      }

      if (!skipRouteUpdate) {
        this.updateLessonRoute(numericId);
      }
    } catch (err: any) {
      this.error = err?.message || 'Failed to load lesson';
    } finally {
      this.loadingLesson = false;
    }
  }

  private updateLessonRoute(lessonId: number) {
    if (this.courseId) {
      this.skipNextQueryParamHandling = true;
      this.router.navigate([], {
        relativeTo: this.route,
        queryParams: { lessonId },
        queryParamsHandling: 'merge',
        replaceUrl: true
      }).finally(() => {
        this.skipNextQueryParamHandling = false;
      });
      return;
    }

    if (this.moduleId) {
      this.skipNextQueryParamHandling = true;
      this.router.navigate(['/module', this.moduleId, 'lesson', lessonId], {
        replaceUrl: true
      }).finally(() => {
        this.skipNextQueryParamHandling = false;
      });
    }
  }

  // try to find the module and index within it that contains the given lesson id
  private findModuleForLesson(id: number | string) {
    if (!id) return null;
    const needle = id as any;
    // look through fullModules first
    for (const m of this.fullModules || []) {
      const items = Array.isArray(m.videos) ? m.videos : (Array.isArray(m.lessons) ? m.lessons : []);
      for (let i = 0; i < items.length; i++) {
        const it = items[i];
        if ((it && ((it.id && it.id === needle) || (it.videoId && it.videoId === needle))) || it === needle) {
          return { module: m, index: i };
        }
      }
    }
    // fallback: try groupedModules
    for (const g of this.groupedModules || []) {
      for (let i = 0; i < (g.items || []).length; i++) {
        const it = g.items[i];
        if ((it && ((it.id && it.id === needle) || (it.videoId && it.videoId === needle))) || it === needle) {
          return { module: g, index: i };
        }
      }
    }
    return null;
  }

  // navigate to previous lesson in the module
  prevLesson() {
    if (!this.lessons || this.lessons.length === 0) return;
    const i = Math.max(0, (this.lessonIndex || 1) - 2); // zero-based
    const target = this.lessons[i];
    if (target) this.selectLesson(target.id || target.videoId || target);
  }

  // navigate to next lesson in the module
  nextLesson() {
    if (!this.lessons || this.lessons.length === 0) return;
    const i = Math.min(this.lessons.length - 1, (this.lessonIndex || 1));
    const target = this.lessons[i];
    if (target) this.selectLesson(target.id || target.videoId || target);
  }


  // initialize Plyr player for the video element
  loadPlyr() {
    const video = document.querySelector('#player') as HTMLMediaElement | null;
    if (!video || !this.videoSrc) {
      console.warn('[LessonPage] loadPlyr: video element or videoSrc not available', { video: !!video, videoSrc: !!this.videoSrc });
      return;
    }

    console.log('[LessonPage] loadPlyr called for lesson:', this.lesson?.id || this.lessonId, 'userId:', this.userId);

    // Cleanup previous progress tracking if exists
    if (this.cleanupProgressTracking) {
      this.cleanupProgressTracking();
      this.cleanupProgressTracking = null;
    }

    // Initialize video progress tracking using the service
    if (this.userId) {
      const currentLessonId = this.lesson?.id || this.lesson?.videoId || this.lessonId;
      if (currentLessonId) {
        this.cleanupProgressTracking = this.videoProgressService.initializeProgressTracking({
          videoElement: video,
          lessonId: currentLessonId,
          userId: this.userId,
          completionThreshold: 90,
          onCompletion: (lessonId) => {
            // Update local state and UI when lesson is marked as completed
            this.watchedVideoIds.add(lessonId);
            this.markCompletedLessons();
          }
        });
      }
    }

    // destroy previous sources
    try {
      // If source is HLS (.m3u8), use hls.js when needed
      const isHls = typeof this.videoSrc === 'string' && this.videoSrc.indexOf('.m3u8') !== -1;
      const PlyrCtor = (require('plyr') as any).default || (require('plyr') as any);
      if (isHls) {
        if (Hls.isSupported()) {
          const hls = new Hls();
          hls.loadSource(this.videoSrc as string);
          hls.attachMedia(video);
          hls.on(Hls.Events.MANIFEST_PARSED, () => {
            // initialize Plyr after hls attach
            // eslint-disable-next-line no-unused-vars
            const player = new PlyrCtor(video as any, { controls: ['play', 'progress', 'current-time', 'mute', 'volume', 'fullscreen'] });
            console.log('[LessonPage] HLS player initialized');
          });
        } else {
          // Fallback to native playback if browser supports
          video.src = this.videoSrc as string;
          const player = new PlyrCtor(video as any, { controls: ['play', 'progress', 'current-time', 'mute', 'volume', 'fullscreen'] });
          console.log('[LessonPage] Native HLS player initialized');
        }
      } else {
        // non-HLS video (mp4 etc)
        // Ensure crossorigin is set so browser performs CORS fetch without credentials
        try { video.setAttribute('crossorigin', 'anonymous'); } catch (e) {}
        video.src = this.videoSrc as string;
        const player = new PlyrCtor(video as any, { controls: ['play', 'progress', 'current-time', 'mute', 'volume', 'fullscreen'] });
        console.log('[LessonPage] Standard video player initialized');
      }
    } catch (e) {
      console.error('[LessonPage] Error initializing player:', e);
    }
  }

  // UI helper from template: toggle notes panel (simple stub)
  toggleNotes() {
    this.showNotes = !this.showNotes;
  }

  // attachments tab helpers
  setAttachmentsTab(tab: 'attachments' | 'quizzes') {
    this.attachmentsTab = tab;
    // reset quiz selection when leaving quizzes
    if (tab !== 'quizzes') this.selectedQuiz = null;
  }

  toggleAttachmentsHide() {
    this.attachmentsHidden = !this.attachmentsHidden;
  }

  // collapse toggle for module groups in sidebar
  toggleModuleCollapse(key: string) {
    this.moduleCollapsed[key] = !this.moduleCollapsed[key];
  }

  // toggle left sidebar visibility
  toggleSidebar() {
    this.showSidebar = !this.showSidebar;
  }

  // load quizzes for the lesson's course (if available)
  private async loadQuizzesForLesson() {
    // try to resolve courseId from lesson metadata or other fields
    let courseId: number | null = null;
    if (!this.lesson) return;
    // backend VideoDto doesn't include explicit courseId on video, but video.metadata or videoUrl may contain mapping
    // Try several places conservatively
    if (this.lesson.metadata && this.lesson.metadata.courseId) {
      courseId = Number(this.lesson.metadata.courseId);
    }
    if (!courseId && (this.lesson.courseId || this.lesson.course?.id)) {
      courseId = this.lesson.courseId || this.lesson.course?.id;
    }
    // If we still don't have courseId but have moduleId, try to find course via module loader (module object may include courseId)
    if (!courseId && this.moduleId) {
      try {
        const mod = await this.courseService.getModule(this.moduleId).toPromise();
        if (mod && mod.courseId) courseId = mod.courseId;
      } catch (e) {
        // ignore
      }
    }

    try {
      this.quizzes = [];
      if (courseId) {
          const q: any = await this.courseService.getQuizzes(courseId).toPromise();
          if (Array.isArray(q)) this.quizzes = q;
          else if (q && Array.isArray(q.data)) this.quizzes = q.data;
          else this.quizzes = q || [];
      }
    } catch (e) {
      // ignore quiz loading errors for now
    }
  }

  // show quiz details/questions
  showQuiz(quiz: any) {
    this.selectedQuiz = quiz;
  }

  // helper to compute grouped modules from lessons
  getGroupedModules() {
    return this.groupedModules || [];
  }

  // Build grouped modules structure from a simple lessons array (single module case)
  buildGroupedFromLessons(lessons: any[], moduleMeta?: any) {
    const title = (moduleMeta && (moduleMeta.moduleName || moduleMeta.moduleTitle || moduleMeta.name)) || 'Module 1 • Content';
    return [{ title, items: lessons || [], key: (moduleMeta && (moduleMeta.moduleId || moduleMeta.id) || title).toString().replace(/\s+/g, '_') }];
  }

  // Build grouped modules structure from fullModules array (already normalized)
  buildGroupedFromFullModules(fullModules: any[]) {
    console.log('[buildGroupedFromFullModules] input fullModules:', fullModules);
    const groups: { title: string; items: any[]; key: string }[] = [];
    for (const m of fullModules || []) {
      const title = m.title || m.name || m.moduleName || m.moduleTitle || 'Module';
      const items = Array.isArray(m.videos) ? m.videos : (Array.isArray(m.lessons) ? m.lessons : []);
      console.log('[buildGroupedFromFullModules] module:', m, 'title:', title, 'items count:', items.length);
      groups.push({ title, items, key: (m.id || m.moduleId || title).toString().replace(/\s+/g, '_') });
    }
    console.log('[buildGroupedFromFullModules] returning groups:', groups);
    return groups;
  }

  // trackBy functions for *ngFor to avoid unnecessary re-renders
  trackByModule(index: number, item: any) {
    return item.key || item.title || index;
  }

  trackByLesson(index: number, item: any) {
    return item?.id || item?.videoId || item?.title || index;
  }

  // UI helper from template: download resources/fallback
  downloadResources() {
    if (this.attachments && this.attachments.length) {
      const a = this.attachments[0];
      if (a.fileDownloadUrl) {
        window.open(a.fileDownloadUrl, '_blank', 'noopener');
      }
    }
  }

  // Load watched videos for the current user
  async loadWatchedVideos(): Promise<void> {
    if (!this.userId) return;

    try {
      this.watchedVideoIds = await this.videoProgressService.loadWatchedVideos(this.userId);
      console.log('[LessonPage] Loaded watched videos:', this.watchedVideoIds.size);
    } catch (err) {
      console.warn('Could not load watched videos:', err);
    }
  }

  // Mark lessons as completed based on watched videos
  markCompletedLessons(): void {
    console.log('[LessonPage] Marking completed lessons. Watched IDs:', Array.from(this.watchedVideoIds));

    const hasFullCourse = Array.isArray(this.fullModules) && this.fullModules.length > 0;

    // Mark lessons in fullModules
    if (hasFullCourse) {
      let totalMarked = 0;
      this.fullModules.forEach(module => {
        const videos = module.videos || module.lessons || [];
        videos.forEach((video: any) => {
          const videoId = video.videoId || video.id;
          video.completed = this.watchedVideoIds.has(videoId);
          if (video.completed) {
            totalMarked++;
            console.log(`  ✓ Lesson ${videoId} marked as completed`);
          }
        });
      });
      console.log(`[LessonPage] Marked ${totalMarked} lessons as completed in fullModules`);

      // Rebuild grouped modules to reflect completion status
      this.groupedModules = this.buildGroupedFromFullModules(this.fullModules);
    }

    // Mark lessons in the lessons array
    if (this.lessons && this.lessons.length > 0) {
      let totalMarked = 0;
      this.lessons.forEach(lesson => {
        const lessonId = lesson.videoId || lesson.id;
        lesson.completed = this.watchedVideoIds.has(lessonId);
        if (lesson.completed) totalMarked++;
      });
      console.log(`[LessonPage] Marked ${totalMarked} lessons as completed in lessons array`);

      // If using lessons (single module), rebuild grouped modules
      if (!hasFullCourse && this.selectedModule) {
        this.groupedModules = this.buildGroupedFromLessons(this.lessons, this.selectedModule);
      }
    }
  }

  ngOnDestroy(): void {
    // Cleanup video progress tracking listeners when component is destroyed
    if (this.cleanupProgressTracking) {
      this.cleanupProgressTracking();
      this.cleanupProgressTracking = null;
      console.log('[LessonPage] Cleaned up video progress tracking on component destroy');
    }

    if (this.queryParamSub) {
      this.queryParamSub.unsubscribe();
      this.queryParamSub = null;
    }
  }
}
