import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../environments/environment';
import {map, Observable} from 'rxjs';
// import {Courses} from '../../courses/courses';
import { CourseDto } from '../../Dtos/CourseDto';
import { HttpParams } from '@angular/common/http';
import { CourseFilterParams } from '../../Dtos/CourseFilterParams';
import { ApiResponse } from '../../Dtos/ApiResponse';
import { PageResponse } from '../../Dtos/PageResponse';
import { TagDto } from '../../Dtos/TagDto';


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

  // getCourses(): Observable<CourseInterface[]> {
  //   return this.http.get<{success: boolean,message: string ,data: CourseInterface[]}>(this.api+'/api/courses/all')
  //     .pipe(
  //       map(res => res.data)
  //     )
  // }







  getCourses(filters: CourseFilterParams = {}): Observable<ApiResponse<PageResponse<CourseDto>>> {
    let params = new HttpParams();

    // Add pagination parameters
    if (filters.page !== undefined) {
      params = params.set('page', filters.page.toString());
    }
    if (filters.size !== undefined) {
      params = params.set('size', filters.size.toString());
    }
    if (filters.sort) {
      params = params.set('sort', filters.sort);
    }

    // Add search filters
    if (filters.name) {
      params = params.set('name', filters.name);
    }
    if (filters.description) {
      params = params.set('description', filters.description);
    }

    // Add price filters
    if (filters.minPrice !== undefined) {
      params = params.set('minPrice', filters.minPrice.toString());
    }
    if (filters.maxPrice !== undefined) {
      params = params.set('maxPrice', filters.maxPrice.toString());
    }
    if (filters.currency) {
      params = params.set('currency', filters.currency);
    }

    // Add duration filters
    if (filters.minDurationHours !== undefined) {
      params = params.set('minDurationHours', filters.minDurationHours.toString());
    }
    if (filters.maxDurationHours !== undefined) {
      params = params.set('maxDurationHours', filters.maxDurationHours.toString());
    }

    // Add boolean filters
    if (filters.isActive !== undefined) {
      params = params.set('isActive', filters.isActive.toString());
    }
    if (filters.isFree !== undefined) {
      params = params.set('isFree', filters.isFree.toString());
    }

    // Add user filter
    if (filters.createdByUserId !== undefined) {
      params = params.set('createdByUserId', filters.createdByUserId.toString());
    }

    // Add array filters
    if (filters.statuses && filters.statuses.length > 0) {
      filters.statuses.forEach(status => {
        params = params.append('statuses', status);
      });
    }
    if (filters.difficultyLevels && filters.difficultyLevels.length > 0) {
      filters.difficultyLevels.forEach(level => {
        params = params.append('difficultyLevels', level.toUpperCase());
      });
    }

    if (filters.tags && filters.tags.length > 0) {
      const tagsJson = JSON.stringify(filters.tags.map(tag => ({ name: tag })));
      params = params.set('tags', tagsJson);
    }

    return this.http.get<ApiResponse<PageResponse<CourseDto>>>(`${this.api}/api/courses`, { params });
  }



  getCategories(): Observable<ApiResponse<TagDto[]>> {
    return this.http.get<ApiResponse<TagDto[]>>(`${this.api}/api/courses/categories`);
  }

  uploadAttachment(file: File, name: string): Observable<number> {
    const formData = new FormData();
    console.log("Uploading file:", file);
    formData.append('file', file);
    formData.append('name', name);
    return this.http.post<ApiResponse<number>>(`${this.api}/api/attachments`, formData).pipe(
      map(response => response.data)
    );
  }

  createCourse(courseData: any): Observable<any> {
    const formData = new FormData();
    for (const key in courseData) {
      if (courseData[key] != null) {
        if (Array.isArray(courseData[key])) {
          courseData[key].forEach((item: any) => formData.append(key, item));
        } else {
          formData.append(key, courseData[key].toString());
        }
      }
    }
    return this.http.post<ApiResponse<any>>(`${this.api}/api/courses`, formData).pipe(
      map(response => response.data)
    );
  }

  createModule(moduleData: any): Observable<number> {
    const headers = { 'Content-Type': 'application/json' };
    return this.http.post<ApiResponse<number>>(`${this.api}/api/modules`, moduleData, { headers }).pipe(
      map(response => response.data)
    );
  }

  updateModule(moduleId: number, moduleData: any): Observable<any> {
    const headers = { 'Content-Type': 'application/json' };
    return this.http.put<ApiResponse<any>>(`${this.api}/api/modules/${moduleId}`, moduleData, { headers }).pipe(
      map(response => response.data)
    );
  }

  addModuleToCourse(courseId: number, moduleId: number, order: number): Observable<any> {
    return this.http.post<ApiResponse<any>>(`${this.api}/api/courses/${courseId}/modules/${moduleId}/${order}`, {}, {}).pipe(
      map(response => response.data)
    );
  }

  updateCourse(courseId: number, courseData: any): Observable<any> {
    const formData = new FormData();
    for (const key in courseData) {
      if (courseData[key] != null) {
        if (Array.isArray(courseData[key])) {
          courseData[key].forEach((item: any) => formData.append(key, item));
        } else {
          formData.append(key, courseData[key].toString());
        }
      }
    }
    return this.http.put<ApiResponse<any>>(`${this.api}/api/courses/${courseId}`, formData).pipe(
      map(response => response.data)
    );
  }

  createLesson(lessonData: FormData): Observable<number> {
    return this.http.post<ApiResponse<any>>(`${this.api}/api/lessons/upload`, lessonData).pipe(
      map(response => response.data.id)
    );
  }

  updateLesson(lessonId: number, lessonData: FormData): Observable<any> {
    return this.http.put<ApiResponse<any>>(`${this.api}/api/lessons/${lessonId}`, lessonData).pipe(
      map(response => response.data)
    );
  }

  addLessonToModule(moduleId: number, lessonId: number, order: number): Observable<any> {
    return this.http.post<ApiResponse<any>>(`${this.api}/api/modules/${moduleId}/videos/${lessonId}?order=${order}`, {}).pipe(
      map(response => response.data)
    );
  }

  addAttachmentToLesson(lessonId: number, attachmentId: number): Observable<any> {
    return this.http.post<ApiResponse<any>>(`${this.api}/api/lessons/${lessonId}/attachments/${attachmentId}`, {}).pipe(
      map(response => response.data)
    );
  }

  removeAttachmentFromLesson(lessonId: number, attachmentId: number): Observable<any> {
    return this.http.delete<ApiResponse<any>>(`${this.api}/api/lessons/${lessonId}/attachments/${attachmentId}`).pipe(
      map(response => response.data)
    );
  }

  deleteLesson(lessonId: number): Observable<any> {
    return this.http.delete<ApiResponse<any>>(`${this.api}/api/videos/${lessonId}`).pipe(
      map(response => response.data)
    );
  }

  deleteModule(moduleId: number): Observable<any> {
    return this.http.delete<ApiResponse<any>>(`${this.api}/api/modules/${moduleId}`).pipe(
      map(response => response.data)
    );
  }

  removeModuleFromCourse(courseId: number, moduleId: number): Observable<any> {
    return this.http.delete<ApiResponse<any>>(`${this.api}/api/courses/${courseId}/modules/${moduleId}/`).pipe(
      map(response => response.data)
    );
  }

  updateModuleOrderInCourse(courseId: number, moduleId: number, newOrder: number): Observable<any> {
    return this.http.put<ApiResponse<any>>(`${this.api}/api/courses/${courseId}/modules/${moduleId}/order/${newOrder}`, {}).pipe(
      map(response => response.data)
    );
  }

  getLesson(lessonId: number): Observable<any> {
    return this.http.get<ApiResponse<any>>(`${this.api}/api/lessons/${lessonId}`).pipe(
      map(response => response.data)
    );
  }

  getModule(moduleId: number): Observable<any> {
    return this.http.get<ApiResponse<any>>(`${this.api}/api/modules/${moduleId}`).pipe(
      map(response => response.data)
    );
  }

  getLessons(): Observable<any> {
    return this.http.get<ApiResponse<any>>(`${this.api}/api/lessons`).pipe(
      map(response => response.data)
    );
  }
}
