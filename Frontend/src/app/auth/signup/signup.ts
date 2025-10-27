import {Component, OnDestroy} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {Auth} from '../../Services/Auth/auth';
import {ToastService} from '../../Services/ToastService/toast-service';
import {RegisterRequestDTO} from '../../models/authDto';
import {Subject, takeUntil} from 'rxjs';
import {RouterLink} from '@angular/router';
@Component({
  selector: 'app-signup',
  imports: [
    ReactiveFormsModule,
    RouterLink
  ],
  templateUrl: './signup.html',
  styleUrls: ['./signup.css']
})
export class Signup implements OnDestroy{
  signUpForm: FormGroup;
  showPassword = false;
  private destroy$ = new Subject<void>();

  constructor(private fb: FormBuilder, private authService: Auth , private toast :ToastService) {
    this.signUpForm = this.fb.group({
        name: ['', Validators.required],
        email: ['', [Validators.required, Validators.email]],
        password: ['', [Validators.required, Validators.minLength(8)]],
        confirmPassword: ['', Validators.required],
        phone: [''],
        bio: [''],
      },
      {validators: this.passwordMatchValidator})

  }

  passwordMatchValidator(formGroup: FormGroup) {
    const password = formGroup.get('password')?.value;
    const confirmPassword = formGroup.get('confirmPassword')?.value;
    return password === confirmPassword ? null : {passwordMismatch: true};
  }

  getErrors(controlName: string): string[] {
    const control = this.signUpForm.get(controlName);
    if (!control || !control.touched) return [];

    const messages: Record<string, string> = {
      required: `${controlName} is required.`,
      email: 'Invalid email format.',
      minlength: controlName === 'password' ? 'Password must be at least 8 characters.' : ''
    };

    let errors = control.errors ? Object.keys(control.errors).map(key => messages[key]).filter(msg => msg !== '') : [];

    if (controlName === 'confirmPassword' && this.signUpForm.errors?.['passwordMismatch'] && control.value) {
      errors.push('Passwords do not match.');
    }

    return errors;
  }


  onSubmit() {
    this.signUpForm.markAllAsTouched();
    if (this.signUpForm.valid) {
      const { confirmPassword, ...rest } = this.signUpForm.value;
      const registerDTO: RegisterRequestDTO = rest;
      this.authService.signUp(registerDTO).pipe(takeUntil(this.destroy$))
        .subscribe({
        next: res => {
          localStorage.setItem('token', res.token);
          this.toast.success('Sign up successful!');
        },
        error: err => {
          this.toast.error(err.error.message);
        }
      });
    }
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

}
