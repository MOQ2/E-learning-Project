import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.html',
  styleUrls: ['./sidebar.css'],
  standalone: true
})
export class SidebarComponent {
  @Input() activeLink: 'overview' | 'modules' | 'add-lesson' | 'quizzes' | 'publish' = 'overview';
}
