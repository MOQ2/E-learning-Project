import { Component, Input, Output, EventEmitter } from '@angular/core';
import { ViewEncapsulation } from '@angular/core';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.html',
  styleUrls: ['./sidebar.css'],
  standalone: true,
  encapsulation: ViewEncapsulation.None
})
export class SidebarComponent {
  @Input() activeLink: 'overview' | 'modules' | 'lesson' | 'publish' = 'overview';
  @Input() courseCreated: boolean = false;
  @Output() activeLinkChange = new EventEmitter<'overview' | 'modules' | 'lesson' | 'publish'>();

  setActiveLink(link: 'overview' | 'modules' | 'lesson' | 'publish') {
    this.activeLink = link;
    this.activeLinkChange.emit(link);
  }
}
