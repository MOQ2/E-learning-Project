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
  @Input() activeLink: 'overview' | 'modules' | 'add-lesson' | 'publish' = 'overview';
  @Output() activeLinkChange = new EventEmitter<'overview' | 'modules' | 'add-lesson' | 'publish'>();

  setActiveLink(link: 'overview' | 'modules' | 'add-lesson' | 'publish') {
    this.activeLink = link;
    this.activeLinkChange.emit(link);
  }
}
