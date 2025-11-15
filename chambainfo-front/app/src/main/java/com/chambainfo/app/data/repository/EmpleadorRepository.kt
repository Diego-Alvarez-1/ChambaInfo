package com.chambainfo.app.data.repository

import com.chambainfo.app.data.api.RetrofitClient
import com.chambainfo.app.data.model.EmpleadorEstadisticas
import com.chambainfo.app.data.model.EmpleoConPostulaciones
import com.chambainfo.app.data.model.Postulante
import retrofit2.Response

class EmpleadorRepository {

    private val apiService = RetrofitClient.apiService

    suspend fun obtenerEstadisticas(token: String): Response<EmpleadorEstadisticas> {
        return apiService.obtenerEstadisticasEmpleador("Bearer $token")
    }

    suspend fun obtenerMisEmpleosConPostulaciones(token: String): Response<List<EmpleoConPostulaciones>> {
        return apiService.obtenerMisEmpleosConPostulaciones("Bearer $token")
    }

    suspend fun obtenerPostulantesDeEmpleo(token: String, empleoId: Long): Response<List<Postulante>> {
        return apiService.obtenerPostulantesDeEmpleo("Bearer $token", empleoId)
    }

    

    suspend fun cambiarEstadoPostulacion(
        token: String,
        postulacionId: Long,
        nuevoEstado: String
    ): Response<Map<String, String>> {
        return apiService.cambiarEstadoPostulacion("Bearer $token", postulacionId, nuevoEstado)
    }

    suspend fun finalizarEmpleo(token: String, empleoId: Long): Response<Map<String, String>> {
        return apiService.finalizarEmpleo("Bearer $token", empleoId)
    }
}