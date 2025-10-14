package com.chambainfo.app.data.model

data class PublicarEmpleoRequest(
    val nombreEmpleo: String,
    val descripcionEmpleo: String,
    val celularContacto: String,
    val mostrarNumero: Boolean = true,
    val ubicacion: String? = null,
    val salario: String? = null,
    val ruc: String? = null,
    val adjuntos: String? = null
)