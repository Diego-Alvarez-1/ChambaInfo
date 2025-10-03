export interface AuthResponse {
  id: number;
  dni: string;
  nombres: string;
  apellidos: string;
  celular?: string;
  email?: string;
  tipoUsuario: 'TRABAJADOR' | 'EMPLEADOR';
  token: string;
  mensaje: string;
}