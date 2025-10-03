
package com.chambainfo.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 8)
    @NotBlank(message = "El DNI es obligatorio")
    private String dni;
    
    @Column(nullable = false)
    @NotBlank(message = "El nombre completo es obligatorio")
    private String nombreCompleto;
    
    @Column(unique = true, nullable = false)
    @NotBlank(message = "El usuario es obligatorio")
    private String usuario;
    
    @Column(nullable = false)
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
    
    @Column(length = 15)
    private String celular;
    
    @Column(columnDefinition = "TEXT")
    private String habilidades;
    
    @Column(columnDefinition = "TEXT")
    private String experienciaLaboral;
    
    @Column(name = "foto_dni_anverso")
    private String fotoDniAnverso;
    
    @Column(name = "foto_dni_reverso")
    private String fotoDniReverso;
    
    @Column(name = "certificado_laboral")
    private String certificadoLaboral;
    
    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;
    
    @Column(name = "ultima_actualizacion")
    private LocalDateTime ultimaActualizacion;
    
    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
        ultimaActualizacion = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        ultimaActualizacion = LocalDateTime.now();
    }
}