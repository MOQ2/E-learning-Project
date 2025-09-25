import { Routes } from '@angular/router';
import { HomePage } from './components/home-page/home-page';
import {Login} from './auth/login/login';
import {Signup} from './auth/signup/signup';
import {Payment} from './payment/payment';

import { ExploreCoursesPage } from './components/courses/explore-courses/explore-courses';
import {SubmitQuiz} from './Quizzes/submit-quiz/submit-quiz';
import {FetchQuizzes} from './Quizzes/fetch-quizzes/fetch-quizzes';


export const routes: Routes = [
  { path: '', component: ExploreCoursesPage},
  {path: 'login',  component:Login},
  {path: 'signUp',  component:Signup},
  {path: 'payment',  component:Payment},
  {path: 'submitQuiz' , component: SubmitQuiz},
  {path: 'quizzes' , component: FetchQuizzes},
  {path: 'courses', component:ExploreCoursesPage}

];

