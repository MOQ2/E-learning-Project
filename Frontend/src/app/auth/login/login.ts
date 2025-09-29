import {Component, OnDestroy} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Auth } from '../../Services/Auth/auth';
import {ToastService} from '../../Services/ToastService/toast-service';
import {UserService} from '../../Services/User/user-service';
import {Router, RouterLink} from '@angular/router';
import {LoginRequestDTO} from '../../models/authDto';
import {Subject, takeUntil} from 'rxjs';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class Login implements OnDestroy{
  loginForm: FormGroup;
  private destroy$ = new Subject<void>();

  constructor(private fb: FormBuilder, private authService: Auth,private toast :ToastService,
              private userService: UserService , private router: Router
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]]
    });
  }

  getErrors(controlName: string): string[] {
    const control = this.loginForm.get(controlName);
    if (!control || !control.touched || !control.errors) return [];

    const messages: Record<string, string> = {
      required: `${controlName} is required.`,
      email: 'Invalid email format.',
      minlength: controlName === 'password'
        ? 'Password must be at least 8 characters.'
        : ''
    };

    return Object.keys(control.errors).map(key => messages[key]).filter(msg => msg !== '');
  }


  onSubmit() {
    this.loginForm.markAllAsTouched();
    if (this.loginForm.valid) {
      const loginDTO: LoginRequestDTO = this.loginForm.value;
      this.authService.login(loginDTO).pipe(takeUntil(this.destroy$))
        .subscribe({
        next: (res) => {
          localStorage.setItem('token', res.token);
          this.userService.setUser(res.token);
          this.toast.success('Login successful!');

          this.router.navigate(['/quizzes']);

        },
        error: (err) => {
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
