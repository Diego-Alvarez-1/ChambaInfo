package com.chambainfo.app.data.model

import java.util.*

data class Notificacion(
    val id: Long,
    val tipo: TipoNotificacion,
    val titulo: String,
    val mensaje: String,
    val fecha: Date,
    val leida: Boolean = false,
    val empleoId: Long? = null
)

enum class TipoNotificacion {
    NUEVA_POSTULACION,
    POSTULACION_ENVIADA,
    EMPLEO_PUBLICADO
}