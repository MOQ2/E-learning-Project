import { Component, Input, Output, EventEmitter, ViewEncapsulation } from '@angular/core';
import { Lesson } from '../lesson-page/lesson.model'; // Using the same model
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-lesson-editor',
  templateUrl: './lesson-from.html',
  styleUrl: './lesson-from.css',
  standalone: true,
  imports: [FormsModule, CommonModule],
  encapsulation: ViewEncapsulation.None
})
export class LessonEditorComponent {

  // @Input() allows the parent component to pass data into this component.
  // The 'lesson' property will be populated by the parent's [lesson]="lessonData" binding.
  @Input() lesson!: Lesson;

  // @Output() creates custom events that this component can emit up to the parent.
  // The parent listens for these events like (saveLesson)="handler($event)".
  @Output() saveLesson = new EventEmitter<Lesson>();
  @Output() saveDraft = new EventEmitter<Lesson>();
  @Output() preview = new EventEmitter<Lesson>();
  @Output() goBack = new EventEmitter<void>();

  // --- METHODS THAT EMIT EVENTS TO THE PARENT ---

  onFormSubmit() {
    if (this.lesson) {
      // Convert duration from minutes to seconds
      const lessonToSave = { ...this.lesson, duration: this.lesson.duration * 60 };
      this.saveLesson.emit(lessonToSave);
    }
  }

  onSaveDraftClick() {
    if (this.lesson) {
      const lessonToSave = { ...this.lesson, duration: this.lesson.duration * 60 };
      this.saveDraft.emit(lessonToSave);
    }
  }

  onPreviewClick() {
    if (this.lesson) {
      const lessonToSave = { ...this.lesson, duration: this.lesson.duration * 60 };
      this.preview.emit(lessonToSave);
    }
  }

  onBackClick() {
    // Emits a simple event without any data.
    this.goBack.emit();
  }

  // --- UI INTERACTION METHODS ---

  addBullet(list: 'whatWeWillLearn' | 'prerequisites') {
    if (this.lesson) {
      this.lesson[list].push('');
    }
  }

  removeBullet(list: 'whatWeWillLearn' | 'prerequisites', index: number) {
    if (this.lesson) {
      this.lesson[list].splice(index, 1);
    }
  }

  addAttachment() {
    if (this.lesson) {
      this.lesson.attachments.push({ file: null, displayName: '' });
    }
  }

  removeAttachment(index: number) {
    if (this.lesson) {
      this.lesson.attachments.splice(index, 1);
    }
  }

  onFileSelected(event: any, index: number) {
    const file = event.target.files[0];
    if (this.lesson && file) {
      this.lesson.attachments[index].file = file;
    }
  }

  // Helper for tracking items in ngFor loops to improve performance.
  trackByIndex(index: number, item: any): any {
    return index;
  }
}
