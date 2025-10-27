import {CanActivateFn, Router} from '@angular/router';
import {inject} from '@angular/core';
import {UserService} from '../User/user-service';

export const authGuard: CanActivateFn = (route, state) => {
const userService = inject(UserService);
const router = inject(Router);

const user = userService.getUser();

if (!user) {
  router.navigate(['/login']);
  return false;
}
  return true;
};
