package com.chambainfo.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "chambainfo_prefs")

class TokenManager(private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_ID_KEY = longPreferencesKey("user_id")
        private val DNI_KEY = stringPreferencesKey("dni")
        private val NOMBRE_KEY = stringPreferencesKey("nombre")
        private val USUARIO_KEY = stringPreferencesKey("usuario")
        private val CELULAR_KEY = stringPreferencesKey("celular")
    }

    /**
     * Guarda todos los datos de autenticación del usuario en el almacenamiento local.
     *
     * @param token El token de autenticación JWT.
     * @param userId El ID del usuario.
     * @param dni El DNI del usuario.
     * @param nombre El nombre completo del usuario.
     * @param usuario El nombre de usuario.
     * @param celular El número de celular del usuario.
     */
    suspend fun saveAuthData(
        token: String,
        userId: Long,
        dni: String,
        nombre: String,
        usuario: String,
        celular: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[USER_ID_KEY] = userId
            prefs[DNI_KEY] = dni
            prefs[NOMBRE_KEY] = nombre
            prefs[USUARIO_KEY] = usuario
            prefs[CELULAR_KEY] = celular
        }
    }

    /**
     * Obtiene el token de autenticación guardado.
     *
     * @return Un Flow que emite el token de autenticación o null si no existe.
     */
    fun getToken(): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            prefs[TOKEN_KEY]
        }
    }

    /**
     * Obtiene el ID del usuario guardado.
     *
     * @return Un Flow que emite el ID del usuario o null si no existe.
     */
    fun getUserId(): Flow<Long?> {
        return context.dataStore.data.map { prefs ->
            prefs[USER_ID_KEY]
        }
    }

    /**
     * Obtiene el DNI del usuario guardado.
     *
     * @return Un Flow que emite el DNI del usuario o null si no existe.
     */
    fun getDni(): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            prefs[DNI_KEY]
        }
    }

    /**
     * Obtiene el nombre completo del usuario guardado.
     *
     * @return Un Flow que emite el nombre completo del usuario o null si no existe.
     */
    fun getNombre(): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            prefs[NOMBRE_KEY]
        }
    }

    /**
     * Obtiene el nombre de usuario guardado.
     *
     * @return Un Flow que emite el nombre de usuario o null si no existe.
     */
    fun getUsuario(): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            prefs[USUARIO_KEY]
        }
    }

    /**
     * Obtiene el número de celular del usuario guardado.
     *
     * @return Un Flow que emite el número de celular o null si no existe.
     */
    fun getCelular(): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            prefs[CELULAR_KEY]
        }
    }

    /**
     * Limpia todos los datos de autenticación guardados.
     */
    suspend fun clearAllData() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    /**
     * Verifica si el usuario tiene una sesión activa.
     *
     * @return true si el usuario está logueado, false en caso contrario.
     */
    suspend fun isLoggedIn(): Boolean {
        var isLogged = false
        context.dataStore.data.collect { prefs ->
            isLogged = prefs[TOKEN_KEY] != null
        }
        return isLogged
    }
}