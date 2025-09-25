import { Routes } from '@angular/router';
import { HomePage } from './components/home-page/home-page';
import {Login} from './auth/login/login';
import {Signup} from './auth/signup/signup';
import {Payment} from './payment/payment';
<<<<<<< HEAD
import {Courses} from './courses/courses';
import {SubmitQuiz} from './Quizzes/submit-quiz/submit-quiz';
import {FetchQuizzes} from './Quizzes/fetch-quizzes/fetch-quizzes';
=======
// import {Courses} from './courses/courses';
import { ExploreCoursesPage } from './components/courses/explore-courses/explore-courses';
>>>>>>> 277c0fe4f2a3ed4594bcfd0f7365f0341b716b09

export const routes: Routes = [
  { path: '', component: ExploreCoursesPage},
  {path: 'login',  component:Login},
  {path: 'signUp',  component:Signup},
  {path: 'payment',  component:Payment},
<<<<<<< HEAD
  {path: 'courses', component:Courses},
  {path: 'submitQuiz' , component: SubmitQuiz},
  {path: 'quizzes' , component: FetchQuizzes}

=======
  {path: 'courses', component:ExploreCoursesPage}
>>>>>>> 277c0fe4f2a3ed4594bcfd0f7365f0341b716b09
];

