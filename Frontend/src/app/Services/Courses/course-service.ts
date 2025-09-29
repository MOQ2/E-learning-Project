import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { map, Observable, switchMap } from 'rxjs';

import { PaymentToDto, PaymentFromDto } from '../../models/payment_model';
import { UserCourseAccessFromDto, UserCourseAccessToDto } from '../../models/userCourseAccessModel';
import { CourseDto } from '../../Dtos/CourseDto';
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

createPayment(payment: PaymentToDto): Observable<PaymentFromDto> {
  return this.http.post<PaymentFromDto>(this.api + '/api/payments', payment);
}

grantAccess(grantDto: UserCourseAccessToDto): Observable<UserCourseAccessFromDto> {
  const params = new URLSearchParams();
  params.set('userId', grantDto.userId.toString());
  params.set('courseId', grantDto.courseId.toString());
  params.set('accessType', grantDto.accessType);
  if (grantDto.paymentId) params.set('paymentId', grantDto.paymentId.toString());

  return this.http.post<UserCourseAccessFromDto>(
    `${this.api}/api/user-course-access/grant-course-access?${params.toString()}`,
    null
  );
}

payAndGrantAccess(payment: PaymentToDto, accessType: 'PURCHASED'): Observable<UserCourseAccessFromDto> {
  return this.createPayment(payment).pipe(
    switchMap(paymentRes => {
      const grantDto: UserCourseAccessToDto = {
        userId: payment.userId,
        courseId: payment.courseId!,
        accessType: accessType,
        paymentId: paymentRes.id
      };
      return this.grantAccess(grantDto);
    })
  );
}

getCategories(): Observable<ApiResponse<TagDto[]>> {
  return this.http.get<ApiResponse<TagDto[]>>(`${this.api}/api/courses/categories`);
}


}
