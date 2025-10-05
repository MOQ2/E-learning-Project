import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';
import {AuthResponseDTO, LoginRequestDTO, RegisterRequestDTO} from '../../models/authDto';


@Injectable({
  providedIn: 'root'
})
export class Auth {
  private api = `${environment.apiUrl}`;

  constructor(private http:HttpClient) {
  }

  login(data: LoginRequestDTO): Observable<AuthResponseDTO> {
    return this.http.post<AuthResponseDTO>(`${this.api}/login`, data);
  }

  signUp(data: RegisterRequestDTO): Observable<AuthResponseDTO> {
    return this.http.post<AuthResponseDTO>(`${this.api}/register`, data);
  }
}
