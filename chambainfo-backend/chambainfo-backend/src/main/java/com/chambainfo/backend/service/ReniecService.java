package com.chambainfo.backend.service;

import com.chambainfo.backend.dto.ReniecResponseDTO;

public interface ReniecService {
    /**
     * Consulta un DNI en la base de datos de RENIEC.
     *
     * @param dni El número de DNI a consultar (8 dígitos).
     * @return Los datos del DNI obtenidos de RENIEC.
     * @throws ReniecException Si no se puede obtener información del DNI.
     */
    ReniecResponseDTO consultarDni(String dni);
}