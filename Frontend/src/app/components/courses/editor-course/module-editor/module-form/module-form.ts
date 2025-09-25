
import { Component, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';
import { Module } from '../../course-editor-page-component/course-editor-page-component';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ViewEncapsulation } from '@angular/core';

@Component({
  selector: 'app-module-form',
  templateUrl: './module-form.html',
  styleUrls: ['./module-form.css'],
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
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
    if (this.moduleForm.valid) {
      this.editableModule = { ...this.editableModule, ...this.moduleForm.value };
      this.saveDraft.emit(this.editableModule);
    } else {
      this.moduleForm.markAllAsTouched();
    }
  }
}
