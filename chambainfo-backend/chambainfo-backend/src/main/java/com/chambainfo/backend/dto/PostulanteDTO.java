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
public class PostulanteDTO {
    
    private Long postulacionId;
    private Long trabajadorId;
    private String nombreCompleto;
    private String celular;
    private String mensaje;
    private String estado;
    private LocalDateTime fechaPostulacion;
    private String tiempoTranscurrido; // "hace 1 día", "hace 3 horas"
    private Boolean esNuevo;
}