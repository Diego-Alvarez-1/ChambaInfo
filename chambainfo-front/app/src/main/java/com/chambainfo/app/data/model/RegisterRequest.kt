package com.chambainfo.app.data.model

data class RegisterRequest(
    val dni: String,
    val usuario: String,
    val password: String,
    val confirmPassword: String,
    val celular: String,
    val rol: String // NUEVO
)