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
    
    /**
     * Registra un nuevo usuario en el sistema.
     * Valida las contraseñas, verifica que el usuario no exista, consulta RENIEC y genera un token JWT.
     *
     * @param request Los datos de registro del usuario.
     * @return Una respuesta con los datos de autenticación del usuario registrado.
     * @throws IllegalArgumentException Si las contraseñas no coinciden.
     * @throws UserAlreadyExistsException Si el usuario o DNI ya está registrado.
     * @throws ReniecException Si no se puede validar el DNI en RENIEC.
     */
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
            log.info(" Datos obtenidos de RENIEC:");
            log.info("   - DNI: {}", reniecData.getDocumentNumber());
            log.info("   - Nombres: {}", reniecData.getFirstName());
            log.info("   - Apellido Paterno: {}", reniecData.getFirstLastName());
            log.info("   - Apellido Materno: {}", reniecData.getSecondLastName());
            log.info("   - Nombre Completo: {}", reniecData.getFullName());
        } catch (ReniecException e) {
            log.error(" Error al consultar RENIEC: {}", e.getMessage());
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
        usuario.setCelular(request.getCelular());
        usuario.setRol(request.getRol()); // NUEVO: guardar rol
        
        log.info(" Celular guardado: {}", request.getCelular());
        log.info(" Rol guardado: {}", request.getRol()); // NUEVO
        // ==========================================
        
        // Guardar el usuario en la base de datos
        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        
        log.info(" Usuario registrado exitosamente en BD:");
        log.info("   - ID: {}", usuarioGuardado.getId());
        log.info("   - Rol: {}", usuarioGuardado.getRol()); // NUEVO
        
        // Generar token JWT
        String token = jwtTokenProvider.generateToken(usuarioGuardado.getUsuario());
        
        return AuthResponseDTO.builder()
                .token(token)
                .type("Bearer")
                .id(usuarioGuardado.getId())
                .dni(usuarioGuardado.getDni())
                .nombreCompleto(usuarioGuardado.getNombreCompleto())
                .usuario(usuarioGuardado.getUsuario())
                .celular(usuarioGuardado.getCelular())
                .rol(usuarioGuardado.getRol()) // NUEVO
                .mensaje("Cuenta creada exitosamente")
                .build();
    }

    /**
     * Inicia sesión con las credenciales del usuario.
     * Permite iniciar sesión con DNI, celular o nombre de usuario.
     *
     * @param request Los datos de login (usuario y contraseña).
     * @return Una respuesta con los datos de autenticación del usuario.
     * @throws BadCredentialsException Si las credenciales son incorrectas.
     */
    @Override
    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginRequestDTO request) {

        String identificador = request.getUsuario();
        log.info("Intento de login con identificador: {}", identificador);

        // Buscar usuario por DNI, Celular o Usuario
        Usuario usuario = null;

        // Intentar buscar por DNI (8 dígitos)
        if (identificador.matches("^[0-9]{8}$")) {
            log.info("   → Buscando por DNI...");
            usuario = usuarioRepository.findByDni(identificador).orElse(null);
        }

        // Si no se encontró, intentar buscar por Celular (9 dígitos)
        if (usuario == null && identificador.matches("^[0-9]{9}$")) {
            log.info("   → Buscando por Celular...");
            usuario = usuarioRepository.findByCelular(identificador).orElse(null);
        }

        // Si no se encontró, buscar por nombre de usuario
        if (usuario == null) {
            log.info("   → Buscando por Usuario...");
            usuario = usuarioRepository.findByUsuario(identificador).orElse(null);
        }

        // Si no se encontro, lanzar error
        if (usuario == null) {
            log.error("Usuario no encontrado con identificador: {}", identificador);
            throw new BadCredentialsException("Usuario o contraseña incorrectos");
        }

        log.info("Usuario encontrado: {}", usuario.getUsuario());

        // Verificar contraseña
        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            log.error("Contraseña incorrecta para usuario: {}", usuario.getUsuario());
            throw new BadCredentialsException("Usuario o contraseña incorrectos");
        }

        // Generar token JWT
        String token = jwtTokenProvider.generateToken(usuario.getUsuario());

        log.info("Login exitoso para: {} - {}", usuario.getUsuario(), usuario.getNombreCompleto());

        return AuthResponseDTO.builder()
                .token(token)
                .type("Bearer")
                .id(usuario.getId())
                .dni(usuario.getDni())
                .nombreCompleto(usuario.getNombreCompleto())
                .usuario(usuario.getUsuario())
                .celular(usuario.getCelular())
                .rol(usuario.getRol()) // NUEVO: añadir rol
                .mensaje("Inicio de sesión exitoso")
                .build();
    }
}
