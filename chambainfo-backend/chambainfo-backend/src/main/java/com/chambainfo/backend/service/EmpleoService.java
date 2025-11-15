package com.chambainfo.backend.service;

import com.chambainfo.backend.dto.EmpleoResponseDTO;
import com.chambainfo.backend.dto.PublicarEmpleoRequestDTO;
import java.util.List;

public interface EmpleoService {
    /**
     * Publica un nuevo empleo en el sistema.
     *
     * @param request Los datos del empleo a publicar.
     * @param usuarioAutenticado El nombre de usuario del empleador autenticado.
     * @return Los datos del empleo publicado.
     */
    EmpleoResponseDTO publicarEmpleo(PublicarEmpleoRequestDTO request, String usuarioAutenticado);
    
    /**
     * Obtiene todos los empleos activos disponibles en el sistema.
     *
     * @return Una lista con todos los empleos activos ordenados por fecha de publicación.
     */
    List<EmpleoResponseDTO> obtenerTodosLosEmpleos();
    
    /**
     * Obtiene todos los empleos publicados por un empleador específico.
     *
     * @param empleadorId El ID del empleador.
     * @return Una lista con los empleos del empleador ordenados por fecha de publicación.
     */
    List<EmpleoResponseDTO> obtenerEmpleosPorEmpleador(Long empleadorId);
    
    /**
     * Obtiene los detalles de un empleo específico por su ID.
     *
     * @param id El ID del empleo a obtener.
     * @return Los detalles del empleo.
     * @throws RuntimeException Si el empleo no se encuentra.
     */
    EmpleoResponseDTO obtenerEmpleoPorId(Long id);
    
    /**
     * Desactiva un empleo publicado (solo el empleador puede desactivar sus propios empleos).
     *
     * @param id El ID del empleo a desactivar.
     * @param usuarioAutenticado El nombre de usuario del empleador autenticado.
     * @throws RuntimeException Si el empleo no se encuentra o el usuario no tiene permisos.
     */
    void desactivarEmpleo(Long id, String usuarioAutenticado);


}