package com.chambainfo.controller;

import com.chambainfo.dto.*;
import com.chambainfo.service.AuthService;
import com.chambainfo.service.ReniecService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final AuthService authService;
    private final ReniecService reniecService;
    
    public AuthController(AuthService authService, ReniecService reniecService) {
        this.authService = authService;
        this.reniecService = reniecService;
    }
    
    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.registrar(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }
    
    @GetMapping("/verificar-dni/{dni}")
    public ResponseEntity<?> verificarDni(@PathVariable String dni) {
        try {
            ReniecResponse response = reniecService.consultarDni(dni);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "DNI no encontrado o error en RENIEC");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}