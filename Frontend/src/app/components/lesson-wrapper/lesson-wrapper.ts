import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavBar } from '../nav-bar/nav-bar';
import { LessonPage } from './lesson-page/lesson-page';

@Component({
  selector: 'app-lesson-wrapper',
  standalone: true,
  imports: [CommonModule, NavBar, LessonPage],
  template: `
    <app-nav-bar></app-nav-bar>
    <div class="lesson-wrapper">
      <app-lesson-page></app-lesson-page>
    </div>
  `,
  styles: [`.lesson-wrapper { padding: 16px; }`]
})
export class LessonWrapper {}
