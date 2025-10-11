package com.chambainfo.backend.controller;

import com.chambainfo.backend.dto.EmpleoResponseDTO;
import com.chambainfo.backend.dto.PublicarEmpleoRequestDTO;
import com.chambainfo.backend.service.EmpleoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/empleos")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class EmpleoController {

    private final EmpleoService empleoService;

    @PostMapping("/publicar")
    public ResponseEntity<EmpleoResponseDTO> publicarEmpleo(
            @Valid @RequestBody PublicarEmpleoRequestDTO request,
            Authentication authentication) {

        log.info("Solicitud para publicar empleo: {}", request.getNombreEmpleo());

        String usuarioAutenticado = authentication.getName();
        EmpleoResponseDTO response = empleoService.publicarEmpleo(request, usuarioAutenticado);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<EmpleoResponseDTO>> obtenerTodosLosEmpleos() {
        log.info("Solicitud para obtener todos los empleos");

        List<EmpleoResponseDTO> empleos = empleoService.obtenerTodosLosEmpleos();

        return ResponseEntity.ok(empleos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmpleoResponseDTO> obtenerEmpleoPorId(@PathVariable Long id) {
        log.info("Solicitud para obtener empleo ID: {}", id);

        EmpleoResponseDTO empleo = empleoService.obtenerEmpleoPorId(id);

        return ResponseEntity.ok(empleo);
    }

    @GetMapping("/empleador/{empleadorId}")
    public ResponseEntity<List<EmpleoResponseDTO>> obtenerEmpleosPorEmpleador(
            @PathVariable Long empleadorId) {

        log.info("Solicitud para obtener empleos del empleador ID: {}", empleadorId);

        List<EmpleoResponseDTO> empleos = empleoService.obtenerEmpleosPorEmpleador(empleadorId);

        return ResponseEntity.ok(empleos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> desactivarEmpleo(
            @PathVariable Long id,
            Authentication authentication) {

        log.info("🗑Solicitud para desactivar empleo ID: {}", id);

        String usuarioAutenticado = authentication.getName();
        empleoService.desactivarEmpleo(id, usuarioAutenticado);

        return ResponseEntity.ok("Empleo desactivado exitosamente");
    }
}