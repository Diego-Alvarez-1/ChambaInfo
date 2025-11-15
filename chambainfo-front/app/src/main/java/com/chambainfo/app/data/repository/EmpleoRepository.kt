package com.chambainfo.app.data.repository

import com.chambainfo.app.data.api.RetrofitClient
import com.chambainfo.app.data.model.Empleo
import com.chambainfo.app.data.model.PublicarEmpleoRequest
import retrofit2.Response

class EmpleoRepository {

    private val apiService = RetrofitClient.apiService

    /**
     * Obtiene todos los empleos disponibles en el sistema.
     *
     * @return Una respuesta con la lista de todos los empleos activos.
     */
    suspend fun obtenerTodosLosEmpleos(): Response<List<Empleo>> {
        return apiService.obtenerTodosLosEmpleos()
    }

    /**
     * Obtiene los detalles de un empleo específico por su ID.
     *
     * @param id El ID del empleo a obtener.
     * @return Una respuesta con los detalles del empleo.
     */
    suspend fun obtenerEmpleoPorId(id: Long): Response<Empleo> {
        return apiService.obtenerEmpleoPorId(id)
    }

    /**
     * Obtiene todos los empleos publicados por un empleador específico.
     *
     * @param empleadorId El ID del empleador.
     * @return Una respuesta con la lista de empleos del empleador.
     */
    suspend fun obtenerEmpleosPorEmpleador(empleadorId: Long): Response<List<Empleo>> {
        return apiService.obtenerEmpleosPorEmpleador(empleadorId)
    }

    /**
     * Publica un nuevo empleo en el sistema.
     *
     * @param token El token de autenticación del usuario.
     * @param request Los datos del empleo a publicar.
     * @return Una respuesta con los datos del empleo publicado.
     */
    suspend fun publicarEmpleo(token: String, request: PublicarEmpleoRequest): Response<Empleo> {
        return apiService.publicarEmpleo("Bearer $token", request)
    }

    /**
     * Desactiva un empleo publicado.
     *
     * @param token El token de autenticación del usuario.
     * @param id El ID del empleo a desactivar.
     * @return Una respuesta con un mensaje de confirmación.
     */
    suspend fun desactivarEmpleo(token: String, id: Long): Response<String> {
        return apiService.desactivarEmpleo("Bearer $token", id)
    }
}