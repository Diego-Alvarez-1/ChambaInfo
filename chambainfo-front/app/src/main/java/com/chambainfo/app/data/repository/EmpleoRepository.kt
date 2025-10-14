package com.chambainfo.app.data.repository

import com.chambainfo.app.data.api.RetrofitClient
import com.chambainfo.app.data.model.Empleo
import com.chambainfo.app.data.model.PublicarEmpleoRequest
import retrofit2.Response

class EmpleoRepository {

    private val apiService = RetrofitClient.apiService

    suspend fun obtenerTodosLosEmpleos(): Response<List<Empleo>> {
        return apiService.obtenerTodosLosEmpleos()
    }

    suspend fun obtenerEmpleoPorId(id: Long): Response<Empleo> {
        return apiService.obtenerEmpleoPorId(id)
    }

    suspend fun obtenerEmpleosPorEmpleador(empleadorId: Long): Response<List<Empleo>> {
        return apiService.obtenerEmpleosPorEmpleador(empleadorId)
    }

    suspend fun publicarEmpleo(token: String, request: PublicarEmpleoRequest): Response<Empleo> {
        return apiService.publicarEmpleo("Bearer $token", request)
    }

    suspend fun desactivarEmpleo(token: String, id: Long): Response<String> {
        return apiService.desactivarEmpleo("Bearer $token", id)
    }
}