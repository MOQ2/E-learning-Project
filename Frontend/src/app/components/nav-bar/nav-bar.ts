import { Component } from '@angular/core';
import { SearchBarComponent } from '../search-bar/search-bar';

@Component({
  selector: 'app-nav-bar',
  imports: [SearchBarComponent],
  templateUrl: './nav-bar.html',
  styleUrl: './nav-bar.css',
  standalone: true
})
export class NavBar {
  activeLink: string = 'Courses'; // Default active link

  setActiveLink(linkName: string): void {
    this.activeLink = linkName;
  }

  isActive(linkName: string): boolean {
    return this.activeLink === linkName;
  }
}
