package com.chambainfo.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "empleos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Empleo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "nombre_empleo")
    private String nombreEmpleo;

    @Column(nullable = false, columnDefinition = "TEXT", name = "descripcion_empleo")
    private String descripcionEmpleo;

    @Column(nullable = false, name = "celular_contacto", length = 15)
    private String celularContacto;

    @Column(name = "mostrar_numero")
    private Boolean mostrarNumero = true;

    // Campos opcionales
    @Column(name = "ubicacion")
    private String ubicacion;

    @Column(name = "salario")
    private String salario;

    @Column(name = "ruc")
    private String ruc;

    @Column(name = "adjuntos")
    private String adjuntos; // URLs de archivos separados por comas

    // Relación con el empleador (Usuario que publicó)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleador_id", nullable = false)
    private Usuario empleador;

    @Column(name = "fecha_publicacion")
    private LocalDateTime fechaPublicacion;

    @Column(name = "ultima_actualizacion")
    private LocalDateTime ultimaActualizacion;

    @Column(name = "activo")
    private Boolean activo = true;

    @PrePersist
    protected void onCreate() {
        fechaPublicacion = LocalDateTime.now();
        ultimaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        ultimaActualizacion = LocalDateTime.now();
    }
}