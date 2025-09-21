import { Component } from '@angular/core';
import { SidebarComponent } from '../sidebare/sidebar';
import { NavBar } from '../../nav-bar/nav-bar';
import { Footer } from '../../footer/footer';
import {CourseGridComponent} from '../course-grid/course-grid';

@Component({
  selector: 'app-explore-courses',
  imports: [SidebarComponent, NavBar, Footer, CourseGridComponent],
  templateUrl: './explore-courses.html',
  styleUrls: ['./explore-courses.css'],
  standalone: true
})
export class ExploreCoursesPage {

}
