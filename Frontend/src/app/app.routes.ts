import { Routes } from '@angular/router';
import { HomePage } from './components/home-page/home-page';
import {Login} from './auth/login/login';
import {Signup} from './auth/signup/signup';
import {Payment} from './payment/payment';
// import {Courses} from './courses/courses';
import { CourseEditorPageComponent } from './components/courses/editor-course/course-editor-page-component/course-editor-page-component';
import { ExploreCoursesPage } from './components/courses/explore-courses/explore-courses';
import { CoursePage } from './components/course-page/course-page';

export const routes: Routes = [
  // make the course page the home page; it accepts optional id via /course/:id or ?courseId=
  { path: '', component: CoursePage },
  { path: 'course/:id', component: CoursePage },
  {path: 'login',  component:Login},
  {path: 'signUp',  component:Signup},
  {path: 'payment',  component:Payment},
  {path: 'courses', component:ExploreCoursesPage}
];

