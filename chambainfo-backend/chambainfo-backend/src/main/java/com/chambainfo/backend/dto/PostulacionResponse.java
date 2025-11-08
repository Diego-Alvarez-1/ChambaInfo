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
public class PostulacionResponse {

    private Long id;
    private Long empleoId;
    private String nombreEmpleo;
    private Long trabajadorId;
    private String trabajadorNombre;
    private String trabajadorDni;
    private String trabajadorCelular;
    private String mensaje;
    private String estado;
    private LocalDateTime fechaPostulacion;
}