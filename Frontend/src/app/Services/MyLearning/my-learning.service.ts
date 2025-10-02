import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../Dtos/ApiResponse';
import {
  MyLearningResponseDto,
  EnrolledCourseDto,
  MyLearningStatsDto
} from '../../Dtos/MyLearningDtos';

@Injectable({
  providedIn: 'root'
})
export class MyLearningService {
  private api = `${environment.apiUrl}`;

  constructor(private http: HttpClient) {}

  /**
   * Get complete learning dashboard for a user
   */
  getMyLearningDashboard(userId: number): Observable<MyLearningResponseDto> {
    return this.http.get<ApiResponse<MyLearningResponseDto>>(
      `${this.api}/api/my-learning/dashboard/${userId}`
    ).pipe(
      map(response => response.data)
    );
  }

  /**
   * Get all enrolled courses for a user
   */
  getEnrolledCourses(userId: number): Observable<EnrolledCourseDto[]> {
    return this.http.get<ApiResponse<EnrolledCourseDto[]>>(
      `${this.api}/api/my-learning/courses/${userId}`
    ).pipe(
      map(response => response.data)
    );
  }

  /**
   * Get learning statistics for a user
   */
  getLearningStats(userId: number): Observable<MyLearningStatsDto> {
    return this.http.get<ApiResponse<MyLearningStatsDto>>(
      `${this.api}/api/my-learning/stats/${userId}`
    ).pipe(
      map(response => response.data)
    );
  }

  /**
   * Get recently accessed courses (Continue Learning)
   */
  getContinueLearning(userId: number, limit: number = 5): Observable<EnrolledCourseDto[]> {
    return this.http.get<ApiResponse<EnrolledCourseDto[]>>(
      `${this.api}/api/my-learning/continue/${userId}?limit=${limit}`
    ).pipe(
      map(response => response.data)
    );
  }

  /**
   * Get courses with expiring access
   */
  getUpcomingDeadlines(userId: number, daysThreshold: number = 30): Observable<EnrolledCourseDto[]> {
    return this.http.get<ApiResponse<EnrolledCourseDto[]>>(
      `${this.api}/api/my-learning/deadlines/${userId}?daysThreshold=${daysThreshold}`
    ).pipe(
      map(response => response.data)
    );
  }

  /**
   * Get enrolled course details with progress
   */
  getEnrolledCourseDetails(userId: number, courseId: number): Observable<EnrolledCourseDto> {
    return this.http.get<ApiResponse<EnrolledCourseDto>>(
      `${this.api}/api/my-learning/course/${userId}/${courseId}`
    ).pipe(
      map(response => response.data)
    );
  }
}
