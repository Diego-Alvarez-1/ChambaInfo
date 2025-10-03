import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  loginForm: FormGroup;
  errorMessage: string = '';
  isLoading: boolean = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      identificacion: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.loginForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';

      this.authService.login(this.loginForm.value).subscribe({
        next: (response) => {
          this.isLoading = false;
          localStorage.setItem('token', response.token);
          localStorage.setItem('user', JSON.stringify(response));
          
          if (response.tipoUsuario === 'TRABAJADOR') {
            this.router.navigate(['/ofertas']);
          } else {
            this.router.navigate(['/mis-ofertas']);
          }
        },
        error: (error) => {
          this.isLoading = false;
          this.errorMessage = error.error?.mensaje || 'Error al iniciar sesión';
        }
      });
    }
  }

  goToRegister(): void {
    this.router.navigate(['/registro']);
  }

  selectTipoUsuario(tipo: string): void {
    localStorage.setItem('tipoUsuarioSeleccionado', tipo);
    this.router.navigate(['/registro']);
  }
}