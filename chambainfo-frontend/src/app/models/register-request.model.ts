export interface RegisterRequest {
  dni: string;
  celular: string;
  email?: string;
  password: string;
  confirmarPassword: string;
  tipoUsuario: 'TRABAJADOR' | 'EMPLEADOR';
}