import { Component } from '@angular/core';
import {RouterLink} from '@angular/router';
import {UserService} from '../../Services/User/user-service';

@Component({
  selector: 'app-nav-bar',
  imports: [
    RouterLink
  ],
  templateUrl: './nav-bar.html',
  styleUrl: './nav-bar.css'
})
export class NavBar {
  isLoggedIn = false;
  activeLink: string = 'Courses'; // Default active link

  setActiveLink(linkName: string): void {
    this.activeLink = linkName;
  }

  isActive(linkName: string): boolean {
    return this.activeLink === linkName;
  }

  constructor(private userService: UserService) {
    this.userService.user$.subscribe(user => {
      this.isLoggedIn = !!user;
    });
  }

  logout() {
    this.userService.logOut();
  }
}
