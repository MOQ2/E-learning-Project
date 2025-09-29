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
  videoSrc: string | null | undefined = null;
  loading = false;
  error: string | null = null;
  explanationHtml: SafeHtml | null = null;
  whatYouWillLearnList: string[] = [];
  prerequisitesList: string[] = [];
  attachments: Array<{ id?: number; fileName?: string; fileDownloadUrl?: string }> = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private courseService: CourseService,
    private sanitizer: DomSanitizer
  ) {}

  ngOnInit(): void {
    const modParam = this.route.snapshot.paramMap.get('moduleId');
    const lessonParam = this.route.snapshot.paramMap.get('lessonId');
    this.moduleId = modParam ? Number(modParam) : null;
    this.lessonId = lessonParam ? Number(lessonParam) : null;

    // load module details (which returns module with videos list)
    if (this.moduleId) {
      this.loadModule(this.moduleId).then(() => {
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
    } catch (err: any) {
      this.error = err?.message || 'Failed to load module';
    } finally {
      this.loading = false;
    }
  }

  async selectLesson(id: number) {
    this.loading = true;
    this.error = null;
    this.lesson = null;
    this.videoSrc = null;
    try {
      const lesson = await this.courseService.getLesson(id).toPromise();
      this.lesson = lesson;

      // process explanation HTML safely
      const rawExplanation = lesson?.explanation || lesson?.description || '';
      // Some backends may include HTML tags; sanitize and store
      this.explanationHtml = this.sanitizer.bypassSecurityTrustHtml(rawExplanation);

      // process whatWeWillLearn: may be array or comma-separated string
      const what = lesson?.whatWeWillLearn || lesson?.whatYouWillLearn || '';
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
        this.videoSrc = url;
        // set source on <video> element
        setTimeout(() => this.loadPlyr(), 0);
      }

      // update route to reflect selected lesson
      if (this.moduleId) {
        this.router.navigate(['/module', this.moduleId, 'lesson', id], { replaceUrl: true });
      } else {
        this.router.navigate(['/lesson', id], { replaceUrl: true });
      }
    } catch (err: any) {
      this.error = err?.message || 'Failed to load lesson';
    } finally {
      this.loading = false;
    }
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
        video.src = this.videoSrc as string;
        const player = new PlyrCtor(video as any, { controls: ['play', 'progress', 'current-time', 'mute', 'volume', 'fullscreen'] });
      }
    } catch (e) {
      // ignore initialization errors
      // console.warn('player init err', e);
    }
  }
}
