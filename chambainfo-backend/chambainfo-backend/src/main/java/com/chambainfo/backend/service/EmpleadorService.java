package com.chambainfo.backend.service;

import com.chambainfo.backend.dto.EmpleoConPostulacionesDTO;
import com.chambainfo.backend.dto.EstadisticasEmpleadorDTO;
import com.chambainfo.backend.dto.PostulanteDTO;
import java.util.List;

public interface EmpleadorService {
    
    /**
     * Obtiene las estadísticas generales del empleador.
     */
    EstadisticasEmpleadorDTO obtenerEstadisticas(String usuarioAutenticado);
    
    /**
     * Obtiene todos los empleos del empleador con sus postulaciones.
     */
    List<EmpleoConPostulacionesDTO> obtenerMisEmpleosConPostulaciones(String usuarioAutenticado);
    
    /**
     * Obtiene los postulantes de un empleo específico.
     */
    List<PostulanteDTO> obtenerPostulantesDeEmpleo(Long empleoId, String usuarioAutenticado);
    
    /**
     * Marca una postulación como vista.
     */
    void marcarPostulacionComoVista(Long postulacionId, String usuarioAutenticado);
    
    
    
    /**
     * Cambia el estado de una postulación.
     */
    void cambiarEstadoPostulacion(Long postulacionId, String nuevoEstado, String usuarioAutenticado);
    
    /**
     * Finaliza un empleo.
     */
    void finalizarEmpleo(Long empleoId, String usuarioAutenticado);
}