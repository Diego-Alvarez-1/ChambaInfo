package com.chambainfo.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpleoConPostulacionesDTO {
    
    private Long id;
    private String nombreEmpleo;
    private String descripcionEmpleo;
    private Integer cantidadPostulaciones;
    private Integer nuevasPostulaciones;
    private LocalDateTime fechaPublicacion;
    private Boolean activo;
    private Integer diasRestantes;
    
    // Lista de postulantes
    private List<PostulanteDTO> postulantes;
}