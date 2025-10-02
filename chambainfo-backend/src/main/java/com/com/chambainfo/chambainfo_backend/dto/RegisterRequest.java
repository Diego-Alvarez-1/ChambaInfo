package com.chambainfo.dto;

import com.chambainfo.model.TipoUsuario;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "El DNI es requerido")
    @Pattern(regexp = "\\d{8}", message = "El DNI debe tener 8 dígitos")
    private String dni;
    
    @NotBlank(message = "El celular es requerido")
    @Pattern(regexp = "\\+?51\\s?\\d{3}\\s?\\d{3}\\s?\\d{3}", message = "Formato de celular inválido")
    private String celular;
    
    @Email(message = "Email inválido")
    private String email;
    
    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;
    
    @NotBlank(message = "Debe confirmar la contraseña")
    private String confirmarPassword;
    
    @NotNull(message = "El tipo de usuario es requerido")
    private TipoUsuario tipoUsuario;
}