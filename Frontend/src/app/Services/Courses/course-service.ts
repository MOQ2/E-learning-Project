import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../environments/environment';
import {map, Observable, switchMap} from 'rxjs';
import {PaymentToDto , PaymentFromDto} from '../../models/payment_model';
import {UserCourseAccessFromDto, UserCourseAccessToDto} from '../../models/userCourseAccessModel';
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

  createPayment(payment : PaymentToDto): Observable<PaymentFromDto> {
  return this.http.post<PaymentFromDto>(this.api+'/api/payments',payment);
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

}
