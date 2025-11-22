package com.chambainfo.app.data.model

data class Usuario(
    val id: Long,
    val dni: String,
    val nombreCompleto: String,
    val usuario: String,
    val celular: String,
    val email: String?,
    val habilidades: String?,
    val experienciaLaboral: String?
)