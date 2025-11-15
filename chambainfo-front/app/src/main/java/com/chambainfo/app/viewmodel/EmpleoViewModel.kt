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

    fun cargarEmpleoPorId(id: Long) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null // Limpiar errores anteriores

                println("DEBUG: EmpleoViewModel - Cargando empleo ID: $id")

                val response = repository.obtenerEmpleoPorId(id)

                println("DEBUG: EmpleoViewModel - Respuesta recibida. Código: ${response.code()}")

                if (response.isSuccessful && response.body() != null) {
                    val empleo = response.body()!!
                    println("DEBUG: EmpleoViewModel - Empleo obtenido: ID=${empleo.id}, MostrarNumero=${empleo.mostrarNumero}, Celular='${empleo.celularContacto}'")
                    _empleoDetalle.value = empleo
                } else {
                    println("ERROR: EmpleoViewModel - Empleo no encontrado. Código: ${response.code()}")
                    _error.value = "Empleo no encontrado"
                }
            } catch (e: Exception) {
                println("ERROR: EmpleoViewModel - Excepción: ${e.message}")
                _error.value = e.message ?: "Error de conexión"
            } finally {
                _loading.value = false
            }
        }
    }

    // NUEVO: Método para limpiar datos anteriores
    fun limpiarEmpleoDetalle() {
        println("DEBUG: EmpleoViewModel - Limpiando empleoDetalle")
        _empleoDetalle.value = null
        _error.value = null
    }

    // NUEVO: Método para cargar empleo directamente (alternativa)
    suspend fun cargarEmpleoDirecto(id: Long): Empleo? {
        return try {
            println("DEBUG: EmpleoViewModel - Carga directa empleo ID: $id")

            val response = repository.obtenerEmpleoPorId(id)

            if (response.isSuccessful && response.body() != null) {
                val empleo = response.body()!!
                println("DEBUG: EmpleoViewModel - Carga directa exitosa: ID=${empleo.id}, MostrarNumero=${empleo.mostrarNumero}, Celular='${empleo.celularContacto}'")
                empleo
            } else {
                println("ERROR: EmpleoViewModel - Carga directa fallida. Código: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            println("ERROR: EmpleoViewModel - Excepción en carga directa: ${e.message}")
            null
        }
    }

    // Método para cargar empleos por empleador
    fun cargarEmpleosPorEmpleador(empleadorId: Long) {
        viewModelScope.launch {
            try {
                _loading.value = true
                android.util.Log.d("EmpleoViewModel", "Llamando a obtenerEmpleosPorEmpleador con ID: $empleadorId")

                val response = repository.obtenerEmpleosPorEmpleador(empleadorId)

                android.util.Log.d("EmpleoViewModel", "Respuesta recibida. Código: ${response.code()}")

                if (response.isSuccessful) {
                    val empleos = response.body() ?: emptyList()
                    android.util.Log.d("EmpleoViewModel", "Empleos obtenidos: ${empleos.size}")
                    _empleos.value = empleos
                } else {
                    val errorMsg = "Error al cargar empleos: ${response.code()}"
                    android.util.Log.e("EmpleoViewModel", errorMsg)
                    _error.value = errorMsg
                }
            } catch (e: Exception) {
                val errorMsg = "Error de conexión: ${e.message}"
                android.util.Log.e("EmpleoViewModel", errorMsg, e)
                _error.value = errorMsg
            } finally {
                _loading.value = false
            }
        }
    }

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