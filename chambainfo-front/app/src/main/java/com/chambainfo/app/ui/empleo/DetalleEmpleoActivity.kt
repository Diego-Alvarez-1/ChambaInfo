package com.chambainfo.app.ui.empleo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.chambainfo.app.data.local.TokenManager
import com.chambainfo.app.databinding.ActivityDetalleEmpleoBinding
import com.chambainfo.app.ui.auth.LoginActivity
import com.chambainfo.app.viewmodel.EmpleoViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DetalleEmpleoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleEmpleoBinding
    private val empleoViewModel: EmpleoViewModel by viewModels()
    private lateinit var tokenManager: TokenManager
    private var empleoId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleEmpleoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)
        empleoId = intent.getLongExtra("EMPLEO_ID", 0)

        setupObservers()
        setupClickListeners()

        // Cargar detalle del empleo
        if (empleoId > 0) {
            empleoViewModel.cargarEmpleoPorId(empleoId)
        }

        // Verificar si está logueado
        verificarSesion()
    }

    private fun verificarSesion() {
        lifecycleScope.launch {
            val token = tokenManager.getToken().first()

            if (token == null) {
                // No está logueado
                binding.cardAlertaRegistro.visibility = View.VISIBLE
                binding.btnCrearCuenta.visibility = View.VISIBLE
                binding.tvInfoContacto.visibility = View.GONE
                binding.layoutBotonesContacto.visibility = View.GONE
            } else {
                // Está logueado
                binding.cardAlertaRegistro.visibility = View.GONE
                binding.btnCrearCuenta.visibility = View.GONE
                binding.tvInfoContacto.visibility = View.VISIBLE
                binding.layoutBotonesContacto.visibility = View.VISIBLE
            }
        }
    }

    private fun setupObservers() {
        empleoViewModel.empleoDetalle.observe(this) { empleo ->
            // Llenar datos
            binding.tvPuesto.text = empleo.nombreEmpleo
            binding.tvEmpleador.text = "${empleo.empleadorNombre} · ${calcularTiempoTranscurrido(empleo.fechaPublicacion)}"
            binding.tvDescripcion.text = empleo.descripcionEmpleo

            // Celular
            binding.tvCelular.text = if (empleo.mostrarNumero) {
                "+51 ${empleo.celularContacto}"
            } else {
                "Número oculto"
            }

            // Información adicional
            val infoAdicional = buildString {
                empleo.ubicacion?.let {
                    append("• Ubicación: $it\n")
                }
                empleo.salario?.let {
                    append("• Salario: $it\n")
                }
            }

            if (infoAdicional.isNotEmpty()) {
                binding.tvInfoAdicional.text = infoAdicional.trim()
                binding.tvInfoAdicionalLabel.visibility = View.VISIBLE
                binding.tvInfoAdicional.visibility = View.VISIBLE
            } else {
                binding.tvInfoAdicionalLabel.visibility = View.GONE
                binding.tvInfoAdicional.visibility = View.GONE
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
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnCrearCuenta.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.btnWhatsApp.setOnClickListener {
            Toast.makeText(this, "Funcionalidad de WhatsApp próximamente", Toast.LENGTH_SHORT).show()
        }

        binding.btnTelegram.setOnClickListener {
            Toast.makeText(this, "Funcionalidad de Telegram próximamente", Toast.LENGTH_SHORT).show()
        }
    }

    private fun calcularTiempoTranscurrido(fechaString: String): String {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val fecha = format.parse(fechaString)
            val ahora = Date()
            val diff = ahora.time - (fecha?.time ?: 0)

            val dias = diff / (1000 * 60 * 60 * 24)

            when {
                dias == 0L -> "hoy"
                dias == 1L -> "hace 1 día"
                dias < 7 -> "hace $dias días"
                dias < 30 -> "hace ${dias / 7} semanas"
                else -> "hace ${dias / 30} meses"
            }
        } catch (e: Exception) {
            "recientemente"
        }
    }
}