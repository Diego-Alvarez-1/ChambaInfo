package com.chambainfo.app.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.chambainfo.app.data.model.Notificacion
import com.chambainfo.app.data.model.TipoNotificacion
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.*

private val Context.notificacionDataStore: DataStore<Preferences> by preferencesDataStore(name = "notificaciones")

class NotificacionManager(private val context: Context) {

    companion object {
        private val gson = Gson()

        // Función para generar la key única por usuario
        private fun getNotificacionesKey(userId: Long): Preferences.Key<String> {
            return stringPreferencesKey("notificaciones_user_$userId")
        }
    }

    /**
     * Agrega una nueva notificación para un usuario específico.
     */
    suspend fun agregarNotificacion(
        userId: Long,
        tipo: TipoNotificacion,
        titulo: String,
        mensaje: String,
        empleoId: Long? = null
    ) {
        context.notificacionDataStore.edit { prefs ->
            val key = getNotificacionesKey(userId)
            val notificacionesJson = prefs[key] ?: "[]"
            val notificaciones = gson.fromJson<MutableList<Notificacion>>(
                notificacionesJson,
                object : TypeToken<MutableList<Notificacion>>() {}.type
            ) ?: mutableListOf()

            val nuevaNotificacion = Notificacion(
                id = System.currentTimeMillis(),
                tipo = tipo,
                titulo = titulo,
                mensaje = mensaje,
                fecha = Date(),
                leida = false,
                empleoId = empleoId
            )

            notificaciones.add(0, nuevaNotificacion) // Agregar al inicio

            // Mantener solo las últimas 50 notificaciones
            if (notificaciones.size > 50) {
                notificaciones.removeAt(notificaciones.size - 1)
            }

            prefs[key] = gson.toJson(notificaciones)
        }
    }

    /**
     * Obtiene todas las notificaciones de un usuario específico.
     */
    fun obtenerNotificaciones(userId: Long): Flow<List<Notificacion>> {
        return context.notificacionDataStore.data.map { prefs ->
            val key = getNotificacionesKey(userId)
            val notificacionesJson = prefs[key] ?: "[]"
            gson.fromJson(
                notificacionesJson,
                object : TypeToken<List<Notificacion>>() {}.type
            ) ?: emptyList()
        }
    }

    /**
     * Obtiene el contador de notificaciones no leídas de un usuario.
     */
    fun contarNoLeidas(userId: Long): Flow<Int> {
        return obtenerNotificaciones(userId).map { notificaciones ->
            notificaciones.count { !it.leida }
        }
    }

    /**
     * Marca una notificación como leída para un usuario específico.
     */
    suspend fun marcarComoLeida(userId: Long, notificacionId: Long) {
        context.notificacionDataStore.edit { prefs ->
            val key = getNotificacionesKey(userId)
            val notificacionesJson = prefs[key] ?: "[]"
            val notificaciones = gson.fromJson<MutableList<Notificacion>>(
                notificacionesJson,
                object : TypeToken<MutableList<Notificacion>>() {}.type
            ) ?: mutableListOf()

            val index = notificaciones.indexOfFirst { it.id == notificacionId }
            if (index != -1) {
                notificaciones[index] = notificaciones[index].copy(leida = true)
                prefs[key] = gson.toJson(notificaciones)
            }
        }
    }

    /**
     * Marca todas las notificaciones como leídas para un usuario específico.
     */
    suspend fun marcarTodasComoLeidas(userId: Long) {
        context.notificacionDataStore.edit { prefs ->
            val key = getNotificacionesKey(userId)
            val notificacionesJson = prefs[key] ?: "[]"
            val notificaciones = gson.fromJson<MutableList<Notificacion>>(
                notificacionesJson,
                object : TypeToken<MutableList<Notificacion>>() {}.type
            ) ?: mutableListOf()

            val notificacionesLeidas = notificaciones.map { it.copy(leida = true) }
            prefs[key] = gson.toJson(notificacionesLeidas)
        }
    }

    /**
     * Limpia todas las notificaciones de un usuario específico.
     */
    suspend fun limpiarNotificaciones(userId: Long) {
        context.notificacionDataStore.edit { prefs ->
            val key = getNotificacionesKey(userId)
            prefs[key] = "[]"
        }
    }
}