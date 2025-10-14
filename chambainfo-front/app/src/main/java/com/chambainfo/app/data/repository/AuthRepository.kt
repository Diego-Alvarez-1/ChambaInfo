package com.chambainfo.app.data.repository

import com.chambainfo.app.data.api.RetrofitClient
import com.chambainfo.app.data.model.AuthResponse
import com.chambainfo.app.data.model.LoginRequest
import com.chambainfo.app.data.model.RegisterRequest
import retrofit2.Response

class AuthRepository {

    private val apiService = RetrofitClient.apiService

    suspend fun register(request: RegisterRequest): Response<AuthResponse> {
        return apiService.register(request)
    }

    suspend fun login(request: LoginRequest): Response<AuthResponse> {
        return apiService.login(request)
    }

    suspend fun test(): Response<String> {
        return apiService.test()
    }
}