package com.chambainfo.app.data.model

data class Postulante(
    val postulacionId: Long,
    val trabajadorId: Long,
    val nombreCompleto: String,
    val celular: String,
    val mensaje: String,
    val estado: String,
    val fechaPostulacion: String,
    val tiempoTranscurrido: String,
    val esNuevo: Boolean
)