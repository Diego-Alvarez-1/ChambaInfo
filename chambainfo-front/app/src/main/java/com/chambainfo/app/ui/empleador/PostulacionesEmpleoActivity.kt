package com.chambainfo.app.ui.empleador

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.chambainfo.app.data.local.TokenManager
import com.chambainfo.app.databinding.ActivityPostulacionesEmpleoBinding
import com.chambainfo.app.viewmodel.PostulacionViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PostulacionesEmpleoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostulacionesEmpleoBinding
    private val postulacionViewModel: PostulacionViewModel by viewModels()
    private lateinit var tokenManager: TokenManager
    private lateinit var adapter: PostulacionesAdapter
    private var empleoId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostulacionesEmpleoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)
        empleoId = intent.getLongExtra("EMPLEO_ID", 0)

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        cargarPostulaciones()
    }

    private fun setupRecyclerView() {
        adapter = PostulacionesAdapter(
            onWhatsAppClick = { postulacion ->
                abrirWhatsApp(postulacion.trabajadorCelular)
            },
            onVerPerfilClick = { postulacion ->
                Toast.makeText(this, "Ver perfil próximamente", Toast.LENGTH_SHORT).show()
            }
        )

        binding.rvPostulaciones.layoutManager = LinearLayoutManager(this)
        binding.rvPostulaciones.adapter = adapter
    }

    private fun setupObservers() {
        postulacionViewModel.misPostulaciones.observe(this) { postulaciones ->
            if (postulaciones.isEmpty()) {
                binding.layoutEmptyState.visibility = View.VISIBLE
                binding.rvPostulaciones.visibility = View.GONE
            } else {
                binding.layoutEmptyState.visibility = View.GONE
                binding.rvPostulaciones.visibility = View.VISIBLE
                adapter.submitList(postulaciones)
            }
        }

        postulacionViewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        postulacionViewModel.error.observe(this) { errorMsg ->
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun cargarPostulaciones() {
        lifecycleScope.launch {
            val token = tokenManager.getToken().first()
            token?.let {
                postulacionViewModel.cargarPostulacionesPorEmpleo(it, empleoId)
            }
        }
    }

    private fun abrirWhatsApp(celular: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://wa.me/51$celular")
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No se pudo abrir WhatsApp", Toast.LENGTH_SHORT).show()
        }
    }
}