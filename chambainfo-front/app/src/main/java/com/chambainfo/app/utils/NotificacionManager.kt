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
import kotlinx.coroutines.flow.map
import java.util.*

private val Context.notificacionDataStore: DataStore<Preferences> by preferencesDataStore(name = "notificaciones")

class NotificacionManager(private val context: Context) {

    companion object {
        private val NOTIFICACIONES_KEY = stringPreferencesKey("notificaciones_list")
        private val gson = Gson()
    }

    /**
     * Agrega una nueva notificación.
     */
    suspend fun agregarNotificacion(
        tipo: TipoNotificacion,
        titulo: String,
        mensaje: String,
        empleoId: Long? = null
    ) {
        context.notificacionDataStore.edit { prefs ->
            val notificacionesJson = prefs[NOTIFICACIONES_KEY] ?: "[]"
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

            prefs[NOTIFICACIONES_KEY] = gson.toJson(notificaciones)
        }
    }

    /**
     * Obtiene todas las notificaciones.
     */
    fun obtenerNotificaciones(): Flow<List<Notificacion>> {
        return context.notificacionDataStore.data.map { prefs ->
            val notificacionesJson = prefs[NOTIFICACIONES_KEY] ?: "[]"
            gson.fromJson(
                notificacionesJson,
                object : TypeToken<List<Notificacion>>() {}.type
            ) ?: emptyList()
        }
    }

    /**
     * Obtiene el contador de notificaciones no leídas.
     */
    fun contarNoLeidas(): Flow<Int> {
        return obtenerNotificaciones().map { notificaciones ->
            notificaciones.count { !it.leida }
        }
    }

    /**
     * Marca una notificación como leída.
     */
    suspend fun marcarComoLeida(notificacionId: Long) {
        context.notificacionDataStore.edit { prefs ->
            val notificacionesJson = prefs[NOTIFICACIONES_KEY] ?: "[]"
            val notificaciones = gson.fromJson<MutableList<Notificacion>>(
                notificacionesJson,
                object : TypeToken<MutableList<Notificacion>>() {}.type
            ) ?: mutableListOf()

            val index = notificaciones.indexOfFirst { it.id == notificacionId }
            if (index != -1) {
                notificaciones[index] = notificaciones[index].copy(leida = true)
                prefs[NOTIFICACIONES_KEY] = gson.toJson(notificaciones)
            }
        }
    }

    /**
     * Marca todas las notificaciones como leídas.
     */
    suspend fun marcarTodasComoLeidas() {
        context.notificacionDataStore.edit { prefs ->
            val notificacionesJson = prefs[NOTIFICACIONES_KEY] ?: "[]"
            val notificaciones = gson.fromJson<MutableList<Notificacion>>(
                notificacionesJson,
                object : TypeToken<MutableList<Notificacion>>() {}.type
            ) ?: mutableListOf()

            val notificacionesLeidas = notificaciones.map { it.copy(leida = true) }
            prefs[NOTIFICACIONES_KEY] = gson.toJson(notificacionesLeidas)
        }
    }

    /**
     * Limpia todas las notificaciones.
     */
    suspend fun limpiarNotificaciones() {
        context.notificacionDataStore.edit { prefs ->
            prefs[NOTIFICACIONES_KEY] = "[]"
        }
    }
}