import { Routes } from '@angular/router';
import { HomePage } from './components/home-page/home-page';
import {Login} from './auth/login/login';
import {Signup} from './auth/signup/signup';
import {Payment} from './payment/payment';
// import {Courses} from './courses/courses';
import { CourseEditorPageComponent } from './components/courses/editor-course/course-editor-page-component/course-editor-page-component';
import { ExploreCoursesPage } from './components/courses/explore-courses/explore-courses';
import { CoursePage } from './components/course-page/course-page';
import { LessonWrapper } from './components/lesson-wrapper/lesson-wrapper';

export const routes: Routes = [
  // make the course page the home page; it accepts optional id via /course/:id or ?courseId=
  { path: '', component: CoursePage },
  // direct learning view for a course: loads LessonWrapper and (by default) selects the first module
  // and first lesson when no module/lesson are specified. Component reads `courseId` route param.
  { path: 'course/:courseId/learn', component: LessonWrapper },
  { path: 'course/:id', component: CoursePage },
  {path: 'login',  component:Login},
  {path: 'signUp',  component:Signup},
  {path: 'payment',  component:Payment},
  {path: 'courses', component:ExploreCoursesPage},
  {path: 'course-editor', component: CourseEditorPageComponent},
  {path: 'course-editor/:courseId', component: CourseEditorPageComponent},
  {path: 'homepage', component: HomePage},
  // module lessons: /module/:moduleId/lesson/:lessonId
  { path: 'module/:moduleId/lesson/:lessonId', component: LessonWrapper },
  { path: 'module/:moduleId', component: LessonWrapper },
  { path: 'lesson/:lessonId', component: LessonWrapper }
];

