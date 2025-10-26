
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ListCard } from '../../shared/list-card/list-card';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, ListCard],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css'
})
export class Sidebar {
  @Input() groupedModules: { title: string; items: any[]; key: string }[] = [];
  @Input() activeLessonId: number | null = null;
  @Input() progress: number = 0;
  @Input() loading: boolean = false;
  @Input() moduleCollapsed: { [key: string]: boolean } = {};

  @Output() lessonSelected = new EventEmitter<number>();
  @Output() moduleToggled = new EventEmitter<string>();
  @Output() toggleSidebar = new EventEmitter<void>();

  selectLesson(id: number) {
    this.lessonSelected.emit(id);
  }

  toggleModuleCollapse(key: string) {
    this.moduleToggled.emit(key);
  }

  trackByModule(index: number, item: any) {
    return item.key || index;
  }

  trackByLesson(index: number, item: any) {
    return item.id || item.videoId || index;
  }
}

