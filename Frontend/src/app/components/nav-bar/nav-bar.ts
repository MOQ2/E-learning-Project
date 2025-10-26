import { NgForOf, NgIf } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { SearchBarComponent } from '../search-bar/search-bar';
import { UserService, User } from '../../Services/User/user-service';

type NavLink = {
  label: string;
  route: string | any[];
  exact?: boolean;
  requiresAuth?: boolean;
  roles?: string[];
};

@Component({
  selector: 'app-nav-bar',
  imports: [SearchBarComponent, RouterLink, RouterLinkActive, NgIf, NgForOf],
  templateUrl: './nav-bar.html',
  styleUrl: './nav-bar.css',
  standalone: true
})
export class NavBar {
  private readonly router = inject(Router);
  private readonly userService = inject(UserService);

  private readonly instructorRoles = new Set<string>([
    'ADMIN',
    'TEACHER',
    'LEAD_INSTRUCTOR',
    'TEACHING_ASSISTANT',
    'COURSE_MODERATOR'
  ]);

  private readonly navLinks: NavLink[] = [
    { label: 'Home', route: '/', exact: true },
  { label: 'Courses', route: '/courses' },
  { label: 'My Learning', route: '/my-learning', requiresAuth: true, roles: ['USER'] },
    {
      label: 'My Courses',
      route: '/my-courses',
      requiresAuth: true,
      roles: ['ADMIN', 'TEACHER', 'LEAD_INSTRUCTOR', 'TEACHING_ASSISTANT', 'COURSE_MODERATOR']
    }
  ];

  readonly exactMatchOptions = { exact: true } as const;
  readonly defaultMatchOptions = { exact: false } as const;
  readonly currentUser = toSignal(this.userService.user$, { initialValue: this.userService.getUser() });

  readonly visibleLinks = computed(() => this.getLinksForUser(this.currentUser()));
  readonly showExploreCoursesButton = computed(() => this.shouldShowExploreButton(this.currentUser()));
  readonly showCreateCourseButton = computed(() => this.shouldShowCreateButton(this.currentUser()));
  readonly isAuthenticated = computed(() => !!this.currentUser());

  goToExplore(): void {
    this.router.navigate(['/courses']);
  }

  goToCreateCourse(): void {
    this.router.navigate(['/courses/editor/new']);
  }

  goToLogin(): void {
    this.router.navigate(['/login']);
  }

  logout(): void {
    this.userService.logOut();
    this.router.navigate(['/']);
  }

  private getLinksForUser(user: User | null): NavLink[] {
    const userRole = user?.role?.toUpperCase() ?? null;
    return this.navLinks.filter(link => {
      if (link.requiresAuth && !user) {
        return false;
      }

      if (!link.roles || link.roles.length === 0) {
        return true;
      }

      return link.roles.some(role => role.toUpperCase() === userRole);
    });
  }

  private shouldShowExploreButton(user: User | null): boolean {
    const role = user?.role?.toUpperCase();
    return !role || role === 'USER';
  }

  private shouldShowCreateButton(user: User | null): boolean {
    const role = user?.role?.toUpperCase();
    return role ? this.instructorRoles.has(role) : false;
  }
}
