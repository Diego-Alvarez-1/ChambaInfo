
package com.chambainfo.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDTO {
    
    private String token;
    private String type = "Bearer";
    private Long id;
    private String dni;
    private String nombreCompleto;
    private String usuario;
    private String celular;
    private String mensaje;
}