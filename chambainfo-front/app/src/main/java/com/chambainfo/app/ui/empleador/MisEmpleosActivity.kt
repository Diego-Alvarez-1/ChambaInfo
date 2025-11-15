package com.chambainfo.app.ui.empleador

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.chambainfo.app.data.local.TokenManager
import com.chambainfo.app.databinding.ActivityMisEmpleosBinding
import com.chambainfo.app.viewmodel.EmpleadorViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MisEmpleosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMisEmpleosBinding
    private val empleadorViewModel: EmpleadorViewModel by viewModels()
    private lateinit var tokenManager: TokenManager
    private lateinit var adapter: MisEmpleosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMisEmpleosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        cargarDatos()
    }

    private fun setupRecyclerView() {
        adapter = MisEmpleosAdapter { empleo ->
            // Ir a ver postulantes del empleo
            val intent = Intent(this, PostulantesActivity::class.java)
            intent.putExtra("EMPLEO_ID", empleo.id)
            intent.putExtra("NOMBRE_EMPLEO", empleo.nombreEmpleo)
            startActivity(intent)
        }

        binding.rvMisEmpleos.layoutManager = LinearLayoutManager(this)
        binding.rvMisEmpleos.adapter = adapter
    }

    private fun setupObservers() {
        empleadorViewModel.estadisticas.observe(this) { stats ->
            binding.tvEmpleosActivos.text = stats.empleosActivos.toString()
            binding.tvEmpleosFinalizados.text = stats.empleosFinalizados.toString()
            binding.tvTotalPostulaciones.text = stats.totalPostulaciones.toString()
            binding.tvNuevasPostulaciones.text = "+${stats.nuevasPostulaciones}"
        }

        empleadorViewModel.misEmpleos.observe(this) { empleos ->
            if (empleos.isEmpty()) {
                binding.layoutEmptyState.visibility = View.VISIBLE
                binding.rvMisEmpleos.visibility = View.GONE
            } else {
                binding.layoutEmptyState.visibility = View.GONE
                binding.rvMisEmpleos.visibility = View.VISIBLE
                adapter.submitList(empleos)
            }
        }

        empleadorViewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            if (!isLoading) {
                binding.swipeRefresh.isRefreshing = false
            }
        }

        empleadorViewModel.error.observe(this) { errorMsg ->
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.swipeRefresh.setOnRefreshListener {
            cargarDatos()
        }
    }

    private fun cargarDatos() {
        lifecycleScope.launch {
            val token = tokenManager.getToken().first()
            if (token != null) {
                empleadorViewModel.cargarEstadisticas(token)
                empleadorViewModel.cargarMisEmpleos(token)
            } else {
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        cargarDatos()
    }
}