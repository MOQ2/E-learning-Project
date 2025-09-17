import {Component} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {Auth} from '../auth';
import {ToastService} from '../../Services/ToastService/toast-service';
@Component({
  selector: 'app-signup',
  imports: [
    ReactiveFormsModule
  ],
  templateUrl: './signup.html',
  styleUrls: ['./signup.css']
})
export class Signup {
  signUpForm: FormGroup;
  showPassword = false;

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

  onSubmit() {
    this.signUpForm.markAllAsTouched();
    if (this.signUpForm.valid) {
      const {name, email, password, phone, bio} = this.signUpForm.value;
      this.authService.signUp(name, email, password, phone, bio).subscribe({
        next: res => {
          localStorage.setItem('token', res.token);
          this.toast.success('Sign up successful!');
        },
        error: err => {
          console.log(err);
          this.toast.error('Sign up failed!');
        }
      })
    }
  }
}
