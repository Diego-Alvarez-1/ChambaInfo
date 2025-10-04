package com.chambainfo.backend.service;

import com.chambainfo.backend.dto.*;
import com.chambainfo.backend.entity.Usuario;
import com.chambainfo.backend.exception.ReniecException;
import com.chambainfo.backend.exception.UserAlreadyExistsException;
import com.chambainfo.backend.repository.UsuarioRepository;
import com.chambainfo.backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    
    private final UsuarioRepository usuarioRepository;
    private final ReniecService reniecService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    
    @Override
    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        
        // Validar que las contraseñas coincidan
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }
        
        // Verificar si el usuario ya existe
        if (usuarioRepository.existsByUsuario(request.getUsuario())) {
            throw new UserAlreadyExistsException("El usuario ya está registrado");
        }
        
        // Verificar si el DNI ya existe
        if (usuarioRepository.existsByDni(request.getDni())) {
            throw new UserAlreadyExistsException("El DNI ya está registrado");
        }
        
        // Consultar RENIEC para obtener los datos completos
        ReniecResponseDTO reniecData;
        try {
            reniecData = reniecService.consultarDni(request.getDni());
            log.info("✅ Datos obtenidos de RENIEC:");
            log.info("   - DNI: {}", reniecData.getDocumentNumber());
            log.info("   - Nombres: {}", reniecData.getFirstName());
            log.info("   - Apellido Paterno: {}", reniecData.getFirstLastName());
            log.info("   - Apellido Materno: {}", reniecData.getSecondLastName());
            log.info("   - Nombre Completo: {}", reniecData.getFullName());
        } catch (ReniecException e) {
            log.error("❌ Error al consultar RENIEC: {}", e.getMessage());
            throw new ReniecException("No se pudo validar el DNI. Verifique que sea correcto.");
        }
        
        // Crear el usuario con TODOS los datos separados de RENIEC
        Usuario usuario = new Usuario();
        usuario.setDni(request.getDni());
        
        // ===== GUARDAR DATOS SEPARADOS DE RENIEC =====
        usuario.setNombres(reniecData.getFirstName());
        usuario.setApellidoPaterno(reniecData.getFirstLastName());
        usuario.setApellidoMaterno(reniecData.getSecondLastName());
        usuario.setNombreCompleto(reniecData.getFullName());
        // ============================================
        
        // Datos de la cuenta del usuario
        usuario.setUsuario(request.getUsuario());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        
        // Guardar el usuario en la base de datos
        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        
        log.info("✅ Usuario registrado exitosamente en BD:");
        log.info("   - ID: {}", usuarioGuardado.getId());
        log.info("   - DNI: {}", usuarioGuardado.getDni());
        log.info("   - Nombres: {}", usuarioGuardado.getNombres());
        log.info("   - Apellido Paterno: {}", usuarioGuardado.getApellidoPaterno());
        log.info("   - Apellido Materno: {}", usuarioGuardado.getApellidoMaterno());
        log.info("   - Nombre Completo: {}", usuarioGuardado.getNombreCompleto());
        log.info("   - Usuario: {}", usuarioGuardado.getUsuario());
        
        // Generar token JWT
        String token = jwtTokenProvider.generateToken(usuarioGuardado.getUsuario());
        
        return AuthResponseDTO.builder()
                .token(token)
                .type("Bearer")
                .id(usuarioGuardado.getId())
                .dni(usuarioGuardado.getDni())
                .nombreCompleto(usuarioGuardado.getNombreCompleto())
                .usuario(usuarioGuardado.getUsuario())
                .mensaje("Cuenta creada exitosamente")
                .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginRequestDTO request) {
        
        // Buscar usuario
        Usuario usuario = usuarioRepository.findByUsuario(request.getUsuario())
                .orElseThrow(() -> new BadCredentialsException("Usuario o contraseña incorrectos"));
        
        // Verificar contraseña
        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new BadCredentialsException("Usuario o contraseña incorrectos");
        }
        
        // Generar token JWT
        String token = jwtTokenProvider.generateToken(usuario.getUsuario());
        
        log.info("✅ Usuario autenticado: {} - {}", usuario.getUsuario(), usuario.getNombreCompleto());
        
        return AuthResponseDTO.builder()
                .token(token)
                .type("Bearer")
                .id(usuario.getId())
                .dni(usuario.getDni())
                .nombreCompleto(usuario.getNombreCompleto())
                .usuario(usuario.getUsuario())
                .celular(usuario.getCelular())
                .mensaje("Inicio de sesión exitoso")
                .build();
    }
}