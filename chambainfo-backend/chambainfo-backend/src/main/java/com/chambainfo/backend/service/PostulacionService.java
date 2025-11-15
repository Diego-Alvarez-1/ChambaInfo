package com.chambainfo.backend.service;

import com.chambainfo.backend.dto.PostulacionRequest;
import com.chambainfo.backend.dto.PostulacionResponse;
import java.util.List;

public interface PostulacionService {
    /**
     * Envía una postulación para un empleo específico.
     *
     * @param request Los datos de la postulación.
     * @param usuarioAutenticado El nombre de usuario del trabajador autenticado.
     * @return Los datos de la postulación creada.
     */
    PostulacionResponse postular(PostulacionRequest request, String usuarioAutenticado);
    
    /**
     * Obtiene todas las postulaciones de un empleo específico.
     *
     * @param empleoId El ID del empleo.
     * @return Una lista con las postulaciones del empleo ordenadas por fecha descendente.
     */
    List<PostulacionResponse> obtenerPostulacionesPorEmpleo(Long empleoId);
    
    /**
     * Obtiene todas las postulaciones del usuario autenticado.
     *
     * @param usuarioAutenticado El nombre de usuario del trabajador autenticado.
     * @return Una lista con las postulaciones del usuario ordenadas por fecha descendente.
     */
    List<PostulacionResponse> obtenerMisPostulaciones(String usuarioAutenticado);
    
    /**
     * Verifica si el usuario ya postuló a un empleo específico.
     *
     * @param empleoId El ID del empleo a verificar.
     * @param usuarioAutenticado El nombre de usuario del trabajador autenticado.
     * @return true si ya postuló, false en caso contrario.
     */
    boolean yaPostulo(Long empleoId, String usuarioAutenticado);
}