package com.chambainfo.app.data.api

import com.chambainfo.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Auth Endpoints
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("auth/test")
    suspend fun test(): Response<String>

    @GET("auth/verificar-dni/{dni}")
    suspend fun verificarDni(@Path("dni") dni: String): Response<ReniecResponse>

    // Empleos Endpoints
    @GET("empleos")
    suspend fun obtenerTodosLosEmpleos(): Response<List<Empleo>>

    @GET("empleos/{id}")
    suspend fun obtenerEmpleoPorId(@Path("id") id: Long): Response<Empleo>

    @GET("empleos/empleador/{empleadorId}")
    suspend fun obtenerEmpleosPorEmpleador(@Path("empleadorId") empleadorId: Long): Response<List<Empleo>>

    @POST("empleos/publicar")
    suspend fun publicarEmpleo(
        @Header("Authorization") token: String,
        @Body request: PublicarEmpleoRequest
    ): Response<Empleo>

    @DELETE("empleos/{id}")
    suspend fun desactivarEmpleo(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<String>
}