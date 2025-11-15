package com.chambainfo.app.ui.trabajador

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
import com.chambainfo.app.databinding.ActivityMisPostulacionesBinding
import com.chambainfo.app.ui.empleo.DetalleEmpleoActivity
import com.chambainfo.app.viewmodel.EmpleoViewModel
import com.chambainfo.app.viewmodel.PostulacionViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MisPostulacionesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMisPostulacionesBinding
    private val postulacionViewModel: PostulacionViewModel by viewModels()
    private val empleoViewModel: EmpleoViewModel by viewModels()
    private lateinit var tokenManager: TokenManager
    private lateinit var adapter: MisPostulacionesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMisPostulacionesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        cargarMisPostulaciones()
    }

    private fun setupRecyclerView() {
        adapter = MisPostulacionesAdapter(
            onEmpleoClick = { postulacion ->
                val intent = Intent(this, DetalleEmpleoActivity::class.java)
                intent.putExtra("EMPLEO_ID", postulacion.empleoId)
                startActivity(intent)
            },
            onWhatsAppClick = { postulacion ->
                // Cargar el empleo para obtener el número del empleador
                lifecycleScope.launch {
                    empleoViewModel.cargarEmpleoPorId(postulacion.empleoId)
                }
            }
        )

        binding.rvMisPostulaciones.layoutManager = LinearLayoutManager(this)
        binding.rvMisPostulaciones.adapter = adapter
    }

    private fun setupObservers() {
        postulacionViewModel.misPostulaciones.observe(this) { todasPostulaciones ->
            // Filtrar postulaciones activas (menos de 10 días)
            val postulacionesActivas = todasPostulaciones.filter { postulacion ->
                val diasTranscurridos = calcularDiasTranscurridos(postulacion.fechaPostulacion)
                diasTranscurridos < 10
            }

            if (postulacionesActivas.isEmpty()) {
                binding.layoutEmptyState.visibility = View.VISIBLE
                binding.rvMisPostulaciones.visibility = View.GONE
            } else {
                binding.layoutEmptyState.visibility = View.GONE
                binding.rvMisPostulaciones.visibility = View.VISIBLE
                adapter.submitList(postulacionesActivas)
            }

            // Actualizar contador de archivadas
            val archivadas = todasPostulaciones.size - postulacionesActivas.size
            if (archivadas > 0) {
                binding.btnVerArchivadas.visibility = View.VISIBLE
                binding.btnVerArchivadas.text = "Ver archivadas ($archivadas)"
            } else {
                binding.btnVerArchivadas.visibility = View.GONE
            }
        }

        // Observar cuando se carga un empleo para abrir WhatsApp
        empleoViewModel.empleoDetalle.observe(this) { empleo ->
            if (empleo.mostrarNumero) {
                abrirWhatsApp(empleo.celularContacto)
            } else {
                Toast.makeText(
                    this,
                    "El empleador no ha compartido su número de contacto",
                    Toast.LENGTH_LONG
                ).show()
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

        binding.btnVerArchivadas.setOnClickListener {
            Toast.makeText(this, "Postulaciones archivadas próximamente", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarMisPostulaciones() {
        lifecycleScope.launch {
            val token = tokenManager.getToken().first()
            token?.let {
                postulacionViewModel.cargarMisPostulaciones(it)
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

    private fun calcularDiasTranscurridos(fechaString: String): Long {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val fecha = format.parse(fechaString)
            val ahora = Date()
            val diff = ahora.time - (fecha?.time ?: 0)
            diff / (1000 * 60 * 60 * 24)
        } catch (e: Exception) {
            0
        }
    }
}