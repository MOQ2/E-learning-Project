
import { Component, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import {CourseoverviewCreationDto } from '../enums/CourseOverviewCreationDto';
import { CourseFormComponent } from '../course-form/course-form';
import { SidebarComponent } from '../sidebar/sidebar';
import { NavBar } from '../../../../nav-bar/nav-bar';
import { ViewEncapsulation } from '@angular/core';

@Component({
  selector: 'app-course-editor-page',
  templateUrl: './course-editor-page-component.html',
  styleUrl: './course-editor-page-component.css',
  imports: [CourseFormComponent,SidebarComponent , NavBar],
  standalone: true,
  encapsulation: ViewEncapsulation.None
})
export class CourseEditorPageComponent implements OnInit {


  currentCourse: CourseoverviewCreationDto | null = {
    id: 'c1',
    name: 'Introduction to UX Design',
    description: 'A beginner-friendly course on user experience principles.',
    estimatedDurationInHours: 12,
    difficultyLevel: 'beginner',
    status: 'DRAFT',
    currency: 'USD',
    category: 'design',
    thumbnail: 'https://example.com/thumb.jpg',
    thumbnailName: 'thumbnail.jpg',
    previewVideoUrl: 'https://example.com/preview.mp4',
    createdBy: 'user1',
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

  constructor() {}

  ngOnInit(): void {
    // fetch set up the courses data here if needed
  }

  // Captures the form group emitted from the course-form component
  handleFormChange(formGroup: FormGroup) {
    this.courseFormState = formGroup;
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
    if (this.courseFormState.valid) {
      console.log("Continue clicked. Form is valid. Data:", this.courseFormState.getRawValue());
      // Navigate to the next step, e.g., modules
    } else {
      console.log("Form is invalid. Cannot continue.");
      this.courseFormState.markAllAsTouched(); // Show validation errors
    }
  }
}
