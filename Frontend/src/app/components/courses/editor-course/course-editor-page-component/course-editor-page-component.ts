
import { Component, OnInit, ViewChild } from '@angular/core';
import { FormGroup } from '@angular/forms';
import {CourseoverviewCreationDto } from '../course-overview/enums/CourseOverviewCreationDto';
import { CourseFormComponent } from '../course-overview/course-form/course-form';
import { SidebarComponent } from '../course-overview/sidebar/sidebar';
import { NavBar } from '../../../nav-bar/nav-bar';
import { ViewEncapsulation } from '@angular/core';
import { CourseService } from '../../../../Services/Courses/course-service';
import { Observable } from 'rxjs';
import { ModuleFormComponent } from '../module-editor/module-form/module-form';
import { LessonEditorComponent } from '../lesson-editor/lesson-from/lesson-from';
import { ModulesListComponent } from '../module-editor/modules-list/modules-list';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Lesson } from '../lesson-editor/lesson-page/lesson.model';

export interface Module {
  id?: number;
  title: string;
  summary: string;
  order: number;
  estimatedTime: number;
  isOptional: boolean;
  status: 'Active' | 'Inactive';
  lessons?: Lesson[];
}

@Component({
  selector: 'app-course-editor-page',
  templateUrl: './course-editor-page-component.html',
  styleUrl: './course-editor-page-component.css',
  imports: [CourseFormComponent, SidebarComponent, NavBar, ModuleFormComponent, LessonEditorComponent, ModulesListComponent, FormsModule, CommonModule],
  standalone: true,
  encapsulation: ViewEncapsulation.None
})
export class CourseEditorPageComponent implements OnInit {

  @ViewChild(LessonEditorComponent) lessonEditor!: LessonEditorComponent;

  activePage: 'overview' | 'modules' | 'add-lesson' | 'publish' = 'overview';

  currentLesson: Lesson = {
    title: '',
    explanation: '',
    whatWeWillLearn: [''],
    duration: 0,
    status: 'Active',
    attachments: [],
    prerequisites: ['']
  };

  currentModule: Module = {
    title: '',
    summary: '',
    order: 1,
    estimatedTime: 0,
    isOptional: false,
    status: 'Active',
    lessons: [
      { id: 1, title: 'Lesson 1 • Welcome', explanation: '', whatWeWillLearn: [], duration: 0, status: 'Active', attachments: [], prerequisites: [] },
      { id: 2, title: 'Lesson 2 • Setting up your Environment', explanation: '', whatWeWillLearn: [], duration: 0, status: 'Active', attachments: [], prerequisites: [] },
      { id: 3, title: 'Lesson 3 • Basic Syntax', explanation: '', whatWeWillLearn: [], duration: 0, status: 'Active', attachments: [], prerequisites: [] },
      { id: 4, title: 'Lesson 4 • Your First Program', explanation: '', whatWeWillLearn: [], duration: 0, status: 'Active', attachments: [], prerequisites: [] }
    ]
  };

  // Sample modules to power the sidebar list
  modules: Module[] = [
    { id: 1, title: 'Introduction to Python', summary: '', order: 1, estimatedTime: 15, isOptional: false, status: 'Active', lessons: [{ id: 1, title: 'Lesson 1', explanation: '', whatWeWillLearn: [], duration: 5, status: 'Active', attachments: [], prerequisites: [] }, { id: 2, title: 'Lesson 2', explanation: '', whatWeWillLearn: [], duration: 10, status: 'Active', attachments: [], prerequisites: [] }] },
    { id: 2, title: 'Data Types & Variables', summary: '', order: 2, estimatedTime: 35, isOptional: false, status: 'Inactive', lessons: [{ id: 3, title: 'Lesson 1', explanation: '', whatWeWillLearn: [], duration: 15, status: 'Active', attachments: [], prerequisites: [] }] },
    { id: 3, title: 'Control Flow', summary: '', order: 3, estimatedTime: 50, isOptional: false, status: 'Active', lessons: [{ id: 4, title: 'Lesson 1', explanation: '', whatWeWillLearn: [], duration: 20, status: 'Active', attachments: [], prerequisites: [] }] },
    { id: 4, title: 'Functions', summary: '', order: 4, estimatedTime: 45, isOptional: false, status: 'Inactive', lessons: [] },
    { id: 5, title: 'Object-Oriented Programming', summary: '', order: 5, estimatedTime: 75, isOptional: false, status: 'Inactive', lessons: [] }
  ];

  onModuleSelected(module: Module) {
    this.currentModule = module;
    this.activePage = 'modules';
  }

  onAddModule() {
    const nextOrder = this.modules.length + 1;
    const newModule: Module = { id: nextOrder, title: `New module ${nextOrder}`, summary: '', order: nextOrder, estimatedTime: 0, isOptional: false, status: 'Inactive', lessons: [] };
    this.modules = [...this.modules, newModule];
  }


  currentCourseOverview: CourseoverviewCreationDto | null = {
    id: 2,
    name: 'Introduction to UX Design',
    description: 'A beginner-friendly course on user experience principles.',
    estimatedDurationInHours: 12,
    difficultyLevel: 'BIGINNER',
    status: 'DRAFT',
    currency: 'USD',
    category: 'design',
    thumbnail: 10,
    thumbnailName: 'thumbnail.jpg',
    tags: ['design', 'beginner', 'ux'],
    pricing: {
      oneTimePrice: 99.99,
      allowsSubscription: true,
      subscriptionPriceMonthly: 9.99,
      subscriptionPrice3Months: 24.99,
      subscriptionPrice6Months: 49.99,
    },
    isActive: true
  };

  // This will hold the latest state of the form from the child component
  private courseFormState!: FormGroup;
  selectedFile: File | null = null;

  constructor(private courseService: CourseService) {}

  ngOnInit(): void {
    // fetch set up the courses data here if needed
  }

  onActiveLinkChange(link: 'overview' | 'modules' | 'add-lesson' | 'publish') {
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
    console.log("Continuing to next step...");
    console.log("file size is ", this.selectedFile?.size);
    console.log("file metadata is ", this.selectedFile);
    console.log("file", this.selectedFile);

    if (this.courseFormState && this.courseFormState.valid) {
      if (this.selectedFile) {
        this.uploadFile(this.selectedFile).subscribe(attachmentId => {
          const raw = this.courseFormState.getRawValue();
          const { thumbnailName, thumbnail, ...courseData } = raw;
          courseData.thumbnail = attachmentId;
          console.log ("attachment id is ", attachmentId);
          this.createCourse(courseData);
        });
      } else {
        const raw = this.courseFormState.getRawValue();
        const { thumbnailName, ...courseData } = raw;
        this.createCourse(courseData);
      }
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

  private createCourse(courseData: any) {
    this.courseService.createCourse(courseData).subscribe((response: any) => {
      console.log('Course created:', response);
      // Navigate or show success
    });
  }

  onSaveLesson(lesson: Lesson) {
    console.log('Saving lesson:', lesson);
    // Implement save logic
  }

  onSaveDraftLesson(lesson: Lesson) {
    console.log('Saving draft lesson:', lesson);
    // Implement save draft logic
  }

  onPreviewLesson(lesson: Lesson) {
    console.log('Previewing lesson:', lesson);
    // Implement preview logic
  }

  onGoBackFromLesson() {
    this.activePage = 'modules';
  }
}
