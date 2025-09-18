import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import {PaymentFromDto, PaymentToDto} from '../models/payment_model';
import {UserService} from '../Services/User/user-service';
import {CourseService} from '../Services/Courses/course-service';
import {ToastService} from '../Services/ToastService/toast-service';
import {CurrencyPipe} from '@angular/common';
import {UserCourseAccessFromDto} from '../models/userCourseAccessModel';


@Component({
  selector: 'app-payment',
  imports: [
    ReactiveFormsModule,
    CurrencyPipe
  ],
  templateUrl: './payment.html',
  styleUrl: './payment.css'
})

export class Payment {
  countries: string[] = [
    'United States', 'Canada', 'United Kingdom', 'Australia', 'Germany',
    'France', 'Italy', 'Spain', 'Netherlands', 'Sweden',
    'Norway', 'Denmark', 'Finland', 'Switzerland', 'Belgium',
    'Austria', 'Ireland', 'New Zealand', 'South Africa', 'Brazil',
    'Mexico', 'Argentina', 'Japan', 'China', 'India',
    'South Korea', 'Singapore', 'Russia', 'Turkey', 'United Arab Emirates'
  ];
  paymentForm: FormGroup;
  courseId?: number;
  price?: number;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private userService: UserService,
    private courseService: CourseService,
    private toast :ToastService
  ) {
    this.paymentForm = this.fb.group({
      cardNumber: ['', [Validators.required, Validators.pattern('\\d{16}')]],
      expirationDate: ['', [Validators.required, Validators.pattern('(0[1-9]|1[0-2])/\\d{2}')]],
      cvv: ['', [Validators.required, Validators.pattern('\\d{3}')]],
      cardHolderName: ['', Validators.required],
      address: ['', Validators.required],
      country: ['', Validators.required],
      city: ['', Validators.required],
      zipCode: ['', Validators.required],
      state: ['', Validators.required]
    });

    this.route.queryParams.subscribe(params => {
      this.courseId = +params['courseId'];
      this.price = +params['price'];
    });
  }

  pay() {
    this.paymentForm.markAllAsTouched();
    console.log(this.userService.getUser());
    if (this.paymentForm.valid) {
      const paymentDto: PaymentToDto = {
        ...this.paymentForm.value,
        courseId: this.courseId,
        amount: this.price || 1,
        userId: this.userService.getUser()?.user_id || 0,
        paymentType: "COURSE_PURCHASE"
      };

      this.courseService.payAndGrantAccess(paymentDto, 'PURCHASED').subscribe({
        next: (res: UserCourseAccessFromDto) => {
          this.toast.success('Payment successful and course access granted!');
        },
        error: (err) => {
          console.error(err);
          this.toast.error('Payment or access grant failed!');
        }
      });
    }
  }
}
