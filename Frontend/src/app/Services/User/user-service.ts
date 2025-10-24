import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { jwtDecode } from 'jwt-decode';

interface JwtPayload {
  user_id: number;
  name: string;
  role: string;
  sub: string;
  profilePictureUrl: string;
  iat: number;
  exp: number;
}

export interface User {
  user_id: number;
  name: string;
  role: string;
  email: string;
  profile_picture: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private userSubject = new BehaviorSubject<User | null>(null);
  readonly user$ = this.userSubject.asObservable();

  constructor() {
    const token = localStorage.getItem('token');
    if (token) {
      const decodedUser = this.decodeToken(token);
      if (decodedUser) {
        this.userSubject.next(decodedUser);
      } else {
        localStorage.removeItem('token');
      }
    }
  }

  setUser(token: string) {
    const user = this.decodeToken(token);
    if (user) {
      this.userSubject.next(user);
    }
  }

  getUser(): User | null {
    return this.userSubject.value;
  }

  isInRole(...roles: string[]): boolean {
    const currentRole = this.userSubject.value?.role?.toUpperCase();
    return currentRole ? roles.some(role => role.toUpperCase() === currentRole) : false;
  }

  logOut() {
    localStorage.removeItem('token');
    this.userSubject.next(null);
  }

  private decodeToken(token: string): User | null {
    try {
      const decoded = jwtDecode<JwtPayload>(token);
      return {
        user_id: decoded.user_id,
        name: decoded.name,
        role: decoded.role,
        email: decoded.sub,
        profile_picture: decoded.profilePictureUrl
      };
    } catch (error) {
      return null;
    }
  }

}
