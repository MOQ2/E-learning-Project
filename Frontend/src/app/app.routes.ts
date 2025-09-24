import { Routes } from '@angular/router';
import { HomePage } from './components/home-page/home-page';
import {Login} from './auth/login/login';
import {Signup} from './auth/signup/signup';
import {Payment} from './payment/payment';
// import {Courses} from './courses/courses';
import { CourseEditorPageComponent } from './components/courses/editor-course/course-overview/course-editor-page-component/course-editor-page-component';
import { ExploreCoursesPage } from './components/courses/explore-courses/explore-courses';

export const routes: Routes = [
  { path: '', component: CourseEditorPageComponent},
  {path: 'login',  component:Login},
  {path: 'signUp',  component:Signup},
  {path: 'payment',  component:Payment},
  {path: 'courses', component:ExploreCoursesPage}
];

