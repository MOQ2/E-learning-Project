import { Component, Input, Output, EventEmitter, ViewEncapsulation, OnChanges } from '@angular/core';
import { Lesson } from '../lesson-page/lesson.model';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, FormArray, FormControl } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-lesson-editor',
  templateUrl: './lesson-from.html',
  styleUrl: './lesson-from.css',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  encapsulation: ViewEncapsulation.None
})
export class LessonEditorComponent implements OnChanges {

  // @Input() allows the parent component to pass data into this component.
  // The 'lesson' property will be populated by the parent's [lesson]="lessonData" binding.
  @Input() lesson!: Lesson;

  // @Output() creates custom events that this component can emit up to the parent.
  // The parent listens for these events like (saveLesson)="handler($event)".
  @Output() saveLesson = new EventEmitter<Lesson>();
  @Output() saveDraft = new EventEmitter<Lesson>();
  @Output() preview = new EventEmitter<Lesson>();
  @Output() goBack = new EventEmitter<void>();

  lessonForm!: FormGroup;

  constructor(private fb: FormBuilder) {
    this.initForm();
  }

  ngOnChanges() {
    if (this.lesson) {
      this.populateForm();
    }
  }

  private initForm(): void {
    this.lessonForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(3)]],
      explanation: ['', [Validators.required, Validators.minLength(10)]],
      duration: [1, [Validators.required, Validators.min(1)]],
      status: ['Active'],
      whatWeWillLearn: this.fb.array([new FormControl('', Validators.required)]),
      prerequisites: this.fb.array([new FormControl('', Validators.required)]),
      attachments: this.fb.array([])
    });
  }

  private populateForm(): void {
    if (this.lesson) {
      this.lessonForm.patchValue({
        title: this.lesson.title,
        explanation: this.lesson.explanation,
        duration: Math.floor(this.lesson.duration / 60),
        status: this.lesson.status
      });
      this.setFormArray('whatWeWillLearn', this.lesson.whatWeWillLearn);
      this.setFormArray('prerequisites', this.lesson.prerequisites);
      this.attachments.clear();
      this.lesson.attachments.forEach(att => {
        this.attachments.push(this.fb.group({
          displayName: [att.displayName]
        }));
      });
    }
  }

  private setFormArray(arrayName: 'whatWeWillLearn' | 'prerequisites', values: string[]): void {
    const formArray = this.lessonForm.get(arrayName) as FormArray;
    formArray.clear();
    values.forEach(value => {
      formArray.push(new FormControl(value || '', Validators.required));
    });
  }

  get whatWeWillLearnControls() {
    return (this.lessonForm.get('whatWeWillLearn') as FormArray).controls;
  }

  get prerequisitesControls() {
    return (this.lessonForm.get('prerequisites') as FormArray).controls;
  }

  get attachments() {
    return this.lessonForm.get('attachments') as FormArray;
  }

  // --- METHODS THAT EMIT EVENTS TO THE PARENT ---

  onFormSubmit() {
    if (this.lessonForm.valid) {
      const formValue = this.lessonForm.value;
      const lessonToSave: Lesson = {
        ...this.lesson,
        ...formValue,
        duration: formValue.duration * 60, // Convert to seconds
        attachments: formValue.attachments.map((att: any) => ({ file: null, displayName: att.displayName }))
      };
      this.saveLesson.emit(lessonToSave);
    } else {
      this.lessonForm.markAllAsTouched();
    }
  }

  onSaveDraftClick() {
    if (this.lessonForm.valid) {
      const formValue = this.lessonForm.value;
      const lessonToSave: Lesson = {
        ...this.lesson,
        ...formValue,
        duration: formValue.duration * 60,
        attachments: formValue.attachments.map((att: any) => ({ file: null, displayName: att.displayName }))
      };
      this.saveDraft.emit(lessonToSave);
    } else {
      this.lessonForm.markAllAsTouched();
    }
  }

  onPreviewClick() {
    if (this.lessonForm.valid) {
      const formValue = this.lessonForm.value;
      const lessonToSave: Lesson = {
        ...this.lesson,
        ...formValue,
        duration: formValue.duration * 60,
        attachments: formValue.attachments.map((att: any) => ({ file: null, displayName: att.displayName }))
      };
      this.preview.emit(lessonToSave);
    } else {
      this.lessonForm.markAllAsTouched();
    }
  }

  onBackClick() {
    // Emits a simple event without any data.
    this.goBack.emit();
  }

  // --- UI INTERACTION METHODS ---

  addBullet(list: 'whatWeWillLearn' | 'prerequisites') {
    const formArray = this.lessonForm.get(list) as FormArray;
    formArray.push(new FormControl('', Validators.required));
  }

  removeBullet(list: 'whatWeWillLearn' | 'prerequisites', index: number) {
    const formArray = this.lessonForm.get(list) as FormArray;
    if (formArray.length > 1) {
      formArray.removeAt(index);
    }
  }

  addAttachment() {
    this.attachments.push(this.fb.group({
      displayName: ['']
    }));
  }

  removeAttachment(index: number) {
    this.attachments.removeAt(index);
  }

  onFileSelected(event: any, index: number) {
    // Handle file selection, perhaps store in a separate array
  }

  // Helper for tracking items in ngFor loops to improve performance.
  trackByIndex(index: number, item: any): any {
    return index;
  }
}
