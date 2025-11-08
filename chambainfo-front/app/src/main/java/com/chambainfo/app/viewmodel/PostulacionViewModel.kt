package com.chambainfo.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chambainfo.app.data.model.PostulacionRequest
import com.chambainfo.app.data.model.PostulacionResponse
import com.chambainfo.app.data.repository.PostulacionRepository
import kotlinx.coroutines.launch

class PostulacionViewModel : ViewModel() {

    private val repository = PostulacionRepository()

    private val _postularResult = MutableLiveData<Result<PostulacionResponse>>()
    val postularResult: LiveData<Result<PostulacionResponse>> = _postularResult

    private val _yaPostuloResult = MutableLiveData<Boolean>()
    val yaPostuloResult: LiveData<Boolean> = _yaPostuloResult

    private val _misPostulaciones = MutableLiveData<List<PostulacionResponse>>()
    val misPostulaciones: LiveData<List<PostulacionResponse>> = _misPostulaciones

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    /**
     * Envía una postulación para un empleo específico.
     *
     * @param token El token de autenticación del usuario.
     * @param request Los datos de la postulación.
     */
    fun postular(token: String, request: PostulacionRequest) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val response = repository.postular(token, request)
                if (response.isSuccessful && response.body() != null) {
                    _postularResult.value = Result.success(response.body()!!)
                } else {
                    _postularResult.value = Result.failure(
                        Exception(response.errorBody()?.string() ?: "Error al postular")
                    )
                }
            } catch (e: Exception) {
                _postularResult.value = Result.failure(e)
            } finally {
                _loading.value = false
            }
        }
    }

    /**
     * Verifica si el usuario ya postuló a un empleo específico.
     *
     * @param token El token de autenticación del usuario.
     * @param empleoId El ID del empleo a verificar.
     */
    fun verificarYaPostulo(token: String, empleoId: Long) {
        viewModelScope.launch {
            try {
                val response = repository.yaPostulo(token, empleoId)
                if (response.isSuccessful && response.body() != null) {
                    _yaPostuloResult.value = response.body()!!
                } else {
                    _yaPostuloResult.value = false
                }
            } catch (e: Exception) {
                _yaPostuloResult.value = false
            }
        }
    }

    /**
     * Carga todas las postulaciones del usuario autenticado.
     *
     * @param token El token de autenticación del usuario.
     */
    fun cargarMisPostulaciones(token: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val response = repository.obtenerMisPostulaciones(token)
                if (response.isSuccessful) {
                    _misPostulaciones.value = response.body() ?: emptyList()
                } else {
                    _error.value = "Error al cargar postulaciones"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error de conexión"
            } finally {
                _loading.value = false
            }
        }
    }
}