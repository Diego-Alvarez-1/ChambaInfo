package com.chambainfo.app.ui.empleador

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.chambainfo.app.data.local.TokenManager
import com.chambainfo.app.data.model.PostulacionResponse
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

    private val perfilLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            cargarPostulaciones()
        }
    }

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
                abrirPerfilTrabajador(postulacion)
            }
        )

        binding.rvPostulaciones.layoutManager = LinearLayoutManager(this)
        binding.rvPostulaciones.adapter = adapter
    }

    private fun setupObservers() {
        postulacionViewModel.misPostulaciones.observe(this) { todasPostulaciones ->
            // Filtrar solo postulaciones NO archivadas
            val postulacionesActivas = todasPostulaciones.filter {
                it.estado != "ARCHIVADO"
            }

            if (postulacionesActivas.isEmpty()) {
                binding.layoutEmptyState.visibility = View.VISIBLE
                binding.rvPostulaciones.visibility = View.GONE
            } else {
                binding.layoutEmptyState.visibility = View.GONE
                binding.rvPostulaciones.visibility = View.VISIBLE
                adapter.submitList(postulacionesActivas)
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
            val numeroLimpio = celular.replace(Regex("[^0-9]"), "")
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://wa.me/51$numeroLimpio")
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No se pudo abrir WhatsApp", Toast.LENGTH_SHORT).show()
        }
    }

    private fun abrirPerfilTrabajador(postulacion: PostulacionResponse) {
        val intent = Intent(this, PerfilTrabajadorActivity::class.java)
        intent.putExtra("TRABAJADOR_ID", postulacion.trabajadorId)
        intent.putExtra("POSTULACION_ID", postulacion.id)
        intent.putExtra("TRABAJADOR_NOMBRE", postulacion.trabajadorNombre)
        intent.putExtra("TRABAJADOR_DNI", postulacion.trabajadorDni)
        intent.putExtra("TRABAJADOR_CELULAR", postulacion.trabajadorCelular)
        startActivity(intent)
    }
}