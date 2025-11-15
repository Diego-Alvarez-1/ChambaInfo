package com.chambainfo.app.ui.empleador

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class EmpleadorDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmpleadorDashboardBinding
    private val empleoViewModel: EmpleoViewModel by viewModels()
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

        binding.btnNotificaciones.setOnClickListener {
            // TODO: Implementar notificaciones
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
    }
}