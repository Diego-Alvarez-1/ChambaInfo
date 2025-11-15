package com.chambainfo.app.ui.empleador

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.chambainfo.app.R
import com.chambainfo.app.data.local.TokenManager
import com.chambainfo.app.data.model.Empleo
import com.chambainfo.app.databinding.ActivityEmpleadorDashboardBinding
import com.chambainfo.app.ui.MainActivity
import com.chambainfo.app.ui.auth.LoginActivity
import com.chambainfo.app.ui.empleo.DetalleEmpleoActivity
import com.chambainfo.app.ui.empleo.PublicarEmpleoActivity
import com.chambainfo.app.ui.profile.PerfilActivity
import com.chambainfo.app.viewmodel.EmpleoViewModel
import com.chambainfo.app.viewmodel.PostulacionViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class EmpleadorDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmpleadorDashboardBinding
    private val empleoViewModel: EmpleoViewModel by viewModels()
    private val postulacionViewModel: PostulacionViewModel by viewModels()
    private lateinit var tokenManager: TokenManager
    private lateinit var adapter: MisEmpleosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityEmpleadorDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        cargarMisEmpleos()
        cargarEstadisticas()
    }

    private fun setupRecyclerView() {
        adapter = MisEmpleosAdapter(
            onEmpleoClick = { empleo ->
                abrirDetalleEmpleo(empleo.id)
            },
            onVerPostulacionesClick = { empleo ->
                abrirPostulaciones(empleo.id)
            }
        )

        binding.rvMisEmpleos.layoutManager = LinearLayoutManager(this)
        binding.rvMisEmpleos.adapter = adapter
    }

    private fun setupObservers() {
        empleoViewModel.empleos.observe(this) { empleos ->
            if (empleos.isEmpty()) {
                binding.layoutEmptyState.visibility = View.VISIBLE
                binding.rvMisEmpleos.visibility = View.GONE
            } else {
                binding.layoutEmptyState.visibility = View.GONE
                binding.rvMisEmpleos.visibility = View.VISIBLE
                adapter.submitList(empleos)
            }
        }

        empleoViewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        empleoViewModel.error.observe(this) { errorMsg ->
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClickListeners() {
        binding.btnPerfil.setOnClickListener {
            mostrarMenuPerfil()
        }

        binding.layoutNotificaciones.btnNotificaciones.setOnClickListener {
            Toast.makeText(this, "Notificaciones próximamente", Toast.LENGTH_SHORT).show()
        }

        binding.btnPublicarEmpleo.setOnClickListener {
            startActivity(Intent(this, PublicarEmpleoActivity::class.java))
        }

        binding.btnVerTodosEmpleos.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun cargarMisEmpleos() {
        lifecycleScope.launch {
            val userId = tokenManager.getUserId().first()
            Log.d("EmpleadorDashboard", "Usuario ID: $userId")

            if (userId == null) {
                Log.e("EmpleadorDashboard", "Error: userId es null")
                Toast.makeText(
                    this@EmpleadorDashboardActivity,
                    "Error: No se pudo obtener el ID del usuario",
                    Toast.LENGTH_LONG
                ).show()
                return@launch
            }

            Log.d("EmpleadorDashboard", "Cargando empleos para empleador: $userId")
            empleoViewModel.cargarEmpleosPorEmpleador(userId)
        }
    }

    private fun cargarEstadisticas() {
        lifecycleScope.launch {
            val token = tokenManager.getToken().first()
            val userId = tokenManager.getUserId().first()

            if (token != null && userId != null) {
                empleoViewModel.cargarEmpleosPorEmpleador(userId)

                empleoViewModel.empleos.observe(this@EmpleadorDashboardActivity) { empleos ->
                    val empleosActivos = empleos.filter { it.activo }
                    binding.tvTotalEmpleos.text = empleosActivos.size.toString()

                    // Cargar todas las postulaciones
                    var todasLasPostulaciones = mutableListOf<com.chambainfo.app.data.model.PostulacionResponse>()

                    empleosActivos.forEach { empleo ->
                        lifecycleScope.launch {
                            try {
                                val response = com.chambainfo.app.data.api.RetrofitClient.apiService
                                    .obtenerPostulacionesPorEmpleo("Bearer $token", empleo.id)

                                if (response.isSuccessful && response.body() != null) {
                                    // Filtrar solo postulaciones NO archivadas (estado diferente de ARCHIVADO)
                                    val postulacionesActivas = response.body()!!.filter {
                                        it.estado != "ARCHIVADO"
                                    }
                                    todasLasPostulaciones.addAll(postulacionesActivas)

                                    // Actualizar contadores
                                    binding.tvTotalPostulaciones.text = todasLasPostulaciones.size.toString()

                                    // Calcular nuevas (últimas 24 horas)
                                    val ahora = System.currentTimeMillis()
                                    val hace24Horas = ahora - (24 * 60 * 60 * 1000)

                                    val nuevas = todasLasPostulaciones.count { postulacion ->
                                        try {
                                            val formato = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                                            val fecha = formato.parse(postulacion.fechaPostulacion)
                                            (fecha?.time ?: 0) > hace24Horas
                                        } catch (e: Exception) {
                                            false
                                        }
                                    }

                                    binding.tvTotalNuevas.text = nuevas.toString()

                                    // Actualizar badge
                                    val badgeCount = binding.layoutNotificaciones.tvBadgeCount
                                    if (nuevas > 0) {
                                        badgeCount.visibility = View.VISIBLE
                                        badgeCount.text = if (nuevas > 99) "99+" else nuevas.toString()
                                    } else {
                                        badgeCount.visibility = View.GONE
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("EmpleadorDashboard", "Error cargando postulaciones: ${e.message}")
                            }
                        }
                    }
                }
            }
        }
    }
    private fun abrirDetalleEmpleo(empleoId: Long) {
        val intent = Intent(this, DetalleEmpleoActivity::class.java)
        intent.putExtra("EMPLEO_ID", empleoId)
        startActivity(intent)
    }

    private fun abrirPostulaciones(empleoId: Long) {
        val intent = Intent(this, PostulacionesEmpleoActivity::class.java)
        intent.putExtra("EMPLEO_ID", empleoId)
        startActivity(intent)
    }

    private fun mostrarMenuPerfil() {
        val opciones = arrayOf("Ver mi perfil", "Cerrar sesión")

        AlertDialog.Builder(this)
            .setTitle("Mi cuenta")
            .setItems(opciones) { dialog, which ->
                when (which) {
                    0 -> {
                        startActivity(Intent(this, PerfilActivity::class.java))
                    }
                    1 -> {
                        mostrarDialogoCerrarSesion()
                    }
                }
            }
            .show()
    }

    private fun mostrarDialogoCerrarSesion() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Estás seguro de que deseas cerrar sesión?")
            .setPositiveButton("Sí") { _, _ ->
                cerrarSesion()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun cerrarSesion() {
        lifecycleScope.launch {
            tokenManager.clearAllData()

            Toast.makeText(
                this@EmpleadorDashboardActivity,
                "Sesión cerrada exitosamente",
                Toast.LENGTH_SHORT
            ).show()

            val intent = Intent(this@EmpleadorDashboardActivity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        cargarMisEmpleos()
        cargarEstadisticas()
    }
}