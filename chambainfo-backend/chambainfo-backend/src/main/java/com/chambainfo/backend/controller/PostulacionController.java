package com.chambainfo.backend.controller;

import com.chambainfo.backend.dto.PostulacionRequest;
import com.chambainfo.backend.dto.PostulacionResponse;
import com.chambainfo.backend.service.PostulacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/postulaciones")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PostulacionController {

    private final PostulacionService postulacionService;

    /**
     * Envía una postulación para un empleo específico.
     *
     * @param request Los datos de la postulación.
     * @param authentication La información de autenticación del usuario.
     * @return Una respuesta con los datos de la postulación creada.
     */
    @PostMapping
    public ResponseEntity<PostulacionResponse> postular(
            @Valid @RequestBody PostulacionRequest request,
            Authentication authentication) {

        log.info("Solicitud de postulación recibida");

        String usuarioAutenticado = authentication.getName();
        PostulacionResponse response = postulacionService.postular(request, usuarioAutenticado);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtiene todas las postulaciones de un empleo específico.
     *
     * @param empleoId El ID del empleo.
     * @return Una respuesta con la lista de postulaciones del empleo.
     */
    @GetMapping("/empleo/{empleoId}")
    public ResponseEntity<List<PostulacionResponse>> obtenerPostulacionesPorEmpleo(
            @PathVariable Long empleoId) {

        log.info("Obteniendo postulaciones para empleo ID: {}", empleoId);

        List<PostulacionResponse> postulaciones =
                postulacionService.obtenerPostulacionesPorEmpleo(empleoId);

        return ResponseEntity.ok(postulaciones);
    }

    /**
     * Obtiene todas las postulaciones del usuario autenticado.
     *
     * @param authentication La información de autenticación del usuario.
     * @return Una respuesta con la lista de postulaciones del usuario.
     */
    @GetMapping("/mis-postulaciones")
    public ResponseEntity<List<PostulacionResponse>> obtenerMisPostulaciones(
            Authentication authentication) {

        String usuarioAutenticado = authentication.getName();
        List<PostulacionResponse> postulaciones =
                postulacionService.obtenerMisPostulaciones(usuarioAutenticado);

        return ResponseEntity.ok(postulaciones);
    }

    /**
     * Verifica si el usuario ya postuló a un empleo específico.
     *
     * @param empleoId El ID del empleo a verificar.
     * @param authentication La información de autenticación del usuario.
     * @return Una respuesta con true si ya postuló, false en caso contrario.
     */
    @GetMapping("/ya-postulo/{empleoId}")
    public ResponseEntity<Boolean> yaPostulo(
            @PathVariable Long empleoId,
            Authentication authentication) {

        String usuarioAutenticado = authentication.getName();
        boolean yaPostulo = postulacionService.yaPostulo(empleoId, usuarioAutenticado);

        return ResponseEntity.ok(yaPostulo);
    }

    /**
     * Actualiza el estado de una postulación.
     *
     * @param id El ID de la postulación.
     * @param body El cuerpo de la petición que contiene el nuevo estado.
     * @return Una respuesta con los datos actualizados de la postulación.
     */
    @PutMapping("/{id}/estado")
    public ResponseEntity<PostulacionResponse> actualizarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        log.info("Actualizando estado de postulación ID: {}", id);

        String nuevoEstado = body.get("estado");
        PostulacionResponse response = postulacionService.actualizarEstado(id, nuevoEstado);

        return ResponseEntity.ok(response);
    }
}
