import { Routes } from '@angular/router';
import {Login} from './auth/login/login';
import {Signup} from './auth/signup/signup';
import {Payment} from './payment/payment';
import { ExploreCoursesPage } from './components/courses/explore-courses/explore-courses';
import {FetchQuizzes} from './Quizzes/fetch-quizzes/fetch-quizzes';
import {QuizSubmission} from './Quizzes/quiz-submission/quiz-submmision';


export const routes: Routes = [
  { path: '', component: ExploreCoursesPage},
  {path: 'login',  component:Login},
  {path: 'signUp',  component:Signup},
  {path: 'payment',  component:Payment},
  {path: 'quizzes' , component: FetchQuizzes},
  {path: 'courses', component:ExploreCoursesPage},
  {path: 'quizSubmission/:id' , component: QuizSubmission},


];

