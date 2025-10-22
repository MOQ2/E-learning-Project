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
import { EnrolledCoursePageComponent } from './components/enrolled-course-page/enrolled-course-page.component';
import { MyLearningPage } from './components/my-learning-page/my-learning-page';
import { MyCoursesPage } from './components/my-courses-page/my-courses-page';
import { authGuard } from './Services/Auth/auth-guard';

export const routes: Routes = [
  // make the course page the home page; it accepts optional id via /course/:id or ?courseId=
  { path: '', component: CoursePage },
  { path: 'course/:courseId/learn', component: LessonWrapper },
  { path: 'course/:courseId/enrolled', component: EnrolledCoursePageComponent },
  { path: 'my-course/:courseId', component: EnrolledCoursePageComponent },
  { path: 'course/:id', component: CoursePage },
  {path: 'login',  component:Login},
  {path: 'signUp',  component:Signup},
  {path: 'payment',  component:Payment},
  { path: 'courses', component: ExploreCoursesPage}, //, canActivate: [authGuard] 
  {path: 'my-learning', component: MyLearningPage},
  {path: 'my-courses', component: MyCoursesPage},
  {path: 'course-editor', component: CourseEditorPageComponent},
  {path: 'course-editor/:courseId', component: CourseEditorPageComponent},
  {path: 'courses/editor/new', component: CourseEditorPageComponent},
  {path: 'courses/editor/:courseId', component: CourseEditorPageComponent},
  {path: 'homepage', component: HomePage},
  { path: 'payment', component: Payment, canActivate: [authGuard] },
  { path: 'module/:moduleId/lesson/:lessonId', component: LessonWrapper },
  { path: 'module/:moduleId', component: LessonWrapper },
  { path: 'lesson/:lessonId', component: LessonWrapper }
];
