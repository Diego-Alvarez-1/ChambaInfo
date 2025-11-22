
package com.chambainfo.backend.service;

import com.chambainfo.backend.dto.ActualizarPerfilDTO;
import com.chambainfo.backend.dto.AuthResponseDTO;
import com.chambainfo.backend.dto.LoginRequestDTO;
import com.chambainfo.backend.dto.RegisterRequestDTO;

public interface AuthService {
    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param request Los datos de registro del usuario.
     * @return Una respuesta con los datos de autenticación del usuario registrado.
     */
    AuthResponseDTO register(RegisterRequestDTO request);
    
    /**
     * Inicia sesión con las credenciales del usuario.
     *
     * @param request Los datos de login (usuario y contraseña).
     * @return Una respuesta con los datos de autenticación del usuario.
     */
    AuthResponseDTO login(LoginRequestDTO request);

    /**
     * Actualiza la información adicional del perfil del usuario.
     *
     * @param request Los datos a actualizar.
     * @param usuarioAutenticado El nombre de usuario autenticado.
     */
    void actualizarPerfil(ActualizarPerfilDTO request, String usuarioAutenticado);
}