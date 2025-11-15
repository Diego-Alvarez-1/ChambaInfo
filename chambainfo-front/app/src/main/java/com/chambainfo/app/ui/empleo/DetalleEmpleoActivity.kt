package com.chambainfo.app.ui.empleo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.chambainfo.app.R
import com.chambainfo.app.data.local.TokenManager
import com.chambainfo.app.databinding.ActivityDetalleEmpleoBinding
import com.chambainfo.app.ui.auth.LoginActivity
import com.chambainfo.app.viewmodel.EmpleoViewModel
import com.chambainfo.app.viewmodel.PostulacionViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DetalleEmpleoActivity : AppCompatActivity() {

    // Properties
    private lateinit var binding: ActivityDetalleEmpleoBinding
    private lateinit var tokenManager: TokenManager

    private val empleoViewModel: EmpleoViewModel by viewModels()
    private val postulacionViewModel: PostulacionViewModel by viewModels()

    private var empleoId: Long = 0
    private var nombreEmpleo: String = ""
    private var mostrarNumero: Boolean = true

    // Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleEmpleoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeComponents()
        setupObservers()
        setupClickListeners()
        loadEmpleoData()
        verificarSesion()
    }

    override fun onResume() {
        super.onResume()
        verificarPostulacionExistente()
    }

    // Initialization
    private fun initializeComponents() {
        tokenManager = TokenManager(this)
        empleoId = intent.getLongExtra("EMPLEO_ID", 0)
    }

    private fun loadEmpleoData() {
        if (empleoId > 0) {
            empleoViewModel.cargarEmpleoPorId(empleoId)
        }
    }

    // Session Management
    private fun verificarSesion() {
        lifecycleScope.launch {
            val token = tokenManager.getToken().first()

            if (token == null) {
                mostrarVistaNoAutenticado()
            } else {
                ocultarVistaNoAutenticado()
                postulacionViewModel.verificarYaPostulo(token, empleoId)
            }
        }
    }

    private fun verificarPostulacionExistente() {
        lifecycleScope.launch {
            val token = tokenManager.getToken().first()
            if (token != null && empleoId > 0) {
                postulacionViewModel.verificarYaPostulo(token, empleoId)
            }
        }
    }

    // UI Setup
    private fun setupObservers() {
        observeEmpleoDetalle()
        observePostulacionStatus()
        observeLoadingState()
        observeErrors()
    }

    private fun observeEmpleoDetalle() {
        empleoViewModel.empleoDetalle.observe(this) { empleo ->
            nombreEmpleo = empleo.nombreEmpleo
            mostrarNumero = empleo.mostrarNumero

            actualizarDatosEmpleo(empleo)
            actualizarInfoAdicional(empleo)
            verificarPropietarioEmpleo(empleo)
        }
    }

    private fun actualizarDatosEmpleo(empleo: com.chambainfo.app.data.model.Empleo) {
        with(binding) {
            tvPuesto.text = empleo.nombreEmpleo
            tvEmpleador.text = "${empleo.empleadorNombre} · ${calcularTiempoTranscurrido(empleo.fechaPublicacion)}"
            tvDescripcion.text = empleo.descripcionEmpleo

            tvCelular.text = if (empleo.mostrarNumero) {
                "+51 ${empleo.celularContacto}"
            } else {
                "Número oculto"
            }
        }
    }

    private fun actualizarInfoAdicional(empleo: com.chambainfo.app.data.model.Empleo) {
        val infoAdicional = buildString {
            empleo.ubicacion?.let { append("• Ubicación: $it\n") }
            empleo.salario?.let { append("• Salario: $it\n") }
        }

        with(binding) {
            if (infoAdicional.isNotEmpty()) {
                tvInfoAdicional.text = infoAdicional.trim()
                tvInfoAdicionalLabel.visibility = View.VISIBLE
                tvInfoAdicional.visibility = View.VISIBLE
            } else {
                tvInfoAdicionalLabel.visibility = View.GONE
                tvInfoAdicional.visibility = View.GONE
            }
        }
    }

    private fun verificarPropietarioEmpleo(empleo: com.chambainfo.app.data.model.Empleo) {
        lifecycleScope.launch {
            val token = tokenManager.getToken().first()
            val userId = tokenManager.getUserId().first()

            when {
                userId == empleo.empleadorId -> mostrarVistaPropietario()
                token != null -> configurarVistaUsuarioAutenticado(empleo.mostrarNumero)
            }
        }
    }

    private fun observePostulacionStatus() {
        postulacionViewModel.yaPostuloResult.observe(this) { yaPostulo ->
            if (yaPostulo) {
                deshabilitarBotonPostular()
            }
        }
    }

    private fun observeLoadingState() {
        empleoViewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun observeErrors() {
        empleoViewModel.error.observe(this) { errorMsg ->
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
        }
    }

    // Click Listeners
    private fun setupClickListeners() {
        with(binding) {
            btnBack.setOnClickListener { finish() }
            btnCrearCuenta.setOnClickListener { navegarALogin() }
            btnWhatsApp.setOnClickListener { abrirWhatsApp() }
            btnTelegram.setOnClickListener { mostrarMensajeTelegram() }
            btnPostular.setOnClickListener { navegarAPostulacion() }
        }
    }

    // UI State Management
    private fun mostrarVistaNoAutenticado() {
        with(binding) {
            cardAlertaRegistro.visibility = View.VISIBLE
            btnCrearCuenta.visibility = View.VISIBLE
            tvInfoContacto.visibility = View.GONE
            layoutBotonesContacto.visibility = View.GONE
            btnPostular.visibility = View.GONE
        }
    }

    private fun ocultarVistaNoAutenticado() {
        with(binding) {
            cardAlertaRegistro.visibility = View.GONE
            btnCrearCuenta.visibility = View.GONE
        }
    }

    private fun mostrarVistaPropietario() {
        with(binding) {
            btnPostular.visibility = View.GONE
            tvInfoContacto.visibility = View.GONE
            layoutBotonesContacto.visibility = View.GONE

            cardAlertaRegistro.visibility = View.VISIBLE
            cardAlertaRegistro.removeAllViews()

            val textView = TextView(this@DetalleEmpleoActivity).apply {
                text = "Este es tu empleo. No puedes postular a tus propias ofertas."
                setPadding(16, 16, 16, 16)
                setTextColor(resources.getColor(R.color.primary_blue_dark, theme))
            }
            cardAlertaRegistro.addView(textView)
        }
    }

    private fun configurarVistaUsuarioAutenticado(mostrarNumero: Boolean) {
        with(binding) {
            if (mostrarNumero) {
                tvInfoContacto.visibility = View.VISIBLE
                layoutBotonesContacto.visibility = View.VISIBLE
                btnPostular.visibility = View.GONE
            } else {
                tvInfoContacto.visibility = View.GONE
                layoutBotonesContacto.visibility = View.GONE
                btnPostular.visibility = View.VISIBLE
            }
        }
    }

    private fun deshabilitarBotonPostular() {
        with(binding.btnPostular) {
            isEnabled = false
            text = "Ya postulaste a este empleo"
            alpha = 0.6f
        }
    }

    // Navigation
    private fun navegarALogin() {
        startActivity(Intent(this, LoginActivity::class.java))
    }

    private fun navegarAPostulacion() {
        val intent = Intent(this, PostularEmpleoActivity::class.java).apply {
            putExtra("EMPLEO_ID", empleoId)
            putExtra("NOMBRE_EMPLEO", nombreEmpleo)
            putExtra("EMPLEADOR_NOMBRE", empleoViewModel.empleoDetalle.value?.empleadorNombre ?: "")
        }
        startActivity(intent)
    }

    // External Actions
    private fun abrirWhatsApp() {
        val celular = empleoViewModel.empleoDetalle.value?.celularContacto
        if (celular != null && celular != "Número oculto") {
            abrirWhatsApp(celular)
        } else {
            Toast.makeText(this, "Número no disponible", Toast.LENGTH_SHORT).show()
        }
    }

    private fun abrirWhatsApp(celular: String) {
        try {
            val numeroLimpio = celular.replace(Regex("[^0-9]"), "")
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://wa.me/51$numeroLimpio")
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No se pudo abrir WhatsApp", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarMensajeTelegram() {
        Toast.makeText(this, "Funcionalidad de Telegram próximamente", Toast.LENGTH_SHORT).show()
    }

    // Utilities
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