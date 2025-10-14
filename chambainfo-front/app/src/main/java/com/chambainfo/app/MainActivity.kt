package com.chambainfo.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.chambainfo.app.R
import com.chambainfo.app.data.local.TokenManager
import com.chambainfo.app.databinding.ActivityMainBinding
import com.chambainfo.app.ui.auth.LoginActivity
import com.chambainfo.app.ui.empleo.DetalleEmpleoActivity
import com.chambainfo.app.ui.empleo.EmpleoAdapter
import com.chambainfo.app.ui.empleo.PublicarEmpleoActivity
import com.chambainfo.app.ui.profile.PerfilActivity
import com.chambainfo.app.viewmodel.EmpleoViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val empleoViewModel: EmpleoViewModel by viewModels()
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        setupRecyclerViews()
        setupObservers()
        setupClickListeners()

        // Cargar empleos
        empleoViewModel.cargarEmpleos()
    }

    private fun setupRecyclerViews() {
        // Configurar RecyclerViews horizontales
        binding.rvEmpleosAtencion.layoutManager = LinearLayoutManager(
            this, LinearLayoutManager.HORIZONTAL, false
        )
        binding.rvEmpleosConstruccion.layoutManager = LinearLayoutManager(
            this, LinearLayoutManager.HORIZONTAL, false
        )
        binding.rvEmpleosCocina.layoutManager = LinearLayoutManager(
            this, LinearLayoutManager.HORIZONTAL, false
        )
        binding.rvEmpleosLimpieza.layoutManager = LinearLayoutManager(
            this, LinearLayoutManager.HORIZONTAL, false
        )
        binding.rvEmpleosDelivery.layoutManager = LinearLayoutManager(
            this, LinearLayoutManager.HORIZONTAL, false
        )
    }

    private fun setupObservers() {
        empleoViewModel.empleos.observe(this) { empleos ->
            // Filtrar empleos por categoría (basándonos en el nombre)
            val atencion = empleos.filter {
                it.nombreEmpleo.contains("cajero", true) ||
                        it.nombreEmpleo.contains("vendedor", true)
            }
            val construccion = empleos.filter {
                it.nombreEmpleo.contains("albañil", true) ||
                        it.nombreEmpleo.contains("ayudante", true)
            }
            val cocina = empleos.filter {
                it.nombreEmpleo.contains("cocinero", true) ||
                        it.nombreEmpleo.contains("mesero", true)
            }
            val limpieza = empleos.filter {
                it.nombreEmpleo.contains("limpieza", true)
            }
            val delivery = empleos.filter {
                it.nombreEmpleo.contains("delivery", true)
            }

            // Configurar adapters
            if (atencion.isNotEmpty()) {
                binding.rvEmpleosAtencion.adapter = EmpleoAdapter(atencion) { empleo ->
                    abrirDetalleEmpleo(empleo.id)
                }
            }

            if (construccion.isNotEmpty()) {
                binding.rvEmpleosConstruccion.adapter = EmpleoAdapter(construccion) { empleo ->
                    abrirDetalleEmpleo(empleo.id)
                }
            }

            if (cocina.isNotEmpty()) {
                binding.rvEmpleosCocina.adapter = EmpleoAdapter(cocina) { empleo ->
                    abrirDetalleEmpleo(empleo.id)
                }
            }

            if (limpieza.isNotEmpty()) {
                binding.rvEmpleosLimpieza.adapter = EmpleoAdapter(limpieza) { empleo ->
                    abrirDetalleEmpleo(empleo.id)
                }
            }

            if (delivery.isNotEmpty()) {
                binding.rvEmpleosDelivery.adapter = EmpleoAdapter(delivery) { empleo ->
                    abrirDetalleEmpleo(empleo.id)
                }
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
        binding.btnPublicarEmpleo.setOnClickListener {
            lifecycleScope.launch {
                tokenManager.getToken().collect { token ->
                    if (token != null) {
                        // Usuario logueado, abrir publicar empleo
                        startActivity(Intent(this@MainActivity, PublicarEmpleoActivity::class.java))
                    } else {
                        // No logueado, ir a login
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                    }
                }
            }
        }

        binding.btnComenzarAhora.setOnClickListener {
            lifecycleScope.launch {
                tokenManager.getToken().collect { token ->
                    if (token != null) {
                        startActivity(Intent(this@MainActivity, PublicarEmpleoActivity::class.java))
                    } else {
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
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

    override fun onResume() {
        super.onResume()
        // Recargar empleos cuando se vuelve a la actividad
        empleoViewModel.cargarEmpleos()
    }
}