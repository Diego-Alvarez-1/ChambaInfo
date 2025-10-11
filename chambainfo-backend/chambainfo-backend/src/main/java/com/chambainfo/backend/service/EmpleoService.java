package com.chambainfo.backend.service;

import com.chambainfo.backend.dto.EmpleoResponseDTO;
import com.chambainfo.backend.dto.PublicarEmpleoRequestDTO;
import java.util.List;

public interface EmpleoService {
    EmpleoResponseDTO publicarEmpleo(PublicarEmpleoRequestDTO request, String usuarioAutenticado);
    List<EmpleoResponseDTO> obtenerTodosLosEmpleos();
    List<EmpleoResponseDTO> obtenerEmpleosPorEmpleador(Long empleadorId);
    EmpleoResponseDTO obtenerEmpleoPorId(Long id);
    void desactivarEmpleo(Long id, String usuarioAutenticado);
}