package com.chambainfo.app.data.repository

import com.chambainfo.app.data.api.RetrofitClient
import com.chambainfo.app.data.model.AuthResponse
import com.chambainfo.app.data.model.LoginRequest
import com.chambainfo.app.data.model.RegisterRequest
import com.chambainfo.app.data.model.ReniecResponse
import retrofit2.Response

class AuthRepository {

    private val apiService = RetrofitClient.apiService

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param request Los datos de registro del usuario.
     * @return Una respuesta con los datos de autenticación del usuario registrado.
     */
    suspend fun register(request: RegisterRequest): Response<AuthResponse> {
        return apiService.register(request)
    }

    /**
     * Inicia sesión con las credenciales del usuario.
     *
     * @param request Los datos de login (usuario y contraseña).
     * @return Una respuesta con los datos de autenticación del usuario.
     */
    suspend fun login(request: LoginRequest): Response<AuthResponse> {
        return apiService.login(request)
    }

    /**
     * Endpoint de prueba para verificar la conexión con el servidor.
     *
     * @return Una respuesta con un mensaje de prueba.
     */
    suspend fun test(): Response<String> {
        return apiService.test()
    }

    /**
     * Verifica un DNI consultando la base de datos de RENIEC.
     *
     * @param dni El número de DNI a verificar (8 dígitos).
     * @return Una respuesta con los datos del DNI obtenidos de RENIEC.
     */
    suspend fun verificarDni(dni: String): Response<ReniecResponse> {
        return apiService.verificarDni(dni)
    }
}