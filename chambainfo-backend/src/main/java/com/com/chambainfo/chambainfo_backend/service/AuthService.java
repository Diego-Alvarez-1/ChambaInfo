package com.chambainfo.service;

import com.chambainfo.dto.*;
import com.chambainfo.model.Usuario;
import com.chambainfo.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ReniecService reniecService;
    private final JwtService jwtService;
    
    public AuthService(UsuarioRepository usuarioRepository, 
                      PasswordEncoder passwordEncoder,
                      ReniecService reniecService,
                      JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.reniecService = reniecService;
        this.jwtService = jwtService;
    }
    
    @Transactional
    public AuthResponse registrar(RegisterRequest request) {
        // Validar que las contraseñas coincidan
        if (!request.getPassword().equals(request.getConfirmarPassword())) {
            throw new RuntimeException("Las contraseñas no coinciden");
        }
        
        // Verificar si el usuario ya existe
        if (usuarioRepository.existsByDni(request.getDni())) {
            throw new RuntimeException("El DNI ya está registrado");
        }
        
        if (request.getCelular() != null && usuarioRepository.existsByCelular(request.getCelular())) {
            throw new RuntimeException("El celular ya está registrado");
        }
        
        if (request.getEmail() != null && usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }
        
        // Consultar RENIEC
        ReniecResponse reniecData = reniecService.consultarDni(request.getDni());
        
        // Crear usuario
        Usuario usuario = new Usuario();
        usuario.setDni(request.getDni());
        usuario.setNombres(reniecData.getNombres());
        usuario.setApellidos(reniecData.getApellidoPaterno() + " " + reniecData.getApellidoMaterno());
        usuario.setCelular(request.getCelular());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setTipoUsuario(request.getTipoUsuario());
        
        usuario = usuarioRepository.save(usuario);
        
        String token = jwtService.generateToken(usuario);
        
        return new AuthResponse(
            usuario.getId(),
            usuario.getDni(),
            usuario.getNombres(),
            usuario.getApellidos(),
            usuario.getCelular(),
            usuario.getEmail(),
            usuario.getTipoUsuario(),
            token,
            "Registro exitoso"
        );
    }
    
    public AuthResponse login(LoginRequest request) {
        // Buscar usuario por DNI, celular o email
        Usuario usuario = usuarioRepository.findByDni(request.getIdentificacion())
            .or(() -> usuarioRepository.findByCelular(request.getIdentificacion()))
            .or(() -> usuarioRepository.findByEmail(request.getIdentificacion()))
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Verificar contraseña
        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new RuntimeException("Contraseña incorrecta");
        }
        
        String token = jwtService.generateToken(usuario);
        
        return new AuthResponse(
            usuario.getId(),
            usuario.getDni(),
            usuario.getNombres(),
            usuario.getApellidos(),
            usuario.getCelular(),
            usuario.getEmail(),
            usuario.getTipoUsuario(),
            token,
            "Login exitoso"
        );
    }
}