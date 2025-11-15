package com.chambainfo.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chambainfo.app.data.model.EmpleadorEstadisticas
import com.chambainfo.app.data.model.EmpleoConPostulaciones
import com.chambainfo.app.data.model.Postulante
import com.chambainfo.app.data.repository.EmpleadorRepository
import kotlinx.coroutines.launch

class EmpleadorViewModel : ViewModel() {

    private val repository = EmpleadorRepository()

    private val _estadisticas = MutableLiveData<EmpleadorEstadisticas>()
    val estadisticas: LiveData<EmpleadorEstadisticas> = _estadisticas

    private val _misEmpleos = MutableLiveData<List<EmpleoConPostulaciones>>()
    val misEmpleos: LiveData<List<EmpleoConPostulaciones>> = _misEmpleos

    private val _postulantes = MutableLiveData<List<Postulante>>()
    val postulantes: LiveData<List<Postulante>> = _postulantes

    

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun cargarEstadisticas(token: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val response = repository.obtenerEstadisticas(token)
                if (response.isSuccessful && response.body() != null) {
                    _estadisticas.value = response.body()
                } else {
                    _error.value = "Error al cargar estadísticas"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error de conexión"
            } finally {
                _loading.value = false
            }
        }
    }

    fun cargarMisEmpleos(token: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val response = repository.obtenerMisEmpleosConPostulaciones(token)
                if (response.isSuccessful && response.body() != null) {
                    _misEmpleos.value = response.body()
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

    fun cargarPostulantes(token: String, empleoId: Long) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val response = repository.obtenerPostulantesDeEmpleo(token, empleoId)
                if (response.isSuccessful && response.body() != null) {
                    _postulantes.value = response.body()
                } else {
                    _error.value = "Error al cargar postulantes"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error de conexión"
            } finally {
                _loading.value = false
            }
        }
    }

    

    fun cambiarEstadoPostulacion(token: String, postulacionId: Long, nuevoEstado: String) {
        viewModelScope.launch {
            try {
                val response = repository.cambiarEstadoPostulacion(token, postulacionId, nuevoEstado)
                if (response.isSuccessful) {
                    // Recargar postulantes después de cambiar estado
                    _postulantes.value?.let { listaActual ->
                        val listaActualizada = listaActual.map { postulante ->
                            if (postulante.postulacionId == postulacionId) {
                                postulante.copy(estado = nuevoEstado)
                            } else {
                                postulante
                            }
                        }
                        _postulantes.value = listaActualizada
                    }
                } else {
                    _error.value = "Error al cambiar estado"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error de conexión"
            }
        }
    }

    fun finalizarEmpleo(token: String, empleoId: Long) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val response = repository.finalizarEmpleo(token, empleoId)
                if (response.isSuccessful) {
                    // Recargar empleos después de finalizar
                    cargarMisEmpleos(token)
                } else {
                    _error.value = "Error al finalizar empleo"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error de conexión"
            } finally {
                _loading.value = false
            }
        }
    }
}