package com.chambainfo.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpleoResponseDTO {

    private Long id;
    private String nombreEmpleo;
    private String descripcionEmpleo;
    private String celularContacto;
    private Boolean mostrarNumero;
    private String ubicacion;
    private String salario;
    private String ruc;
    private String adjuntos;

    // Datos del empleador
    private Long empleadorId;
    private String empleadorNombre;
    private String empleadorUsuario;

    private LocalDateTime fechaPublicacion;
    private LocalDateTime ultimaActualizacion;
    private Boolean activo;
}