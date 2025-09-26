
import { Component, Input, Output, EventEmitter, OnChanges, SimpleChanges, ViewEncapsulation } from '@angular/core';
import { Module } from '../../course-editor-page-component/course-editor-page-component';
import { Lesson } from '../../lesson-editor/lesson-page/lesson.model';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { DragDropModule, CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';

@Component({
  selector: 'app-module-form',
  templateUrl: './module-form.html',
  styleUrls: ['./module-form.css'],
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, DragDropModule],
  encapsulation: ViewEncapsulation.None
})
export class ModuleFormComponent implements OnChanges {
  /**
   * INPUT: The module object to be edited.
   */
  @Input() module!: Module;

  /**
   * OUTPUT: Emits the updated module when 'Save draft' is clicked.
   */
  @Output() saveDraft = new EventEmitter<Module>();

  /**
   * OUTPUT: Emits when 'Add lesson' button is clicked.
   */
  @Output() addLesson = new EventEmitter<void>();

  /**
   * OUTPUT: Emits the lesson to edit when 'Edit' button is clicked.
   */
  @Output() editLesson = new EventEmitter<Lesson>();

  moduleForm!: FormGroup;
  editableModule!: Module;

  constructor(private fb: FormBuilder) {
    this.initForm();
  }

  /**
   * A lifecycle hook that runs when any @Input() property changes.
   * We use it to create a safe, local copy of the module for editing.
   */
  ngOnChanges(changes: SimpleChanges): void {
    if (changes['module'] && this.module) {
      // Create a deep copy to prevent two-way binding issues
      this.editableModule = JSON.parse(JSON.stringify(this.module));
      this.populateForm();
      // Set initial state
      this.editableModule.state = this.module.id ? 'saved' : 'new';

      // Track form changes to update state
      this.moduleForm.valueChanges.subscribe(() => {
        if (this.editableModule.state !== 'new') {
          this.editableModule.state = 'edited';
        }
      });
    }
  }

  private initForm(): void {
    this.moduleForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(3)]],
      summary: ['', [Validators.required, Validators.minLength(10)]],
      order: [1],
      estimatedTime: [1, [Validators.required, Validators.min(1)]],
      isOptional: [false],
      status: ['Active']
    });
  }

  private populateForm(): void {
    if (this.editableModule) {
      this.moduleForm.patchValue(this.editableModule, { emitEvent: false });
    }
  }

  /**
   * Emits the current state of the editableModule to the parent.
   */
  onSaveChanges(): void {
    if (this.moduleForm.valid && (this.editableModule.state === 'edited' || this.editableModule.state === 'new')) {
      this.editableModule = { ...this.editableModule, ...this.moduleForm.value };
      this.saveDraft.emit(this.editableModule);
      // Set state to saved after emitting
      this.editableModule.state = 'saved';
    } else {
      this.moduleForm.markAllAsTouched();
    }
  }

  /**
   * Emits event when Add lesson button is clicked.
   */
  onAddLessonClick(): void {
    this.addLesson.emit();
  }

  /**
   * Emits event when Edit button is clicked for a specific lesson.
   */
  onEditLessonClick(lesson: Lesson): void {
    this.editLesson.emit(lesson);
  }

  drop(event: CdkDragDrop<string[]>) {
    if (this.editableModule.lessons) {
      moveItemInArray(this.editableModule.lessons, event.previousIndex, event.currentIndex);
      this.editableModule.lessons.forEach((lesson, index) => {
        lesson.order = index + 1;
      });
    }
  }
}
