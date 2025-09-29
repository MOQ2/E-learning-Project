import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { jwtDecode } from 'jwt-decode';

interface JwtPayload {
  user_id: number;
  name: string;
  role: string;
  sub: string;
  profilePictureUrl: string;
  permissions: string[];
  iat: number;
  exp: number;
}

interface User {
  user_id: number;
  name: string;
  role: string;
  email: string;
  profile_picture: string;
  permissions: string[];
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private userSubject = new BehaviorSubject<User | null>(null);
  user$ = this.userSubject.asObservable();

  setUser(token: string) {
    const decoded = jwtDecode<JwtPayload>(token);
    const user: User = {
      user_id: decoded.user_id,
      name: decoded.name,
      role: decoded.role,
      email: decoded.sub,
      profile_picture:decoded.profilePictureUrl,
      permissions: decoded.permissions
    };
    this.userSubject.next(user);
  }

  getUser(): User | null {
    return this.userSubject.value;
  }

  logOut() {
    localStorage.removeItem('token');
    this.userSubject.next(null);
  }

}
