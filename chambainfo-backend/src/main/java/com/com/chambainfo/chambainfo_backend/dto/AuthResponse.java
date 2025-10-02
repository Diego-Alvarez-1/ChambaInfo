package com.chambainfo.dto;

import com.chambainfo.model.TipoUsuario;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private Long id;
    private String dni;
    private String nombres;
    private String apellidos;
    private String celular;
    private String email;
    private TipoUsuario tipoUsuario;
    private String token;
    private String mensaje;
}