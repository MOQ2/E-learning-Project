import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../Dtos/ApiResponse';
import {
  MyCoursesResponseDto,
  TeacherCourseDto,
  TeacherStatsDto
} from '../../Dtos/MyCourseDtos';

/**
 * Service for teacher's My Courses functionality
 */
@Injectable({
  providedIn: 'root'
})
export class MyCoursesService {
  private api = `${environment.apiUrl}`;

  constructor(private http: HttpClient) {}

  /**
   * Get complete My Courses dashboard for a teacher
   */
  getMyCoursesDashboard(): Observable<MyCoursesResponseDto> {
    return this.http.get<ApiResponse<MyCoursesResponseDto>>(
      `${this.api}/api/my-courses/dashboard`
    ).pipe(
      map(response => response.data)
    );
  }

  /**
   * Get all courses created by a teacher
   */
  getTeacherCourses(): Observable<TeacherCourseDto[]> {
    return this.http.get<ApiResponse<TeacherCourseDto[]>>(
      `${this.api}/api/my-courses/courses`
    ).pipe(
      map(response => response.data)
    );
  }

  /**
   * Get teacher statistics
   */
  getTeacherStats(): Observable<TeacherStatsDto> {
    return this.http.get<ApiResponse<TeacherStatsDto>>(
      `${this.api}/api/my-courses/stats`
    ).pipe(
      map(response => response.data)
    );
  }

  /**
   * Get recently updated courses
   */
  getRecentlyUpdatedCourses(limit: number = 5): Observable<TeacherCourseDto[]> {
    return this.http.get<ApiResponse<TeacherCourseDto[]>>(
      `${this.api}/api/my-courses/recent?limit=${limit}`
    ).pipe(
      map(response => response.data)
    );
  }

  /**
   * Get top performing courses
   */
  getTopPerformingCourses(limit: number = 5): Observable<TeacherCourseDto[]> {
    return this.http.get<ApiResponse<TeacherCourseDto[]>>(
      `${this.api}/api/my-courses/top?limit=${limit}`
    ).pipe(
      map(response => response.data)
    );
  }

  /**
   * Get course details with statistics
   */
  getCourseDetails(courseId: number): Observable<TeacherCourseDto> {
    return this.http.get<ApiResponse<TeacherCourseDto>>(
      `${this.api}/api/my-courses/course/${courseId}`
    ).pipe(
      map(response => response.data)
    );
  }
}
