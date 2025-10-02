package com.chambainfo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Data
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 8)
    private String dni;
    
    @Column(nullable = false)
    private String nombres;
    
    @Column(nullable = false)
    private String apellidos;
    
    @Column(unique = true)
    private String celular;
    
    @Column(unique = true)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoUsuario tipoUsuario;
    
    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;
    
    @Column(name = "habilidades", length = 1000)
    private String habilidades;
    
    @Column(name = "experiencia_laboral", length = 2000)
    private String experienciaLaboral;
    
    @Column(name = "foto_dni_anverso")
    private String fotoDniAnverso;
    
    @Column(name = "foto_dni_reverso")
    private String fotoDniReverso;
    
    @Column(name = "certificado_laboral")
    private String certificadoLaboral;
    
    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
    }
}