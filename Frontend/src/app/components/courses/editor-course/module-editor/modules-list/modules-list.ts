import { Component, Input, Output, EventEmitter } from '@angular/core';
import { Module } from '../../course-editor-page-component/course-editor-page-component';
import { CommonModule } from '@angular/common';
import { ViewEncapsulation } from '@angular/core';

@Component({
  selector: 'app-modules-list',
  templateUrl: './modules-list.html',
  styleUrls: ['./modules-list.css'],
  standalone: true,
  imports: [CommonModule],
  encapsulation: ViewEncapsulation.None
})
export class ModulesListComponent {
  /**
   * INPUT: The full list of modules to display.
   * Received from the parent component.
   */
  @Input() modules: Module[] = [];

  /**
   * INPUT: The currently selected module.
   * Used to apply a visual highlight to the active item in the list.
   */
  @Input() selectedModule: Module | null = null;

  /**
   * OUTPUT: Emits the selected module object when the user clicks 'Open'.
   * The parent component will listen for this event.
   */
  @Output() moduleSelected = new EventEmitter<Module>();

  /**
   * OUTPUT: Emits when the 'Add module' button is clicked.
   */
  @Output() addModule = new EventEmitter<void>();

  /**
   * Calculates the total number of lessons for a given module.
   * @param module The module to calculate lessons for.
   * @returns A descriptive string about the lessons and time.
   */
  getModuleMeta(module: Module): string {
    const lessonsCount = module.lessons?.length || 0;
    const lessonPlural = lessonsCount === 1 ? 'lesson' : 'lessons';
    return `${lessonsCount} ${lessonPlural} • ${module.estimatedTime} min`;
  }
}
