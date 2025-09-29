import { Routes } from '@angular/router';
import { Login } from './auth/login/login';
import { Signup } from './auth/signup/signup';
import { Payment } from './payment/payment';
import { ExploreCoursesPage } from './components/courses/explore-courses/explore-courses';
import { FetchQuizzes } from './Quizzes/fetch-quizzes/fetch-quizzes';
import { QuizSubmission } from './Quizzes/quiz-submission/quiz-submmision';
import { authGuard } from './Services/Auth/auth-guard';

export const routes: Routes = [
  { path: '', component: ExploreCoursesPage, data: { breadcrumb: 'Home' } },
  { path: 'login', component: Login, data: { breadcrumb: 'Login' } },
  { path: 'signUp', component: Signup, data: { breadcrumb: 'Sign Up' } },
  { path: 'courses', component: ExploreCoursesPage, canActivate: [authGuard], data: { breadcrumb: 'Courses' } },
  { path: 'payment', component: Payment, canActivate: [authGuard], data: { breadcrumb: 'Payment' } },

  {
    path: 'quizzes',
    data: { breadcrumb: 'Quizzes' },
    canActivate: [authGuard],
    children: [
      { path: '', component: FetchQuizzes },
      {
        path: 'quizSubmission/:id',
        component: QuizSubmission,
        data: { breadcrumb: 'QuizSubmission' }
      }
    ]
  },

  { path: '**', redirectTo: 'login' },
];
