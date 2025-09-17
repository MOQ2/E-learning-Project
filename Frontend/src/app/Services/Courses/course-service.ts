import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../environments/environment';
import {map, Observable} from 'rxjs';
import {Courses} from '../../courses/courses';

export interface CourseInterface {
  id: number;
  name: string;
  description: string;
  status: string;
  difficultyLevel: string;
  isActive: boolean;
  oneTimePrice: number;
  currency: string;
}

@Injectable({
  providedIn: 'root'
})
export class CourseService {
private api = `${environment.apiUrl}`
  constructor(private http: HttpClient) {}

  getCourses(): Observable<CourseInterface[]> {
    return this.http.get<{success: boolean,message: string ,data: CourseInterface[]}>(this.api+'/api/courses/all')
      .pipe(
        map(res => res.data)
      )
  }

}
