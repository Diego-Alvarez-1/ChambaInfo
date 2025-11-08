package com.chambainfo.app.data.model

data class Empleo(
    val id: Long,
    val nombreEmpleo: String,
    val descripcionEmpleo: String,
    val celularContacto: String,
    val mostrarNumero: Boolean,
    val ubicacion: String?,
    val salario: String?,
    val ruc: String?,
    val adjuntos: String?,
    val empleadorId: Long,
    val empleadorNombre: String,
    val empleadorUsuario: String,
    val fechaPublicacion: String,
    val ultimaActualizacion: String,
    val activo: Boolean
)
