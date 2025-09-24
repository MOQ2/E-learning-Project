
import { Component, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import {CourseoverviewCreationDto } from '../enums/CourseOverviewCreationDto';
import { CourseFormComponent } from '../course-form/course-form';
import { SidebarComponent } from '../sidebar/sidebar';
import { NavBar } from '../../../../nav-bar/nav-bar';
import { ViewEncapsulation } from '@angular/core';
import { CourseService } from '../../../../../Services/Courses/course-service';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-course-editor-page',
  templateUrl: './course-editor-page-component.html',
  styleUrl: './course-editor-page-component.css',
  imports: [CourseFormComponent,SidebarComponent , NavBar],
  standalone: true,
  encapsulation: ViewEncapsulation.None
})
export class CourseEditorPageComponent implements OnInit {


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
}
