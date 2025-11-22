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
        private val ROL_KEY = stringPreferencesKey("rol") // NUEVO
    }

    suspend fun saveAuthData(
        token: String,
        userId: Long,
        dni: String,
        nombre: String,
        usuario: String,
        celular: String,
        rol: String = "TRABAJADOR"
    ) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[USER_ID_KEY] = userId
            prefs[DNI_KEY] = dni
            prefs[NOMBRE_KEY] = nombre
            prefs[USUARIO_KEY] = usuario
            prefs[CELULAR_KEY] = celular
            prefs[ROL_KEY] = rol
        }
    }

    fun getToken(): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            prefs[TOKEN_KEY]
        }
    }

    fun getUserId(): Flow<Long?> {
        return context.dataStore.data.map { prefs ->
            prefs[USER_ID_KEY]
        }
    }

    fun getDni(): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            prefs[DNI_KEY]
        }
    }

    fun getNombre(): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            prefs[NOMBRE_KEY]
        }
    }

    fun getUsuario(): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            prefs[USUARIO_KEY]
        }
    }

    fun getCelular(): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            prefs[CELULAR_KEY]
        }
    }

    // NUEVO
    fun getRol(): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            prefs[ROL_KEY]
        }
    }

    suspend fun clearAllData() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    suspend fun isLoggedIn(): Boolean {
        var isLogged = false
        context.dataStore.data.collect { prefs ->
            isLogged = prefs[TOKEN_KEY] != null
        }
        return isLogged
    }
}