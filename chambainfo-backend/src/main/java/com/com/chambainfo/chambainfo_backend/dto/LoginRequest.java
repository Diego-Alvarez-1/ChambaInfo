package com.chambainfo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "La identificación es requerida")
    private String identificacion; // Puede ser DNI, celular o email
    
    @NotBlank(message = "La contraseña es requerida")
    private String password;
}