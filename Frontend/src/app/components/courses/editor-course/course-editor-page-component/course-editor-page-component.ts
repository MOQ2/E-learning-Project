import { Component, OnInit, OnDestroy, ViewChild, Renderer2, Inject } from '@angular/core';
import { FormGroup } from '@angular/forms';
import {CourseoverviewCreationDto } from '../course-overview/enums/CourseOverviewCreationDto';
import { CourseFormComponent } from '../course-overview/course-form/course-form';
import { SidebarComponent } from '../course-overview/sidebar/sidebar';
import { NavBar } from '../../../nav-bar/nav-bar';
import { ViewEncapsulation } from '@angular/core';
import { CourseService } from '../../../../Services/Courses/course-service';
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
    this.currentModule = module;
    this.activePage = 'modules';
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

  constructor(private courseService: CourseService, private renderer: Renderer2, @Inject(DOCUMENT) private document: Document) {}

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
        this.uploadFile(this.selectedFile).subscribe(attachmentId => {
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

    if (this.currentCourseOverview?.id) {
      // Update existing course
      console.log('Updating course with id:', this.currentCourseOverview.id);
      this.courseService.updateCourse(this.currentCourseOverview.id, courseData).subscribe({
        next: (response) => {
          console.log('Course updated successfully:', response);
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
          console.error('Error updating course:', error);
        }
      });
    } else {
      // Create new course
      console.log('Creating new course');
      this.courseService.createCourse(courseData).subscribe({
        next: (response) => {
          console.log('Course created successfully, response:', response);
          // Update the course with the returned data to preserve form values
          if (this.currentCourseOverview) {
            const courseDetails = response; // response is already the data from the service
            console.log('courseDetails:', courseDetails);
            console.log('courseDetails.id:', courseDetails.id);
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
          console.error('Error creating course:', error);
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
      forkJoin(uploads).subscribe({
        next: (uploadedIds: number[]) => {
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
          console.error('Error uploading files:', error);
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

    if (lesson.id) {
      // Update existing lesson
      this.courseService.updateLesson(lesson.id, lessonData).subscribe({
        next: (response) => {
          console.log('Lesson updated:', response);
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
        error: (error) => {
          console.error('Error updating lesson:', error);
        }
      });
    } else {
      // Create new lesson
      this.courseService.createLesson(lessonData).subscribe({
        next: (lessonId: number) => {
          console.log('Lesson created with id:', lessonId);
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
          console.error('Error creating lesson:', error);
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
    // Set the current lesson to the one being edited
    this.currentLesson = { ...lesson };
    this.activePage = 'lesson';
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

  onSaveModuleDraft(module: Module) {
    console.log('Saving module draft:', module);
    if (!this.currentCourseOverview?.id) {
      console.error('No course created yet');
      return;
    }

    // Map frontend module to backend DTO
    const moduleData = {
      moduleName: module.title,
      moduleDescription: module.summary,
      isActive: module.status === 'Active',
      estimatedDuration: module.estimatedTime
    };

    if (module.id) {
      // Update existing module
      this.courseService.updateModule(module.id, moduleData).subscribe({
        next: (response) => {
          console.log('Module updated:', response);
          module.state = 'saved';
          // Update the module in the modules array
          const index = this.modules.findIndex(m => m.id === module.id);
          if (index !== -1) {
            this.modules[index] = { ...module };
          }
        },
        error: (error) => {
          console.error('Error updating module:', error);
        }
      });
    } else {
      // Create new module
      this.courseService.createModule(moduleData).subscribe({
        next: (moduleId: number) => {
          console.log('Module created with id:', moduleId);
          module.id = moduleId;
          module.state = 'saved';
          // Add to modules array
          this.modules.push({ ...module });
          // Now add to course
          this.courseService.addModuleToCourse(this.currentCourseOverview!.id as number, module.id, module.order).subscribe({
            next: (addResponse) => {
              console.log('Module added to course:', addResponse);
            },
            error: (addError) => {
              console.error('Error adding module to course:', addError);
            }
          });
        },
        error: (error) => {
          console.error('Error creating module:', error);
        }
      });
    }
  }
}
