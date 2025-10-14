package com.chambainfo.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chambainfo.app.data.model.AuthResponse
import com.chambainfo.app.data.model.LoginRequest
import com.chambainfo.app.data.model.RegisterRequest
import com.chambainfo.app.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _registerResult = MutableLiveData<Result<AuthResponse>>()
    val registerResult: LiveData<Result<AuthResponse>> = _registerResult

    private val _loginResult = MutableLiveData<Result<AuthResponse>>()
    val loginResult: LiveData<Result<AuthResponse>> = _loginResult

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    fun register(request: RegisterRequest) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val response = repository.register(request)
                if (response.isSuccessful && response.body() != null) {
                    _registerResult.value = Result.success(response.body()!!)
                } else {
                    _registerResult.value = Result.failure(
                        Exception(response.errorBody()?.string() ?: "Error al registrar")
                    )
                }
            } catch (e: Exception) {
                _registerResult.value = Result.failure(e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun login(request: LoginRequest) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val response = repository.login(request)
                if (response.isSuccessful && response.body() != null) {
                    _loginResult.value = Result.success(response.body()!!)
                } else {
                    _loginResult.value = Result.failure(
                        Exception(response.errorBody()?.string() ?: "Usuario o contraseña incorrectos")
                    )
                }
            } catch (e: Exception) {
                _loginResult.value = Result.failure(e)
            } finally {
                _loading.value = false
            }
        }
    }
}