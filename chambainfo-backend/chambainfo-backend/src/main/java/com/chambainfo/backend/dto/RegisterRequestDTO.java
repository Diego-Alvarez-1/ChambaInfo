package com.chambainfo.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDTO {

    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "^[0-9]{8}$", message = "El DNI debe tener 8 dígitos")
    private String dni;

    @NotBlank(message = "El usuario es obligatorio")
    @Size(min = 5, max = 50, message = "El usuario debe tener entre 5 y 50 caracteres")
    private String usuario;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @NotBlank(message = "Debe confirmar la contraseña")
    private String confirmPassword;

    @NotBlank(message = "El número de celular es obligatorio")
    @Pattern(regexp = "^[0-9]{9}$", message = "El celular debe tener 9 dígitos")
    private String celular;

    @NotBlank(message = "El rol es obligatorio")
    @Pattern(regexp = "^(TRABAJADOR|EMPLEADOR)$", message = "El rol debe ser TRABAJADOR o EMPLEADOR")
    private String rol; // NUEVO: añadir rol
}