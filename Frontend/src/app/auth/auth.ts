import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';

interface AuthResponse{
  token : string;
}

@Injectable({
  providedIn: 'root'
})
export class Auth {
  private api = `${environment.apiUrl}`;

  constructor(private http:HttpClient) {
  }

  login(email : string , password : string) : Observable<AuthResponse>
  {
    return this.http.post<AuthResponse>(`${this.api}/login` , {email , password});
  }

  signUp(name : string ,email : string , password : string ,phone : string , bio : string) : Observable<AuthResponse>
  {
    return this.http.post<AuthResponse>(`${this.api}/register` , {name ,email , password,phone , bio});
  }
}
