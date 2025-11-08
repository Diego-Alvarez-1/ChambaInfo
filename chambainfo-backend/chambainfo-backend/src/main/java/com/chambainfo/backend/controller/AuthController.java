
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

    /**
     * Verifica un DNI consultando la base de datos de RENIEC.
     *
     * @param dni El número de DNI a verificar (8 dígitos).
     * @return Una respuesta con los datos del DNI obtenidos de RENIEC o un error si no se encuentra.
     */
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
    
    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param request Los datos de registro del usuario.
     * @return Una respuesta con los datos de autenticación del usuario registrado.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        log.info("Solicitud de registro recibida para usuario: {}", request.getUsuario());
        AuthResponseDTO response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Inicia sesión con las credenciales del usuario.
     *
     * @param request Los datos de login (usuario y contraseña).
     * @return Una respuesta con los datos de autenticación del usuario.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        log.info("Solicitud de login recibida para usuario: {}", request.getUsuario());
        AuthResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint de prueba para verificar que la API está funcionando correctamente.
     *
     * @return Una respuesta con un mensaje de confirmación.
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("API funcionando correctamente ✅");
    }
}