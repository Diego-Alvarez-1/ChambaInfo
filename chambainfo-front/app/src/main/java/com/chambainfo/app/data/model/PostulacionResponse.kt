package com.chambainfo.app.data.model

data class PostulacionResponse(
    val id: Long,
    val empleoId: Long,
    val nombreEmpleo: String,
    val trabajadorId: Long,
    val trabajadorNombre: String,
    val trabajadorDni: String,
    val trabajadorCelular: String,
    val mensaje: String,
    val estado: String,
    val fechaPostulacion: String
)