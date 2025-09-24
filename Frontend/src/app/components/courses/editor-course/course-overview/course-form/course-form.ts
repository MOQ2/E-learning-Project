

import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CourseoverviewCreationDto  } from '../enums/CourseOverviewCreationDto';
import { ViewEncapsulation } from '@angular/core';


@Component({
  selector: 'app-course-form',
  imports: [CommonModule, ReactiveFormsModule], // Add ReactiveFormsModule here
  templateUrl: './course-form.html',
  styleUrl: './course-form.css',
  standalone: true,
  encapsulation: ViewEncapsulation.None
})
export class CourseFormComponent implements OnInit, OnChanges {
  @Input() course: CourseoverviewCreationDto | null = null;
  @Output() formChange = new EventEmitter<FormGroup>();

  courseForm!: FormGroup;

  constructor(private fb: FormBuilder) {}

  ngOnInit(): void {
    this.initForm();
    // Subscribe to changes and emit them
    this.courseForm.valueChanges.subscribe(() => {
      this.formChange.emit(this.courseForm);
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    // If the input 'course' data changes, patch the form
    if (changes['course'] && this.courseForm) {
      this.courseForm.patchValue(this.course as any, { emitEvent: false });
    }
  }

  private initForm(): void {
    this.courseForm = this.fb.group({
      name: [this.course?.name || '', Validators.required],
      description: [this.course?.description || ''],
      estimatedDurationInHours: [this.course?.estimatedDurationInHours || null],
      difficultyLevel: [this.course?.difficultyLevel || '', Validators.required],
      status: [{ value: this.course?.status || 'DRAFT', disabled: true }],
      currency: [this.course?.currency || ''],
      category: [this.course?.category || ''],
      thumbnail: [this.course?.thumbnail || '', [Validators.pattern('https://.*')]],
      thumbnailName: [this.course?.thumbnailName || ''],
      previewVideoUrl: [this.course?.previewVideoUrl || '', [Validators.pattern('https://.*')]],
      createdBy: [this.course?.createdBy || ''],
      tags: [this.course?.tags || []],
      pricing: this.fb.group({
        oneTimePrice: [this.course?.pricing?.oneTimePrice || null],
        allowsSubscription: [this.course?.pricing?.allowsSubscription || false],
        subscriptionPriceMonthly: [this.course?.pricing?.subscriptionPriceMonthly || null],
        subscriptionPrice3Months: [this.course?.pricing?.subscriptionPrice3Months || null],
        subscriptionPrice6Months: [this.course?.pricing?.subscriptionPrice6Months || null],
      }),
      isActive: [this.course?.isActive || true],
    });
  }

  onThumbnailSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.courseForm.patchValue({ thumbnailName: file.name });
    }
  }

  get pricingForm(): FormGroup {
    return this.courseForm.get('pricing') as FormGroup;
  }

  addTag(event: KeyboardEvent) {
    if (event.key === 'Enter') {
      const input = event.target as HTMLInputElement;
      const tag = input.value.trim();
      if (tag) {
        const currentTags = this.courseForm.get('tags')?.value || [];
        this.courseForm.patchValue({ tags: [...currentTags, tag] });
        input.value = '';
      }
    }
  }

  removeTag(tagToRemove: string) {
    const currentTags = this.courseForm.get('tags')?.value || [];
    this.courseForm.patchValue({ tags: currentTags.filter((tag: string) => tag !== tagToRemove) });
  }
}
