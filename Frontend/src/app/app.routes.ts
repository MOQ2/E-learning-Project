import { Routes } from '@angular/router';
import { HomePage } from './components/home-page/home-page';
import {Login} from './auth/login/login';
import {Signup} from './auth/signup/signup';
import {Payment} from './payment/payment';
import {Courses} from './courses/courses';
import {SubmitQuiz} from './Quizzes/submit-quiz/submit-quiz';
import {FetchQuizzes} from './Quizzes/fetch-quizzes/fetch-quizzes';

export const routes: Routes = [
  { path: '', component: HomePage},
  {path: 'login',  component:Login},
  {path: 'signUp',  component:Signup},
  {path: 'payment',  component:Payment},
  {path: 'courses', component:Courses},
  {path: 'submitQuiz' , component: SubmitQuiz},
  {path: 'quizzes' , component: FetchQuizzes}

];

