import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Auth } from '../auth';
import {ToastService} from '../../Services/ToastService/toast-service';
import {UserService} from '../../Services/user/user-service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class Login {
  loginForm: FormGroup;

  constructor(private fb: FormBuilder, private authService: Auth,private toast :ToastService,
              private userService: UserService , private router: Router
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]]
    });
  }

  onSubmit() {
    this.loginForm.markAllAsTouched();
    if (this.loginForm.valid) {
      const { email, password } = this.loginForm.value;
      this.authService.login(email, password).subscribe({
        next: (res) => {
          localStorage.setItem('token', res.token);
          this.userService.setUser(res.token);
          this.toast.success('Login successful!');
          this.router.navigate(['/']);
        },
        error: () => {
          this.toast.error('Login failed!');
        }
      });
    }
  }
}
