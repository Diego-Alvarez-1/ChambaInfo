package com.chambainfo.backend.controller;

import com.chambainfo.backend.dto.EmpleoConPostulacionesDTO;
import com.chambainfo.backend.dto.EstadisticasEmpleadorDTO;
import com.chambainfo.backend.dto.PostulanteDTO;
import com.chambainfo.backend.service.EmpleadorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para las funcionalidades del empleador.
 * Permite gestionar empleos publicados y ver postulantes.
 */
@RestController
@RequestMapping("/empleador")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class EmpleadorController {

    private final EmpleadorService empleadorService;

    /**
     * Obtiene las estadísticas generales del empleador.
     *
     * @param authentication La información de autenticación del usuario.
     * @return Las estadísticas del empleador (empleos activos, finalizados, postulaciones).
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<EstadisticasEmpleadorDTO> obtenerEstadisticas(
            Authentication authentication) {

        log.info("Obteniendo estadísticas del empleador");

        String usuarioAutenticado = authentication.getName();
        EstadisticasEmpleadorDTO estadisticas = 
                empleadorService.obtenerEstadisticas(usuarioAutenticado);

        return ResponseEntity.ok(estadisticas);
    }

    /**
     * Obtiene todos los empleos del empleador con sus postulaciones.
     *
     * @param authentication La información de autenticación del usuario.
     * @return Lista de empleos con cantidad de postulaciones.
     */
    @GetMapping("/mis-empleos")
    public ResponseEntity<List<EmpleoConPostulacionesDTO>> obtenerMisEmpleos(
            Authentication authentication) {

        log.info("Obteniendo empleos del empleador");

        String usuarioAutenticado = authentication.getName();
        List<EmpleoConPostulacionesDTO> empleos = 
                empleadorService.obtenerMisEmpleosConPostulaciones(usuarioAutenticado);

        return ResponseEntity.ok(empleos);
    }

    /**
     * Obtiene los postulantes de un empleo específico.
     *
     * @param empleoId El ID del empleo.
     * @param authentication La información de autenticación del usuario.
     * @return Lista de postulantes del empleo.
     */
    @GetMapping("/empleo/{empleoId}/postulantes")
    public ResponseEntity<List<PostulanteDTO>> obtenerPostulantes(
            @PathVariable Long empleoId,
            Authentication authentication) {

        log.info("Obteniendo postulantes del empleo ID: {}", empleoId);

        String usuarioAutenticado = authentication.getName();
        List<PostulanteDTO> postulantes = 
                empleadorService.obtenerPostulantesDeEmpleo(empleoId, usuarioAutenticado);

        return ResponseEntity.ok(postulantes);
    }

    /**
     * Marca una postulación como vista (no nueva).
     *
     * @param postulacionId El ID de la postulación.
     * @param authentication La información de autenticación del usuario.
     * @return Mensaje de confirmación.
     */
    @PutMapping("/postulacion/{postulacionId}/marcar-vista")
    public ResponseEntity<Map<String, String>> marcarComoVista(
            @PathVariable Long postulacionId,
            Authentication authentication) {

        log.info("Marcando postulación como vista: {}", postulacionId);

        String usuarioAutenticado = authentication.getName();
        empleadorService.marcarPostulacionComoVista(postulacionId, usuarioAutenticado);

        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Postulación marcada como vista");

        return ResponseEntity.ok(response);
    }

    

    /**
     * Cambia el estado de una postulación.
     *
     * @param postulacionId El ID de la postulación.
     * @param nuevoEstado El nuevo estado (PENDIENTE, CONTACTADO, RECHAZADO).
     * @param authentication La información de autenticación del usuario.
     * @return Mensaje de confirmación.
     */
    @PutMapping("/postulacion/{postulacionId}/estado")
    public ResponseEntity<Map<String, String>> cambiarEstadoPostulacion(
            @PathVariable Long postulacionId,
            @RequestParam String nuevoEstado,
            Authentication authentication) {

        log.info("Cambiando estado de postulación {} a {}", postulacionId, nuevoEstado);

        String usuarioAutenticado = authentication.getName();
        empleadorService.cambiarEstadoPostulacion(postulacionId, nuevoEstado, usuarioAutenticado);

        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Estado actualizado exitosamente");
        response.put("nuevoEstado", nuevoEstado);

        return ResponseEntity.ok(response);
    }

    /**
     * Finaliza un empleo (lo marca como inactivo y no permite más postulaciones).
     *
     * @param empleoId El ID del empleo a finalizar.
     * @param authentication La información de autenticación del usuario.
     * @return Mensaje de confirmación.
     */
    @PutMapping("/empleo/{empleoId}/finalizar")
    public ResponseEntity<Map<String, String>> finalizarEmpleo(
            @PathVariable Long empleoId,
            Authentication authentication) {

        log.info("Finalizando empleo ID: {}", empleoId);

        String usuarioAutenticado = authentication.getName();
        empleadorService.finalizarEmpleo(empleoId, usuarioAutenticado);

        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Empleo finalizado exitosamente");

        return ResponseEntity.ok(response);
    }
}