package com.chambainfo.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "postulaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Postulacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleo_id", nullable = false)
    private Empleo empleo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trabajador_id", nullable = false)
    private Usuario trabajador;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Column(name = "estado")
    private String estado = "PENDIENTE";

    @Column(name = "fecha_postulacion")
    private LocalDateTime fechaPostulacion;

    @PrePersist
    protected void onCreate() {
        fechaPostulacion = LocalDateTime.now();
    }
}