package com.chambainfo.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadisticasEmpleadorDTO {
    
    private Integer empleosActivos;
    private Integer empleosFinalizados;
    private Integer totalPostulaciones;
    private Integer nuevasPostulaciones;
}