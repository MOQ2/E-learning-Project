
import { Component, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';
import { Module } from '../../course-editor-page-component/course-editor-page-component';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ViewEncapsulation } from '@angular/core';

@Component({
  selector: 'app-module-form',
  templateUrl: './module-form.html',
  styleUrls: ['./module-form.css'],
  standalone: true,
  imports: [FormsModule, CommonModule],
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

  // A local copy of the module to avoid directly mutating the input object.
  // This is good practice and prevents unintended side effects.
  editableModule!: Module;

  /**
   * A lifecycle hook that runs when any @Input() property changes.
   * We use it to create a safe, local copy of the module for editing.
   */
  ngOnChanges(changes: SimpleChanges): void {
    if (changes['module'] && this.module) {
      // Create a deep copy to prevent two-way binding issues
      this.editableModule = JSON.parse(JSON.stringify(this.module));
    }
  }

  /**
   * Emits the current state of the editableModule to the parent.
   */
  onSaveChanges(): void {
    this.saveDraft.emit(this.editableModule);
  }
}
