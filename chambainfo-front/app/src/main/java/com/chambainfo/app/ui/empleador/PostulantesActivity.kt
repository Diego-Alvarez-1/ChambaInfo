package com.chambainfo.app.ui.empleador

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.chambainfo.app.R
import com.chambainfo.app.data.local.TokenManager
import com.chambainfo.app.databinding.ActivityPostulantesBinding
import com.chambainfo.app.viewmodel.EmpleadorViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

class PostulantesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostulantesBinding
    private val empleadorViewModel: EmpleadorViewModel by viewModels()
    private lateinit var tokenManager: TokenManager
    private lateinit var adapter: PostulantesAdapter
    private var empleoId: Long = 0
    private var nombreEmpleo: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostulantesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)
        empleoId = intent.getLongExtra("EMPLEO_ID", 0)
        nombreEmpleo = intent.getStringExtra("NOMBRE_EMPLEO") ?: ""

        binding.tvNombreEmpleo.text = nombreEmpleo

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        cargarPostulantes()
    }

    private fun setupRecyclerView() {
        adapter = PostulantesAdapter(
            onWhatsAppClick = { postulante ->
                abrirWhatsApp(postulante.celular, postulante.nombreCompleto)
            },
            onCambiarEstadoClick = { postulante ->
                mostrarDialogoCambiarEstado(postulante.postulacionId, postulante.estado)
            }
        )

        binding.rvPostulantes.layoutManager = LinearLayoutManager(this)
        binding.rvPostulantes.adapter = adapter
    }

    private fun setupObservers() {
        empleadorViewModel.postulantes.observe(this) { postulantes ->
            if (postulantes.isEmpty()) {
                binding.layoutEmptyState.visibility = View.VISIBLE
                binding.rvPostulantes.visibility = View.GONE
            } else {
                binding.layoutEmptyState.visibility = View.GONE
                binding.rvPostulantes.visibility = View.VISIBLE
                adapter.submitList(postulantes)
                binding.tvCantidadPostulantes.text = "${postulantes.size} postulantes"
            }
        }

        empleadorViewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
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
            cargarPostulantes()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun cargarPostulantes() {
        lifecycleScope.launch {
            val token = tokenManager.getToken().first()
            if (token != null && empleoId > 0) {
                empleadorViewModel.cargarPostulantes(token, empleoId)
            } else {
                finish()
            }
        }
    }

    /**
     * 🔥 MÉTODO SIMPLIFICADO: Abre WhatsApp directamente sin pasar por el backend
     */
    private fun abrirWhatsApp(celular: String, nombreCompleto: String) {
        try {
            // Limpiar el número (quitar espacios, guiones, etc.)
            val numeroLimpio = celular.replace(Regex("[^0-9]"), "")
            
            // Crear el mensaje personalizado
            val mensaje = "Hola $nombreCompleto, vi tu postulación para \"$nombreEmpleo\" en ChambaInfo"
            
            // Codificar el mensaje para URL
            val mensajeCodificado = try {
                URLEncoder.encode(mensaje, "UTF-8")
            } catch (e: UnsupportedEncodingException) {
                mensaje // Si falla, usar sin codificar
            }
            
            // Construir el enlace de WhatsApp
            // Formato: https://wa.me/51987654321?text=Mensaje
            val enlaceWhatsApp = "https://wa.me/51$numeroLimpio?text=$mensajeCodificado"
            
            // Abrir WhatsApp con un Intent
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(enlaceWhatsApp)
            
            // Verificar si WhatsApp está instalado
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                // Si no está instalado, mostrar opciones
                mostrarOpcionesWhatsApp(enlaceWhatsApp)
            }
            
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Error al abrir WhatsApp: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Muestra opciones si WhatsApp no está instalado
     */
    private fun mostrarOpcionesWhatsApp(enlace: String) {
        AlertDialog.Builder(this)
            .setTitle("WhatsApp no encontrado")
            .setMessage("No se detectó WhatsApp en tu dispositivo. ¿Qué deseas hacer?")
            .setPositiveButton("Abrir en navegador") { _, _ ->
                try {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(enlace)
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(
                        this,
                        "No se pudo abrir el enlace",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoCambiarEstado(postulacionId: Long, estadoActual: String) {
        val estados = arrayOf("PENDIENTE", "CONTACTADO", "RECHAZADO")
        val estadosDisplay = arrayOf("Pendiente", "Contactado", "Rechazado")
        val seleccionActual = estados.indexOf(estadoActual)

        AlertDialog.Builder(this)
            .setTitle("Cambiar estado")
            .setSingleChoiceItems(estadosDisplay, seleccionActual) { dialog, which ->
                val nuevoEstado = estados[which]
                lifecycleScope.launch {
                    val token = tokenManager.getToken().first()
                    if (token != null) {
                        empleadorViewModel.cambiarEstadoPostulacion(token, postulacionId, nuevoEstado)
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}