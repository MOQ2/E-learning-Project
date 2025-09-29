import { Routes } from '@angular/router';
import { Login } from './auth/login/login';
import { Signup } from './auth/signup/signup';
import { Payment } from './payment/payment';
import { ExploreCoursesPage } from './components/courses/explore-courses/explore-courses';
import { authGuard } from './Services/Auth/auth-guard';

export const routes: Routes = [
  { path: '', component: ExploreCoursesPage },
  { path: 'login', component: Login },
  { path: 'signUp', component: Signup },
  { path: 'courses', component: ExploreCoursesPage, canActivate: [authGuard] }, 
  { path: 'payment', component: Payment, canActivate: [authGuard] }
];
