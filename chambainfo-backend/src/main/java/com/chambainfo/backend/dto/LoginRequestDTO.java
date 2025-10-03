
package com.chambainfo.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {
    
    @NotBlank(message = "El usuario es obligatorio")
    private String usuario;
    
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}