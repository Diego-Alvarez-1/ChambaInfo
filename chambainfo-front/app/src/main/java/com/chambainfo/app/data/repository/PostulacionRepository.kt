package com.chambainfo.app.data.repository

import com.chambainfo.app.data.api.RetrofitClient
import com.chambainfo.app.data.model.PostulacionRequest
import com.chambainfo.app.data.model.PostulacionResponse
import retrofit2.Response

class PostulacionRepository {

    private val apiService = RetrofitClient.apiService

    /**
     * Envía una postulación para un empleo específico.
     *
     * @param token El token de autenticación del usuario.
     * @param request Los datos de la postulación.
     * @return Una respuesta con los datos de la postulación creada.
     */
    suspend fun postular(token: String, request: PostulacionRequest): Response<PostulacionResponse> {
        return apiService.postular("Bearer $token", request)
    }

    /**
     * Obtiene todas las postulaciones de un empleo específico.
     *
     * @param token El token de autenticación del usuario.
     * @param empleoId El ID del empleo.
     * @return Una respuesta con la lista de postulaciones del empleo.
     */
    suspend fun obtenerPostulacionesPorEmpleo(token: String, empleoId: Long): Response<List<PostulacionResponse>> {
        return apiService.obtenerPostulacionesPorEmpleo("Bearer $token", empleoId)
    }

    /**
     * Obtiene todas las postulaciones del usuario autenticado.
     *
     * @param token El token de autenticación del usuario.
     * @return Una respuesta con la lista de postulaciones del usuario.
     */
    suspend fun obtenerMisPostulaciones(token: String): Response<List<PostulacionResponse>> {
        return apiService.obtenerMisPostulaciones("Bearer $token")
    }

    /**
     * Verifica si el usuario ya postuló a un empleo específico.
     *
     * @param token El token de autenticación del usuario.
     * @param empleoId El ID del empleo a verificar.
     * @return Una respuesta con true si ya postuló, false en caso contrario.
     */
    suspend fun yaPostulo(token: String, empleoId: Long): Response<Boolean> {
        return apiService.yaPostulo("Bearer $token", empleoId)
    }
}