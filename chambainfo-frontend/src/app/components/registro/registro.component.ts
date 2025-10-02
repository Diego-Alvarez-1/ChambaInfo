import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-registro',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './registro.component.html',
  styleUrl: './registro.component.css'
})
export class RegistroComponent implements OnInit {
  registroForm: FormGroup;
  errorMessage: string = '';
  isLoading: boolean = false;
  dniVerificado: boolean = false;
  nombres: string = '';
  apellidos: string = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.registroForm = this.fb.group({
      dni: ['', [Validators.required, Validators.pattern(/^\d{8}$/)]],
      celular: ['', [Validators.required]],
      email: [''],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmarPassword: ['', Validators.required]
    });
  }

  ngOnInit(): void {}

  verificarDni(): void {
    const dni = this.registroForm.get('dni')?.value;
    
    if (dni && dni.length === 8) {
      this.isLoading = true;
      this.authService.verificarDni(dni).subscribe({
        next: (response) => {
          this.isLoading = false;
          this.dniVerificado = true;
          this.nombres = response.nombres;
          this.apellidos = `${response.apellidoPaterno} ${response.apellidoMaterno}`;
          this.errorMessage = '';
        },
        error: (error) => {
          this.isLoading = false;
          this.dniVerificado = false;
          this.errorMessage = 'DNI no encontrado en RENIEC';
        }
      });
    }
  }

  onSubmit(): void {
    if (this.registroForm.valid && this.dniVerificado) {
      const password = this.registroForm.get('password')?.value;
      const confirmarPassword = this.registroForm.get('confirmarPassword')?.value;

      if (password !== confirmarPassword) {
        this.errorMessage = 'Las contraseñas no coinciden';
        return;
      }

      this.isLoading = true;
      this.errorMessage = '';

      const tipoUsuario = localStorage.getItem('tipoUsuarioSeleccionado') || 'EMPLEADOR';
      
      const request = {
        ...this.registroForm.value,
        tipoUsuario: tipoUsuario
      };

      this.authService.register(request).subscribe({
        next: (response) => {
          this.isLoading = false;
          if (response.tipoUsuario === 'TRABAJADOR') {
            this.router.navigate(['/ofertas']);
          } else {
            this.router.navigate(['/mis-ofertas']);
          }
        },
        error: (error) => {
          this.isLoading = false;
          this.errorMessage = error.error?.mensaje || 'Error al crear la cuenta';
        }
      });
    } else if (!this.dniVerificado) {
      this.errorMessage = 'Debes verificar tu DNI primero';
    }
  }

  goToLogin(): void {
    this.router.navigate(['/login']);
  }
}