package com.chambainfo.app.data.model

data class EmpleadorEstadisticas(
    val empleosActivos: Int,
    val empleosFinalizados: Int,
    val totalPostulaciones: Int,
    val nuevasPostulaciones: Int
)