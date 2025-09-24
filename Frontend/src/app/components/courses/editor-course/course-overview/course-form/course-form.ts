

import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges } from '@angular/core';
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
export class CourseFormComponent implements OnInit, OnChanges, OnDestroy {
  @Input() course: CourseoverviewCreationDto | null = null;
  @Output() formChange = new EventEmitter<FormGroup>();
  @Output() fileSelected = new EventEmitter<File>();

  courseForm!: FormGroup;
  selectedFile: File | null = null;
  thumbnailUrl: string = '';

  constructor(private fb: FormBuilder) {}

  ngOnInit(): void {
    this.initForm();
    // Subscribe to changes and emit them
    this.courseForm.valueChanges.subscribe(() => {
      // Mark fields as touched to show validation errors on change
      this.markFormGroupTouched(this.courseForm);
      this.formChange.emit(this.courseForm);
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['course'] && this.courseForm) {
      this.populateForm();
    }
  }

  private populateForm(): void {
    if (this.course) {
      this.courseForm.patchValue(this.course as any, { emitEvent: false });
    }
  }

  private initForm(): void {
    this.courseForm = this.fb.group({
      name: [this.course?.name || '', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      description: [this.course?.description || '', [Validators.minLength(10), Validators.maxLength(500)]],
      estimatedDurationInHours: [this.course?.estimatedDurationInHours || null, [Validators.min(1), Validators.max(1000)]],
      difficultyLevel: [this.course?.difficultyLevel || '', Validators.required],
      status: [{ value: this.course?.status || 'DRAFT', disabled: true }],
      currency: [this.course?.currency || 'USD', Validators.required],
      category: [this.course?.category || ''],
      thumbnail: [this.course?.thumbnail || ''],
      thumbnailName: [this.course?.thumbnailName || ''],
      tags: [this.course?.tags || []],
      oneTimePrice: [this.course?.pricing?.oneTimePrice || null, Validators.min(0)],
      allowsSubscription: [this.course?.pricing?.allowsSubscription || false],
      subscriptionPriceMonthly: [this.course?.pricing?.subscriptionPriceMonthly || null, Validators.min(0)],
      subscriptionPrice3Months: [this.course?.pricing?.subscriptionPrice3Months || null, Validators.min(0)],
      subscriptionPrice6Months: [this.course?.pricing?.subscriptionPrice6Months || null, Validators.min(0)],
      isActive: [this.course?.isActive || true],
    });
  }

  onThumbnailSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      // Validate file type (images only)
      if (!file.type.startsWith('image/')) {
        alert('Please select a valid image file.');
        return;
      }
      // Validate file size (max 5MB)
      const maxSize = 5 * 1024 * 1024; // 5MB
      if (file.size > maxSize) {
        alert('File size must be less than 5MB.');
        return;
      }
      this.selectedFile = file;
      this.thumbnailUrl = URL.createObjectURL(file);
      this.courseForm.patchValue({ thumbnail: file, thumbnailName: file.name });
      this.fileSelected.emit(file);
    }
  }

  private markFormGroupTouched(formGroup: FormGroup) {
    Object.keys(formGroup.controls).forEach(key => {
      const control = formGroup.get(key);
      control?.markAsTouched();
      if (control instanceof FormGroup) {
        this.markFormGroupTouched(control);
      }
    });
  }

  get pricingFormGroup(): FormGroup {
    return this.courseForm.get('pricing') as FormGroup;
  }

  addTag(event: KeyboardEvent) {
    if (event.key === 'Enter') {
      const input = event.target as HTMLInputElement;
      const tag = input.value.trim();
      if (tag) {
        const currentTags = this.courseForm.get('tags')?.value || [];
        if (currentTags.length < 10) {
          this.courseForm.patchValue({ tags: [...currentTags, tag] });
          input.value = '';
        }
      }
    }
  }

  removeTag(tagToRemove: string) {
    const currentTags = this.courseForm.get('tags')?.value || [];
    this.courseForm.patchValue({ tags: currentTags.filter((tag: string) => tag !== tagToRemove) });
  }

  getThumbnailUrl(): string {
    const thumbnail = this.courseForm.get('thumbnail')?.value;
    if (thumbnail instanceof File) {
      return this.thumbnailUrl;
    }
    return thumbnail || '';
  }

  isThumbnailFile(): boolean {
    return this.courseForm.get('thumbnail')?.value instanceof File;
  }

  ngOnDestroy(): void {
    if (this.thumbnailUrl) {
      URL.revokeObjectURL(this.thumbnailUrl);
    }
  }
}
