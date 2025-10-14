
package com.chambainfo.backend.controller;

import com.chambainfo.backend.dto.AuthResponseDTO;
import com.chambainfo.backend.dto.LoginRequestDTO;
import com.chambainfo.backend.dto.RegisterRequestDTO;
import com.chambainfo.backend.dto.ReniecResponseDTO;
import com.chambainfo.backend.service.AuthService;
import com.chambainfo.backend.service.ReniecService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final AuthService authService;
    private final ReniecService reniecService;

    @GetMapping("/verificar-dni/{dni}")
    public ResponseEntity<ReniecResponseDTO> verificarDni(@PathVariable String dni) {
        log.info("Solicitud de verificación de DNI: {}", dni);

        // Validar formato del DNI
        if (dni == null || !dni.matches("^[0-9]{8}$")) {
            return ResponseEntity.badRequest().build();
        }

        try {
            ReniecResponseDTO reniecData = reniecService.consultarDni(dni);
            return ResponseEntity.ok(reniecData);
        } catch (Exception e) {
            log.error("Error al verificar DNI: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        log.info("Solicitud de registro recibida para usuario: {}", request.getUsuario());
        AuthResponseDTO response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        log.info("Solicitud de login recibida para usuario: {}", request.getUsuario());
        AuthResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("API funcionando correctamente ✅");
    }
}