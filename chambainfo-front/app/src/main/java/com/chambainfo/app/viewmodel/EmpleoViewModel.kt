package com.chambainfo.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chambainfo.app.data.model.Empleo
import com.chambainfo.app.data.model.PublicarEmpleoRequest
import com.chambainfo.app.data.repository.EmpleoRepository
import kotlinx.coroutines.launch

class EmpleoViewModel : ViewModel() {

    private val repository = EmpleoRepository()

    private val _empleos = MutableLiveData<List<Empleo>>()
    val empleos: LiveData<List<Empleo>> = _empleos

    private val _empleoDetalle = MutableLiveData<Empleo>()
    val empleoDetalle: LiveData<Empleo> = _empleoDetalle

    private val _publicarResult = MutableLiveData<Result<Empleo>>()
    val publicarResult: LiveData<Result<Empleo>> = _publicarResult

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    /**
     * Carga todos los empleos disponibles desde el servidor.
     */
    fun cargarEmpleos() {
        viewModelScope.launch {
            try {
                _loading.value = true
                val response = repository.obtenerTodosLosEmpleos()
                if (response.isSuccessful) {
                    _empleos.value = response.body() ?: emptyList()
                } else {
                    _error.value = "Error al cargar empleos"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error de conexión"
            } finally {
                _loading.value = false
            }
        }
    }

    /**
     * Carga los detalles de un empleo específico por su ID.
     *
     * @param id El ID del empleo a cargar.
     */
    fun cargarEmpleoPorId(id: Long) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val response = repository.obtenerEmpleoPorId(id)
                if (response.isSuccessful && response.body() != null) {
                    _empleoDetalle.value = response.body()
                } else {
                    _error.value = "Empleo no encontrado"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error de conexión"
            } finally {
                _loading.value = false
            }
        }
    }

    /**
     * Publica un nuevo empleo en el sistema.
     *
     * @param token El token de autenticación del usuario.
     * @param request Los datos del empleo a publicar.
     */
    fun publicarEmpleo(token: String, request: PublicarEmpleoRequest) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val response = repository.publicarEmpleo(token, request)
                if (response.isSuccessful && response.body() != null) {
                    _publicarResult.value = Result.success(response.body()!!)
                } else {
                    _publicarResult.value = Result.failure(
                        Exception(response.errorBody()?.string() ?: "Error al publicar")
                    )
                }
            } catch (e: Exception) {
                _publicarResult.value = Result.failure(e)
            } finally {
                _loading.value = false
            }
        }
    }
}