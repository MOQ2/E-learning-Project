import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { CourseService } from '../../../Services/Courses/course-service';
import { HttpClientModule } from '@angular/common/http';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

// local imports for Plyr and hls.js
import Hls from 'hls.js';

@Component({
  selector: 'app-lesson-page',
  standalone: true,
  imports: [CommonModule, HttpClientModule],
  templateUrl: './lesson-page.html',
  styleUrls: ['./lesson-page.css']
})
export class LessonPage implements OnInit {
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

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private courseService: CourseService,
    private sanitizer: DomSanitizer
  ) {}

  ngOnInit(): void {
    const modParam = this.route.snapshot.paramMap.get('moduleId');
    const lessonParam = this.route.snapshot.paramMap.get('lessonId');
    const courseParam = this.route.snapshot.paramMap.get('courseId');
    this.moduleId = modParam ? Number(modParam) : null;
    this.lessonId = lessonParam ? Number(lessonParam) : null;
    console.log('[LessonPage] ngOnInit route params:', { moduleId: this.moduleId, lessonId: this.lessonId, courseId: courseParam });

    // If we have a courseId route param prefer loading full course (modules + lessons)
    if (courseParam) {
      const cid = Number(courseParam);
      this.loadCourseModules(cid).then(() => {
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
  // build grouped modules from the single-loaded module so sidebar has a stable structure
  this.groupedModules = this.buildGroupedFromLessons(this.lessons, mod);
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
        this.loadCourseModules(mod.courseId).catch(() => {});
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
      // build and cache grouped modules for stable rendering
      this.groupedModules = this.buildGroupedFromFullModules(this.fullModules);
    } catch (err: any) {
      // gracefully ignore and keep fullModules empty
      this.fullModules = [];
    } finally {
      this.loading = false;
    }
  }

  async selectLesson(id: number) {
    this.loadingLesson = true;
    this.error = null;
    // Do NOT clear `lesson` or `videoSrc` immediately — keep current content visible
    // while the new lesson data and video are fetched to avoid layout shifts.
    try {
      // If we have the full course modules loaded, try to locate which module contains this lesson
      if (this.fullModules && this.fullModules.length) {
        const found = this.findModuleForLesson(id);
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

      const lesson = await this.courseService.getLesson(id).toPromise();
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
      const videoId = lesson?.id || lesson?.videoId || id;
      if (videoId) {
        const url = await this.courseService.getVideoUrl(videoId).toPromise();
        // swap to new source only after it's available
        this.videoSrc = url;
        // initialize player after the source is set
        setTimeout(() => this.loadPlyr(), 0);
      }

      // update route to reflect selected lesson
      // Removed navigation to prevent page refresh
    } catch (err: any) {
      this.error = err?.message || 'Failed to load lesson';
    } finally {
      this.loadingLesson = false;
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

  // open syllabus modal or route (stub for now)
  openSyllabus() {
    // placeholder: future implementation
    console.log('open syllabus');
  }

  // initialize Plyr player for the video element
  loadPlyr() {
    const video = document.querySelector('#player') as HTMLMediaElement | null;
    if (!video || !this.videoSrc) return;

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
          });
        } else {
          // Fallback to native playback if browser supports
          video.src = this.videoSrc as string;
          const player = new PlyrCtor(video as any, { controls: ['play', 'progress', 'current-time', 'mute', 'volume', 'fullscreen'] });
        }
      } else {
        // non-HLS video (mp4 etc)
        // Ensure crossorigin is set so browser performs CORS fetch without credentials
        try { video.setAttribute('crossorigin', 'anonymous'); } catch (e) {}
        video.src = this.videoSrc as string;
        const player = new PlyrCtor(video as any, { controls: ['play', 'progress', 'current-time', 'mute', 'volume', 'fullscreen'] });
      }
    } catch (e) {
      // ignore initialization errors
      // console.warn('player init err', e);
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
    const groups: { title: string; items: any[]; key: string }[] = [];
    for (const m of fullModules || []) {
      const title = m.title || m.name || m.moduleName || m.moduleTitle || 'Module';
      const items = Array.isArray(m.videos) ? m.videos : (Array.isArray(m.lessons) ? m.lessons : []);
      groups.push({ title, items, key: (m.id || m.moduleId || title).toString().replace(/\s+/g, '_') });
    }
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
}
