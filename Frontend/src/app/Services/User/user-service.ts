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

interface User {
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

  setUser(token: string) {
    const decoded = jwtDecode<JwtPayload>(token);
    const user: User = {
      user_id: decoded.user_id,
      name: decoded.name,
      role: decoded.role,
      email: decoded.sub,
      profile_picture:decoded.profilePictureUrl
    };
    this.userSubject.next(user);
  }

  getUser(): User | null {
    const user = this.userSubject.value;
   
      const fakeUser: User = {
        user_id: 1,
        name: 'Fake User',
        role: 'student',
        email: 'fake@example.com',
        profile_picture: ''
      };
      return fakeUser;
    return user;
  }

  logOut() {
    localStorage.removeItem('token');
    this.userSubject.next(null);
  }

}
