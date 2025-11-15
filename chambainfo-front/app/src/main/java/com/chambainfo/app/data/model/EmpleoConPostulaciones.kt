package com.chambainfo.app.data.model

data class EmpleoConPostulaciones(
    val id: Long,
    val nombreEmpleo: String,
    val descripcionEmpleo: String,
    val cantidadPostulaciones: Int,
    val nuevasPostulaciones: Int,
    val fechaPublicacion: String,
    val activo: Boolean,
    val diasRestantes: Int
)