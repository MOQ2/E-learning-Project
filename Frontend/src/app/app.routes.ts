import { Routes } from '@angular/router';
import {Login} from './auth/login/login';
import {Signup} from './auth/signup/signup';
import {Payment} from './payment/payment';
import {Courses} from './courses/courses';
import {authGuard} from './Services/Auth/auth-guard';

export const routes: Routes = [
  {path: 'login',  component:Login},
  {path: 'signUp',  component:Signup},
  {path: 'courses', component:Courses , canActivate:[authGuard]},
  {path: 'payment',  component:Payment , canActivate:[authGuard]}
];

