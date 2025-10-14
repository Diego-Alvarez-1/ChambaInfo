package com.chambainfo.app.data.model

data class AuthResponse(
    val token: String,
    val type: String,
    val id: Long,
    val dni: String,
    val nombreCompleto: String,
    val usuario: String,
    val celular: String,
    val mensaje: String
)