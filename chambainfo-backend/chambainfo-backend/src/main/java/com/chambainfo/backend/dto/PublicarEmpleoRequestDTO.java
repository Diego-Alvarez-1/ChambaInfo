package com.chambainfo.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicarEmpleoRequestDTO {

    @NotBlank(message = "El nombre del empleo es obligatorio")
    private String nombreEmpleo;

    @NotBlank(message = "La descripción del empleo es obligatoria")
    private String descripcionEmpleo;

    @NotBlank(message = "El número de celular de contacto es obligatorio")
    private String celularContacto;

    private Boolean mostrarNumero = true;

    // Campos opcionales
    private String ubicacion;
    private String salario;
    private String ruc;
    private String adjuntos;
}