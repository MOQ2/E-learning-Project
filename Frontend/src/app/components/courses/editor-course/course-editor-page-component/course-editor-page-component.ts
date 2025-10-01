import { Component, OnInit, OnDestroy, ViewChild, Renderer2, Inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormGroup } from '@angular/forms';
import {CourseoverviewCreationDto } from '../course-overview/enums/CourseOverviewCreationDto';
import { CourseFormComponent } from '../course-overview/course-form/course-form';
import { SidebarComponent } from '../course-overview/sidebar/sidebar';
import { NavBar } from '../../../nav-bar/nav-bar';
import { ViewEncapsulation } from '@angular/core';
import { CourseService } from '../../../../Services/Courses/course-service';
import { ToastService } from '../../../../Services/ToastService/toast-service';
import { environment } from '../../../../../environments/environment';
import { Observable, forkJoin } from 'rxjs';
import { ModuleFormComponent } from '../module-editor/module-form/module-form';
import { LessonEditorComponent } from '../lesson-editor/lesson-from/lesson-from';
import { ModulesListComponent } from '../module-editor/modules-list/modules-list';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Lesson } from '../lesson-editor/lesson-page/lesson.model';
import { DOCUMENT } from '@angular/common';

export interface Module {
  id?: number;
  title: string;
  summary: string;
  order: number;
  estimatedTime: number;
  isOptional: boolean;
  status: 'Active' | 'Inactive';
  lessons?: Lesson[];
  state?: 'saved' | 'edited' | 'new';
}

@Component({
  selector: 'app-course-editor-page',
  templateUrl: './course-editor-page-component.html',
  styleUrl: './course-editor-page-component.css',
  imports: [CourseFormComponent, SidebarComponent, NavBar, ModuleFormComponent, LessonEditorComponent, ModulesListComponent, FormsModule, CommonModule],
  standalone: true,
  encapsulation: ViewEncapsulation.None
})
export class CourseEditorPageComponent implements OnInit, OnDestroy {

  @ViewChild(LessonEditorComponent) lessonEditor!: LessonEditorComponent;

  activePage: 'overview' | 'modules' | 'lesson' | 'publish' = 'overview';

  // Track if course has been created (has ID)
  courseCreated: boolean = false;

  // Loading states for UI feedback
  isLoadingCourse: boolean = false;
  isUploadingFile: boolean = false;
  isSavingCourse: boolean = false;
  isSavingModule: boolean = false;
  isSavingLesson: boolean = false;
  isLoadingModule: boolean = false;

  currentLesson: Lesson  = {
    id: undefined,
    order: 0,
    title: '',
    explanation: '',
    whatWeWillLearn: [''],
    duration: 0,
    status: 'Active',
    attachments: [],
    prerequisites: [''],
    state: 'new'
  };

  currentModule: Module  = {
    id: undefined,
    title: '',
    summary: '',
    order: 0,
    estimatedTime: 0,
    isOptional: false,
    status: 'Inactive',
    lessons: [],
    state: 'new'
  };

  // Sample modules to power the sidebar list
  modules: Module[] = []

  onModuleSelected(module: Module) {
    console.debug('[CourseEditor] onModuleSelected called for module:', module);
    // If module has an id and lessons are not loaded yet, fetch module details (lazy load)
    if (module.id && (!module.lessons || module.lessons.length === 0)) {
      // Use forkJoin to load module metadata and the module lessons endpoint
      this.isLoadingModule = true;
      forkJoin({
        mod: this.courseService.getModule(module.id),
        lessons: this.courseService.getModuleLessons(module.id)
      }).subscribe({
        next: ({ mod, lessons }) => {
          this.isLoadingModule = false;
          console.debug('[CourseEditor] forkJoin results for module', module.id, { mod, lessons });
          // `getModule` maps to response.data already, so `mod` should be the module payload.
          const payload = mod || {};
          // `getModuleLessons` maps to response.data which should be an array. Be defensive.
          let lessonList: any[] = [];
          if (Array.isArray(lessons)) {
            lessonList = lessons;
          } else if (lessons && Array.isArray((lessons as any).data)) {
            lessonList = (lessons as any).data;
          }
          console.debug('[CourseEditor] lessons array after initial parse:', lessonList);
          // If the lessons endpoint returned nothing, fall back to module's embedded videos (if any)
          if ((!lessonList || lessonList.length === 0) && Array.isArray(payload?.videos)) {
            console.debug('[CourseEditor] Falling back to payload.videos');
            lessonList = payload.videos;
          } else if ((!lessonList || lessonList.length === 0) && Array.isArray(payload?.lessons)) {
            console.debug('[CourseEditor] Falling back to payload.lessons');
            lessonList = payload.lessons;
          }

          console.debug('[CourseEditor] final lessonList used for mapping:', lessonList);
          let mappedLessons = (lessonList || []).map((v: any) => this.convertVideoDtoToLesson(v));

          // NOTE: getModuleLessons endpoint returns lessons WITH order from ModuleVideos join table
          // We should NOT fetch individual lessons just to get order, as getLesson() doesn't have module context
          // Only fetch if duration is truly missing (not just 0, which could be a valid short video)
          const missingDetails = mappedLessons.filter(l => {
            // Check if duration is actually missing (undefined/null) not just 0
            const hasMissingDuration = (l.duration === undefined || l.duration === null);
            return hasMissingDuration && l.id;
          });
          if (missingDetails.length > 0) {
            console.debug('[CourseEditor] fetching full lesson details for missing duration', missingDetails.map(m => m.id));
            const lessonFetches = missingDetails.map(l => this.courseService.getLesson(l.id!));
            forkJoin(lessonFetches).subscribe({
              next: (detailedLessons: any[]) => {
                console.debug('[CourseEditor] fetched detailed lessons:', detailedLessons);
                // Only update duration from detailed fetch, preserve order from ModuleVideos
                const detailedMap = new Map<number, any>();
                detailedLessons.forEach(dl => {
                  if (dl.id) detailedMap.set(dl.id, dl);
                });
                mappedLessons = mappedLessons.map(l => {
                  if (l.id && detailedMap.has(l.id)) {
                    const detail = detailedMap.get(l.id);
                    // Only update duration, keep order from ModuleVideos
                    return { ...l, duration: detail.durationSeconds || l.duration };
                  }
                  return l;
                });

                const updatedModule: Module = {
                  id: payload?.id || module.id,
                  title: payload?.moduleName || payload?.title || module.title,
                  summary: payload?.moduleDescription || payload?.summary || module.summary,
                  order: payload?.order || module.order,
                  estimatedTime: payload?.estimatedDuration || module.estimatedTime || 0,
                  isOptional: payload?.isOptional || module.isOptional || false,
                  status: payload?.isActive ? 'Active' : (module.status || 'Inactive'),
                  lessons: mappedLessons,
                  state: 'saved'
                };
                const idx = this.modules.findIndex(m => m.id === updatedModule.id);
                if (idx !== -1) this.modules[idx] = updatedModule; else this.modules.push(updatedModule);
                this.currentModule = updatedModule;
                this.activePage = 'modules';
              },
              error: (err) => {
                console.error('[CourseEditor] error fetching detailed lessons:', err);
                this.toast.error('Failed to fetch some lesson details');
                // Fallback to using whatever we have from ModuleVideos
                const updatedModule: Module = {
                  id: payload?.id || module.id,
                  title: payload?.moduleName || payload?.title || module.title,
                  summary: payload?.moduleDescription || payload?.summary || module.summary,
                  order: payload?.order || module.order,
                  estimatedTime: payload?.estimatedDuration || module.estimatedTime || 0,
                  isOptional: payload?.isOptional || module.isOptional || false,
                  status: payload?.isActive ? 'Active' : (module.status || 'Inactive'),
                  lessons: mappedLessons,
                  state: 'saved'
                };
                const idx = this.modules.findIndex(m => m.id === updatedModule.id);
                if (idx !== -1) this.modules[idx] = updatedModule; else this.modules.push(updatedModule);
                this.currentModule = updatedModule;
                this.activePage = 'modules';
              }
            });
            return; // wait for detailed lessons branch to set state
          }

          const updatedModule: Module = {
            id: payload?.id || module.id,
            title: payload?.moduleName || payload?.title || module.title,
            summary: payload?.moduleDescription || payload?.summary || module.summary,
            order: payload?.order || module.order,
            estimatedTime: payload?.estimatedDuration || module.estimatedTime || 0,
            isOptional: payload?.isOptional || module.isOptional || false,
            status: payload?.isActive ? 'Active' : (module.status || 'Inactive'),
            lessons: mappedLessons,
            state: 'saved'
          };
          const idx = this.modules.findIndex(m => m.id === updatedModule.id);
          if (idx !== -1) this.modules[idx] = updatedModule; else this.modules.push(updatedModule);
          this.currentModule = updatedModule;
          this.activePage = 'modules';
        },
        error: (err: any) => {
          this.isLoadingModule = false;
          console.error('[CourseEditor] Failed to load module details or lessons:', err);
          this.toast.error('Failed to load module details');
          this.currentModule = module;
          this.activePage = 'modules';
        }
      });
    } else {
      console.debug('[CourseEditor] Module already has lessons or no id; using cached/local module', module);
      this.currentModule = module;
      this.activePage = 'modules';
    }
  }

  onAddModule() {
    const nextOrder = this.modules.length + 1;
    const newModule: Module = { id: undefined, title: `New module ${nextOrder}`, summary: '', order: nextOrder, estimatedTime: 0, isOptional: false, status: 'Inactive', lessons: [], state: 'new' };
    this.modules = [...this.modules, newModule];
  }


  currentCourseOverview: CourseoverviewCreationDto | null = {
    id: null,
    name: '',
    description: '',
    estimatedDurationInHours: 0,
    difficultyLevel: 'BIGINNER',
    status: 'DRAFT',
    currency: 'USD',
    category: '',
    thumbnail: 0,
    thumbnailName: '',
    tags: [],
    pricing: {
      oneTimePrice: 0,
      allowsSubscription: false,
      subscriptionPriceMonthly: 0,
      subscriptionPrice3Months: 0,
      subscriptionPrice6Months: 0,
    },
    isActive: false
  };

  // This will hold the latest state of the form from the child component
  private courseFormState!: FormGroup;
  selectedFile: File | null = null;
  private quillLink: HTMLLinkElement | null = null;
  private quillScript: HTMLScriptElement | null = null;

  constructor(private courseService: CourseService, private toast: ToastService, private renderer: Renderer2, @Inject(DOCUMENT) private document: Document, private route: ActivatedRoute, private router: Router) {}

  ngOnInit(): void {
    // Dynamically load Quill.js CSS and JS for this component
    this.quillLink = this.renderer.createElement('link');
    this.renderer.setAttribute(this.quillLink, 'rel', 'stylesheet');
    this.renderer.setAttribute(this.quillLink, 'href', 'https://cdn.quilljs.com/1.3.6/quill.snow.css');
    this.renderer.appendChild(this.document.head, this.quillLink);

    this.quillScript = this.renderer.createElement('script');
    this.renderer.setAttribute(this.quillScript, 'src', 'https://cdn.quilljs.com/1.3.6/quill.js');
    this.renderer.appendChild(this.document.head, this.quillScript);

    // fetch set up the courses data here if needed
    // If a courseId param is provided navigate to edit mode and load the course
    const courseIdParam = this.route.snapshot.paramMap.get('courseId');
    if (courseIdParam) {
      const id = Number(courseIdParam);
      if (!isNaN(id)) {
        this.loadCourseForEdit(id);
      }
    }
  }

  ngOnDestroy(): void {
    // Clean up dynamically added Quill resources
    if (this.quillLink && this.document.head.contains(this.quillLink)) {
      this.renderer.removeChild(this.document.head, this.quillLink);
    }
    if (this.quillScript && this.document.head.contains(this.quillScript)) {
      this.renderer.removeChild(this.document.head, this.quillScript);
    }
  }

  onActiveLinkChange(link: 'overview' | 'modules' | 'lesson' | 'publish') {
    this.activePage = link;
  }

  // Captures the form group emitted from the course-form component
  handleFormChange(formGroup: FormGroup) {
    this.courseFormState = formGroup;
  }

  onFileSelected(file: File) {
    this.selectedFile = file;
  }

  // --- Action Handlers ---

  saveDraft() {
    console.log("Saving Draft:", this.courseFormState.getRawValue());
    // Add logic to save to a backend service
  }

  preview() {
    console.log("Previewing Course...");
  }

  onContinue() {
    if (this.courseFormState && this.courseFormState.valid) {
      if (this.selectedFile) {
        this.isUploadingFile = true;
        this.uploadFile(this.selectedFile).subscribe({
          next: (attachmentId) => {
            this.isUploadingFile = false;
            this.toast.success('Thumbnail uploaded successfully');
            const raw = this.courseFormState.getRawValue();
            let courseData = { ...raw };
            delete courseData.thumbnailName;
            courseData.thumbnail = attachmentId;
            // Flatten pricing fields into root
            if (courseData.pricing) {
              courseData = { ...courseData, ...courseData.pricing };
              delete courseData.pricing;
            }
            console.log ("attachment id is ", attachmentId);
            this.createOrUpdateCourse(courseData);
          },
          error: (error) => {
            this.isUploadingFile = false;
            this.toast.error('Failed to upload thumbnail');
            console.error('Upload error:', error);
          }
        });
      } else {
        const raw = this.courseFormState.getRawValue();
        let courseData = { ...raw };
        delete courseData.thumbnailName;
        // Flatten pricing fields into root
        if (courseData.pricing) {
          courseData = { ...courseData, ...courseData.pricing };
          delete courseData.pricing;
        }
        this.createOrUpdateCourse(courseData);
      }
      console.log("Form is valid. Continuing to next step...");
      console.log("response is ", this.currentCourseOverview)
    } else {
      console.log("Form is invalid. Cannot continue.");
      this.toast.error('Please fill in all required fields');
      if (this.courseFormState) {
        this.courseFormState.markAllAsTouched(); // Show validation errors
      }
    }
  }

  private uploadFile(file: File): Observable<number> {
    return this.courseService.uploadAttachment(file, this.currentCourseOverview?.thumbnailName ?? '');
  }

  private createOrUpdateCourse(courseData: any) {
    console.log('createOrUpdateCourse called with courseData:', courseData);
    console.log('currentCourseOverview.id:', this.currentCourseOverview?.id);

    this.isSavingCourse = true;

    if (this.currentCourseOverview?.id) {
      // Update existing course
      console.log('Updating course with id:', this.currentCourseOverview.id);
      this.courseService.updateCourse(this.currentCourseOverview.id, courseData).subscribe({
        next: (response) => {
          this.isSavingCourse = false;
          console.log('Course updated successfully:', response);
          if (!response) {
            console.error('Empty response from updateCourse; aborting update of local state.');
            this.toast.error('Failed to update course');
            return;
          }
          this.toast.success('Course updated successfully');
          // Update the course with the returned data
          const courseDetails = response;
          this.currentCourseOverview = {
            id: courseDetails.id,
            name: courseDetails.name,
            description: courseDetails.description,
            estimatedDurationInHours: courseDetails.estimatedDurationInHours,
            difficultyLevel: courseDetails.difficultyLevel,
            status: courseDetails.status,
            currency: courseDetails.currency,
            category: courseDetails.category,
            thumbnail: courseDetails.thumbnail,
            thumbnailName: '',
            tags: courseDetails.tags ? Array.from(courseDetails.tags).map((tag: any) => tag.name || tag) : [],
            pricing: {
              oneTimePrice: courseDetails.oneTimePrice,
              allowsSubscription: courseDetails.allowsSubscription,
              subscriptionPriceMonthly: courseDetails.subscriptionPriceMonthly,
              subscriptionPrice3Months: courseDetails.subscriptionPrice3Months,
              subscriptionPrice6Months: courseDetails.subscriptionPrice6Months,
            },
            isActive: courseDetails.isActive
          };
          this.courseCreated = true;
          this.activePage = 'modules'; // Navigate to modules after update
        },
        error: (error) => {
          this.isSavingCourse = false;
          console.error('Error updating course:', error);
          this.toast.error('Failed to update course');
        }
      });
    } else {
      // Create new course
      console.log('Creating new course');
      this.courseService.createCourse(courseData).subscribe({
        next: (response) => {
          this.isSavingCourse = false;
          console.log('Course created successfully, response:', response);
          if (!response) {
            console.error('Empty response from createCourse; aborting update of local state.');
            this.toast.error('Failed to create course');
            return;
          }
          this.toast.success('Course created successfully');
          // Update the course with the returned data to preserve form values
          if (this.currentCourseOverview) {
            const courseDetails = response; // response is already the data from the service
            console.log('courseDetails:', courseDetails);
            console.log('courseDetails.id:', courseDetails?.id);
            this.currentCourseOverview = {
              id: courseDetails.id,
              name: courseDetails.name,
              description: courseDetails.description,
              estimatedDurationInHours: courseDetails.estimatedDurationInHours,
              difficultyLevel: courseDetails.difficultyLevel,
              status: courseDetails.status,
              currency: courseDetails.currency,
              category: courseDetails.category,
              thumbnail: courseDetails.thumbnail,
              thumbnailName: '',
              tags: courseDetails.tags ? Array.from(courseDetails.tags).map((tag: any) => tag.name || tag) : [],
              pricing: {
                oneTimePrice: courseDetails.oneTimePrice,
                allowsSubscription: courseDetails.allowsSubscription,
                subscriptionPriceMonthly: courseDetails.subscriptionPriceMonthly,
                subscriptionPrice3Months: courseDetails.subscriptionPrice3Months,
                subscriptionPrice6Months: courseDetails.subscriptionPrice6Months,
              },
              isActive: courseDetails.isActive
            };
            console.log('Updated currentCourseOverview:', this.currentCourseOverview);
            this.courseCreated = true;
            this.activePage = 'modules'; // Navigate to modules after creation
          }
        },
        error: (error) => {
          this.isSavingCourse = false;
          console.error('Error creating course:', error);
          this.toast.error('Failed to create course');
        }
      });
    }
  }

  onSaveLesson(lesson: Lesson) {
    console.log('Saving lesson:', lesson);
    if (!this.currentModule.id) {
      console.error('No module selected');
      return;
    }
    this.saveLesson(lesson);
  }

  onSaveDraftLesson(lesson: Lesson) {
    console.log('Saving draft lesson:', lesson);
    if (!this.currentModule.id) {
      console.error('No module selected');
      return;
    }
    this.saveLesson(lesson);
  }

  private saveLesson(lesson: Lesson) {
    // First, handle attachments and thumbnail: upload new ones and collect IDs
    const uploads: Observable<number>[] = [];
    const attachmentIds: number[] = [];
    let thumbnailId: number | undefined;

    lesson.attachments.forEach(att => {
      if (att.file) {
        uploads.push(this.courseService.uploadAttachment(att.file, att.displayName));
      } else if (att.id) {
        // Existing attachment
        attachmentIds.push(att.id);
      }
    });

    if (lesson.thumbnailFile) {
      uploads.push(this.courseService.uploadAttachment(lesson.thumbnailFile, 'thumbnail'));
    }

    if (uploads.length > 0) {
      // Upload all new files
      this.isUploadingFile = true;
      forkJoin(uploads).subscribe({
        next: (uploadedIds: number[]) => {
          this.isUploadingFile = false;
          this.toast.success('Files uploaded successfully');
          // First ID is thumbnail if present
          let index = 0;
          if (lesson.thumbnailFile) {
            thumbnailId = uploadedIds[index++];
          }
          // Remaining are attachments
          for (let i = index; i < uploadedIds.length; i++) {
            attachmentIds.push(uploadedIds[i]);
          }
          this.createOrUpdateLesson(lesson, attachmentIds, thumbnailId);
        },
        error: (error: any) => {
          this.isUploadingFile = false;
          console.error('Error uploading files:', error);
          this.toast.error('Failed to upload files');
        }
      });
    } else {
      this.createOrUpdateLesson(lesson, attachmentIds, thumbnailId);
    }
  }

  private createOrUpdateLesson(lesson: Lesson, attachmentIds: number[], thumbnailId?: number) {
    const lessonData = new FormData();
    lessonData.append('title', lesson.title);
    lessonData.append('explanation', lesson.explanation);
    lessonData.append('order', lesson.order.toString());
    lessonData.append('durationSeconds', lesson.duration.toString());
    lessonData.append('status', lesson.status);

    if (lesson.videoData) {
      if (lesson.id) {
        // update
        lessonData.append('video', lesson.videoData);
      } else {
        // create
        lessonData.append('file', lesson.videoData);
      }
    }
    if (thumbnailId) {
      lessonData.append('thumbnail', thumbnailId.toString());
    }

    lessonData.append('whatWeWillLearn', lesson.whatWeWillLearn.join(', '));
    lessonData.append('prerequisites', lesson.prerequisites.join(', '));

    this.isSavingLesson = true;

    if (lesson.id) {
      // Update existing lesson
      this.courseService.updateLesson(lesson.id, lessonData).subscribe({
        next: (response) => {
          console.log('Lesson updated:', response);

          // Now update the order in ModuleVideos join table
          if (this.currentModule?.id) {
            this.courseService.updateLessonOrderInModule(this.currentModule.id, lesson.id!, lesson.order).subscribe({
              next: () => {
                this.isSavingLesson = false;
                this.toast.success('Lesson and order updated successfully');
                lesson.state = 'saved';

                // Update attachments: add new ones to lesson
                attachmentIds.forEach(attId => {
                  this.courseService.addAttachmentToLesson(lesson.id!, attId).subscribe({
                    next: () => console.log('Attachment added to lesson'),
                    error: (err) => console.error('Error adding attachment:', err)
                  });
                });

                // Update lesson in current module
                this.updateLessonInModule(lesson);
              },
              error: (orderError) => {
                this.isSavingLesson = false;
                console.error('Error updating lesson order:', orderError);
                this.toast.error('Lesson updated but failed to update order');
                lesson.state = 'saved'; // Lesson content was saved even if order failed
                this.updateLessonInModule(lesson);
              }
            });
          } else {
            // No module context, just update lesson content
            this.isSavingLesson = false;
            this.toast.success('Lesson updated successfully');
            lesson.state = 'saved';
            attachmentIds.forEach(attId => {
              this.courseService.addAttachmentToLesson(lesson.id!, attId).subscribe({
                next: () => console.log('Attachment added to lesson'),
                error: (err) => console.error('Error adding attachment:', err)
              });
            });
            this.updateLessonInModule(lesson);
          }
        },
        error: (error) => {
          this.isSavingLesson = false;
          console.error('Error updating lesson:', error);
          this.toast.error('Failed to update lesson');
        }
      });
    } else {
      // Create new lesson
      this.courseService.createLesson(lessonData).subscribe({
        next: (lessonId: number) => {
          this.isSavingLesson = false;
          console.log('Lesson created with id:', lessonId);
          this.toast.success('Lesson created successfully');
          lesson.id = lessonId;
          lesson.state = 'saved';
          // Add attachments to lesson
          attachmentIds.forEach(attId => {
            this.courseService.addAttachmentToLesson(lessonId, attId).subscribe({
              next: () => console.log('Attachment added to lesson'),
              error: (err) => console.error('Error adding attachment:', err)
            });
          });
          // Add lesson to module
          this.courseService.addLessonToModule(this.currentModule.id!, lessonId, lesson.order).subscribe({
            next: () => {
              console.log('Lesson added to module');
              this.addLessonToModule(lesson);
            },
            error: (err) => console.error('Error adding lesson to module:', err)
          });
        },
        error: (error) => {
          this.isSavingLesson = false;
          console.error('Error creating lesson:', error);
          this.toast.error('Failed to create lesson');
        }
      });
    }
  }

  private updateLessonInModule(lesson: Lesson) {
    if (!this.currentModule.lessons) {
      this.currentModule.lessons = [];
    }
    const index = this.currentModule.lessons.findIndex(l => l.id === lesson.id);
    if (index !== -1) {
      this.currentModule.lessons[index] = { ...lesson };
    } else {
      this.currentModule.lessons.push({ ...lesson });
    }
  }

  private addLessonToModule(lesson: Lesson) {
    if (!this.currentModule.lessons) {
      this.currentModule.lessons = [];
    }
    this.currentModule.lessons.push({ ...lesson });
  }

  onPreviewLesson(lesson: Lesson) {
    console.log('Previewing lesson:', lesson);
    // Implement preview logic
  }

  onGoBackFromLesson() {
    this.activePage = 'modules';
  }

  onAddLesson() {
    // Create a new lesson
    this.currentLesson = {
      order: 0,
      title: '',
      explanation: '',
      whatWeWillLearn: [''],
      duration: 0,
      status: 'Active',
      attachments: [],
      prerequisites: [''],
      state: 'new'
    };
    this.activePage = 'lesson';
  }

  onEditLesson(lesson: Lesson) {
    if (lesson.id) {
      // Fetch the full lesson data from backend WITH module context to get order
      if (this.currentModule?.id) {
        // Use the new endpoint that includes order from ModuleVideos join table
        this.courseService.getLessonInModule(this.currentModule.id, lesson.id).subscribe({
          next: (videoDto: any) => {
            // Convert VideoDto to Lesson model (now includes order!)
            const converted = this.convertVideoDtoToLesson(videoDto);
            this.currentLesson = converted;
            // Cache/update lesson inside currentModule.lessons if present
            if (this.currentModule && this.currentModule.lessons) {
              const idx = this.currentModule.lessons.findIndex(l => l.id === converted.id);
              if (idx !== -1) {
                this.currentModule.lessons[idx] = converted;
              } else {
                this.currentModule.lessons.push(converted);
              }
            }
            this.activePage = 'lesson';
          },
          error: (error) => {
            console.error('Error fetching lesson with module context:', error);
            this.toast.error('Failed to load lesson');
          }
        });
      } else {
        // Fallback: no module context, fetch lesson without order
        this.courseService.getLesson(lesson.id).subscribe({
          next: (videoDto: any) => {
            // Convert VideoDto to Lesson model
            const converted = this.convertVideoDtoToLesson(videoDto);
            this.currentLesson = converted;
            // Cache/update lesson inside currentModule.lessons if present
            if (this.currentModule && this.currentModule.lessons) {
              const idx = this.currentModule.lessons.findIndex(l => l.id === converted.id);
              if (idx !== -1) {
                this.currentModule.lessons[idx] = converted;
              } else {
                this.currentModule.lessons.push(converted);
              }
            }
            this.activePage = 'lesson';
          },
          error: (error: any) => {
            console.error('Error fetching lesson:', error);
            // Fallback to local data
            // Ensure existing attachments (and thumbnail) have download links
            const mappedAttachments = (lesson.attachments ?? []).map(att => ({
              ...att,
              downloadUrl: att.id ? this.getAttachmentDownloadUrl(att.id) : undefined
            }));
            this.currentLesson = { ...lesson, attachments: mappedAttachments };
            this.activePage = 'lesson';
          }
        });
      }
    } else {
      // New lesson, use as is
      const mappedAttachments = (lesson.attachments ?? []).map(att => ({
        ...att,
        downloadUrl: att.id ? this.getAttachmentDownloadUrl(att.id) : undefined
      }));
      this.currentLesson = { ...lesson, attachments: mappedAttachments };
      this.activePage = 'lesson';
    }
  }

  onDeleteLesson(lesson: Lesson) {
    if (lesson.id) {
      this.courseService.deleteLesson(lesson.id).subscribe({
        next: () => {
          console.log('Lesson deleted');
          // Remove from current module
          if (this.currentModule.lessons) {
            this.currentModule.lessons = this.currentModule.lessons.filter(l => l.id !== lesson.id);
          }
        },
        error: (error: any) => console.error('Error deleting lesson:', error)
      });
    } else {
      // If no id, just remove from local array
      if (this.currentModule.lessons) {
        this.currentModule.lessons = this.currentModule.lessons.filter(l => l !== lesson);
      }
    }
  }

  onDeleteAttachment(data: {lessonId: number, attachmentId: number}) {
    this.courseService.removeAttachmentFromLesson(data.lessonId, data.attachmentId).subscribe({
      next: () => {
        console.log('Attachment removed from lesson');
        // Update the current lesson's attachments
        if (this.currentLesson.attachments) {
          this.currentLesson.attachments = this.currentLesson.attachments.filter(att => att.id !== data.attachmentId);
        }
      },
      error: (error: any) => console.error('Error removing attachment:', error)
    });
  }

  // Called when lessons were reordered inside a module
  onLessonOrderChanged(event: { moduleId?: number, lessons: Lesson[] }) {
    const moduleId = event.moduleId;
    if (!moduleId) {
      console.warn('Module id missing when updating lesson order');
      return;
    }
    // Optimistic UI: already updated in child; call batched endpoint to persist
    const payload = event.lessons.filter(l => l.id).map(l => ({ id: l.id as number, order: l.order }));
    if (payload.length === 0) return;

    // Keep a copy for rollback
    const previous = this.currentModule.lessons ? this.currentModule.lessons.map(l => ({ id: l.id, order: l.order })) : [];

    this.courseService.updateLessonOrdersInModule(moduleId, payload).subscribe({
      next: () => {
        this.toast.success('Lesson order saved');
      },
      error: (err) => {
        console.error('Error updating lesson orders:', err);
        this.toast.error('Failed to save lesson order. Reverting changes.');
        // rollback
        if (this.currentModule.lessons) {
          this.currentModule.lessons.forEach(l => {
            const prev = previous.find(p => p.id === l.id);
            if (prev) l.order = prev.order as number;
          });
        }
      }
    });
  }

  // Called when modules list was reordered
  onModulesReordered(modules: Module[]) {
    if (!this.currentCourseOverview?.id) {
      console.warn('Course id missing when updating module order');
      return;
    }

    // Optimistic UI: replace local modules immediately
    const previous = this.modules.map(m => ({ id: m.id, order: m.order }));
    this.modules = modules.map(m => ({ ...m }));

    const payload = modules.filter(m => m.id).map(m => ({ id: m.id as number, order: m.order }));
    if (payload.length === 0) return;

    this.courseService.updateModuleOrdersInCourse(this.currentCourseOverview!.id as number, payload).subscribe({
      next: () => this.toast.success('Module order saved'),
      error: (err) => {
        console.error('Error updating module orders:', err);
        this.toast.error('Failed to save module order. Reverting.');
        // rollback
        this.modules.forEach(m => {
          const prev = previous.find(p => p.id === m.id);
          if (prev) m.order = prev.order as number;
        });
      }
    });
  }

  onSaveModuleDraft(module: Module) {
    console.log('Saving module draft:', module);
    if (!this.currentCourseOverview?.id) {
      console.error('No course created yet');
      this.toast.error('Please create a course first');
      return;
    }

    // Map frontend module to backend DTO
    const moduleData = {
      moduleName: module.title,
      moduleDescription: module.summary,
      isActive: module.status === 'Active',
      estimatedDuration: module.estimatedTime
    };

    this.isSavingModule = true;

    if (module.id) {
      // Update existing module
      this.courseService.updateModule(module.id, moduleData).subscribe({
        next: (response) => {
          console.log('Module updated:', response);

          // Now update the order in CourseModules join table
          if (this.currentCourseOverview?.id) {
            this.courseService.updateModuleOrderInCourse(this.currentCourseOverview.id as number, module.id!, module.order).subscribe({
              next: () => {
                this.isSavingModule = false;
                this.toast.success('Module and order updated successfully');
                module.state = 'saved';

                // Update the module in the modules array
                const index = this.modules.findIndex(m => m.id === module.id);
                if (index !== -1) {
                  this.modules[index] = { ...module };
                }
              },
              error: (orderError) => {
                this.isSavingModule = false;
                console.error('Error updating module order:', orderError);
                this.toast.error('Module updated but failed to update order');
                module.state = 'saved'; // Module content was saved even if order failed

                // Update the module in the modules array anyway
                const index = this.modules.findIndex(m => m.id === module.id);
                if (index !== -1) {
                  this.modules[index] = { ...module };
                }
              }
            });
          } else {
            // No course context, just update module content
            this.isSavingModule = false;
            this.toast.success('Module updated successfully');
            module.state = 'saved';
            const index = this.modules.findIndex(m => m.id === module.id);
            if (index !== -1) {
              this.modules[index] = { ...module };
            }
          }
        },
        error: (error) => {
          this.isSavingModule = false;
          console.error('Error updating module:', error);
          this.toast.error('Failed to update module');
        }
      });
    } else {
      // Create new module
      this.courseService.createModule(moduleData).subscribe({
        next: (moduleId: number) => {
          this.isSavingModule = false;
          console.log('Module created with id:', moduleId);
          this.toast.success('Module created successfully');
          module.id = moduleId;
          module.state = 'saved';
          // Add to modules array
          this.modules.push({ ...module });
          // Now add to course
          this.courseService.addModuleToCourse(this.currentCourseOverview!.id as number, module.id, module.order).subscribe({
            next: (addResponse) => {
              console.log('Module added to course:', addResponse);
              this.toast.success('Module added to course');
            },
            error: (addError) => {
              console.error('Error adding module to course:', addError);
              this.toast.error('Failed to add module to course');
            }
          });
        },
        error: (error) => {
          this.isSavingModule = false;
          console.error('Error creating module:', error);
          this.toast.error('Failed to create module');
        }
      });
    }
  }

  private convertVideoDtoToLesson(videoDto: any): Lesson {
    console.log('Converting VideoDto to Lesson:', videoDto);
    console.log('VideoDto attachments:', videoDto.attachments);
    // Accept a variety of possible field names coming from the backend DTOs
    const rawOrder = videoDto.order ?? videoDto.position ?? videoDto.sequence ?? videoDto.index ?? videoDto.number ?? videoDto.videoOrder;
    let parsedOrder = Number(rawOrder);
    if (isNaN(parsedOrder) || rawOrder === null || rawOrder === undefined) parsedOrder = 0;

    const rawDuration = videoDto.durationSeconds ?? videoDto.duration ?? videoDto.lengthSeconds ?? videoDto.length ?? videoDto.durationInSeconds ?? videoDto.videoDuration;
    let parsedDuration = Number(rawDuration);
    if (isNaN(parsedDuration) || rawDuration === null || rawDuration === undefined) parsedDuration = 0;

    const lesson: Lesson = {
      id: videoDto.id,
      order: parsedOrder,
      title: videoDto.title || '',
      explanation: videoDto.explanation || '',
      whatWeWillLearn: videoDto.whatWeWillLearn ? videoDto.whatWeWillLearn.split(', ') : [''],
      duration: parsedDuration,
      status: videoDto.status || 'Active',
      attachments: videoDto.attachments ? videoDto.attachments.map((att: any) => {
        console.log('Mapping attachment:', att);
        return {
          id: att.id,
          file: null, // Existing attachments don't have File objects
          displayName: att.fileName || att.title || 'Attachment',
          downloadUrl: att.id ? this.getAttachmentDownloadUrl(att.id) : undefined
        };
      }) : [],
      prerequisites: videoDto.prerequisites ? videoDto.prerequisites.split(', ') : [''],
      state: 'saved'
    };
    console.log('Converted lesson:', lesson);
    return lesson;
  }

  // Helper to construct download URL for attachments (including thumbnails)
  // Use the same `environment.apiUrl` value that the CourseService uses.
  private getAttachmentDownloadUrl(id: number | string): string {

    return `localhost:5000/api/attachments/${encodeURIComponent(String(id))}/download`;
  }

  // Load a course by id and populate the editor's state for editing
  private loadCourseForEdit(courseId: number) {
    this.isLoadingCourse = true;
    this.courseService.getCourse(courseId).subscribe({
      next: (res: any) => {
        this.isLoadingCourse = false;
        console.debug('[CourseEditor] loadCourseForEdit raw response:', res);
        const payload = res && res.data ? res.data : res;
        // map payload into currentCourseOverview shape
        if (!payload) {
          console.warn('[CourseEditor] No payload in getCourse response');
          this.toast.error('Failed to load course data');
          return;
        }
        this.toast.success('Course loaded successfully');
        this.currentCourseOverview = {
          id: payload.id,
          name: payload.name || '',
          description: payload.description || '',
          estimatedDurationInHours: payload.estimatedDurationInHours || 0,
          difficultyLevel: payload.difficultyLevel || 'BIGINNER',
          status: payload.status || 'DRAFT',
          currency: payload.currency || 'USD',
          category: payload.category || '',
          thumbnail: payload.thumbnail || 0,
          thumbnailName: '',
          tags: payload.tags ? Array.from(payload.tags).map((t: any) => t.name || t) : [],
          pricing: {
            oneTimePrice: payload.oneTimePrice || 0,
            allowsSubscription: payload.allowsSubscription || false,
            subscriptionPriceMonthly: payload.subscriptionPriceMonthly || 0,
            subscriptionPrice3Months: payload.subscriptionPrice3Months || 0,
            subscriptionPrice6Months: payload.subscriptionPrice6Months || 0,
          },
          isActive: payload.isActive || false
        };

        // Populate modules list if present
        console.debug('[CourseEditor] payload.modules raw:', payload.modules);
        this.modules = (payload.modules || []).map((m: any, idx: number) => {
          // Backend CourseDetailsDto.modules is a list of CourseModuleDto { moduleOrder, module: ModuleSummaryDto }
          const moduleWrapper = m.module || m; // support both shapes

          // Extract module order - prioritize moduleOrder from the wrapper
          let moduleOrder = m.moduleOrder ?? m.order ?? moduleWrapper?.order ?? moduleWrapper?.moduleOrder;
          if (moduleOrder === null || moduleOrder === undefined || isNaN(Number(moduleOrder))) {
            moduleOrder = idx + 1;
          } else {
            moduleOrder = Number(moduleOrder);
          }

          // moduleId in backend can be a String; try to coerce to number when possible
          const rawModuleId = moduleWrapper?.moduleId ?? moduleWrapper?.id ?? moduleWrapper?.moduleIdString ?? m.moduleId ?? m.id;
          const parsedId = rawModuleId != null ? (Number(rawModuleId) || undefined) : undefined;
          const title = moduleWrapper?.moduleName || moduleWrapper?.title || `Module ${idx+1}`;
          const summary = moduleWrapper?.moduleDescription || moduleWrapper?.summary || '';
          const estimatedTime = moduleWrapper?.estimatedDuration || moduleWrapper?.estimatedTime || 0;
          const isActive = (moduleWrapper?.active !== undefined) ? moduleWrapper.active : moduleWrapper?.isActive;
          console.debug('[CourseEditor] mapping module wrapper', moduleWrapper, 'parsedId', parsedId, 'order', moduleOrder);
          return {
            id: parsedId,
            title,
            summary,
            order: moduleOrder,
            estimatedTime,
            isOptional: false,
            status: isActive ? 'Active' : 'Inactive',
            lessons: [], // lessons will be lazy-loaded when module is selected
            state: 'saved'
          } as Module;
        });

        this.courseCreated = true;
        this.activePage = 'overview';
      },
      error: (err: any) => {
        this.isLoadingCourse = false;
        console.error('Error loading course for edit:', err);
        this.toast.error('Failed to load course for editing');
        // optionally navigate back to courses list
      }
    });
  }

}
