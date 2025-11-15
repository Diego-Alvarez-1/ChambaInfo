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
import com.chambainfo.app.viewmodel.PostulacionViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DetalleEmpleoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleEmpleoBinding
    private val empleoViewModel: EmpleoViewModel by viewModels()
    private val postulacionViewModel: PostulacionViewModel by viewModels()
    private lateinit var tokenManager: TokenManager
    private var empleoId: Long = 0
    private var nombreEmpleo: String = ""
    private var mostrarNumero: Boolean = true

    /**
     * Inicializa la actividad de detalle de empleo y carga los datos.
     *
     * @param savedInstanceState El estado guardado de la actividad, si existe.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleEmpleoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)
        empleoId = intent.getLongExtra("EMPLEO_ID", 0)

        setupObservers()
        setupClickListeners()

        if (empleoId > 0) {
            empleoViewModel.cargarEmpleoPorId(empleoId)
        }

        verificarSesion()
    }

    /**
     * Verifica si el usuario tiene una sesión activa y configura la interfaz en consecuencia.
     */
    private fun verificarSesion() {
        lifecycleScope.launch {
            val token = tokenManager.getToken().first()

            if (token == null) {
                binding.cardAlertaRegistro.visibility = View.VISIBLE
                binding.btnCrearCuenta.visibility = View.VISIBLE
                binding.tvInfoContacto.visibility = View.GONE
                binding.layoutBotonesContacto.visibility = View.GONE
                binding.btnPostular.visibility = View.GONE
            } else {
                binding.cardAlertaRegistro.visibility = View.GONE
                binding.btnCrearCuenta.visibility = View.GONE

                postulacionViewModel.verificarYaPostulo(token, empleoId)
            }
        }
    }

    /**
     * Configura los observadores para los LiveData de los ViewModels.
     */
    private fun setupObservers() {
        empleoViewModel.empleoDetalle.observe(this) { empleo ->
            nombreEmpleo = empleo.nombreEmpleo
            mostrarNumero = empleo.mostrarNumero

            binding.tvPuesto.text = empleo.nombreEmpleo
            binding.tvEmpleador.text = "${empleo.empleadorNombre} · ${calcularTiempoTranscurrido(empleo.fechaPublicacion)}"
            binding.tvDescripcion.text = empleo.descripcionEmpleo

            binding.tvCelular.text = if (empleo.mostrarNumero) {
                "+51 ${empleo.celularContacto}"
            } else {
                "Número oculto"
            }

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

            lifecycleScope.launch {
                val token = tokenManager.getToken().first()
                if (token != null) {
                    if (empleo.mostrarNumero) {
                        binding.tvInfoContacto.visibility = View.VISIBLE
                        binding.layoutBotonesContacto.visibility = View.VISIBLE
                        binding.btnPostular.visibility = View.GONE
                    } else {
                        binding.tvInfoContacto.visibility = View.GONE
                        binding.layoutBotonesContacto.visibility = View.GONE
                        binding.btnPostular.visibility = View.VISIBLE
                    }
                }
            }
        }

        postulacionViewModel.yaPostuloResult.observe(this) { yaPostulo ->
            if (yaPostulo) {
                binding.btnPostular.isEnabled = false
                binding.btnPostular.text = "Ya postulaste a este empleo"
                binding.btnPostular.alpha = 0.6f
            }
        }

        empleoViewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        empleoViewModel.error.observe(this) { errorMsg ->
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Configura los listeners de clic para los botones.
     */
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

        binding.btnPostular.setOnClickListener {
            val intent = Intent(this, PostularEmpleoActivity::class.java)
            intent.putExtra("EMPLEO_ID", empleoId)
            intent.putExtra("NOMBRE_EMPLEO", nombreEmpleo)
            intent.putExtra("EMPLEADOR_NOMBRE", empleoViewModel.empleoDetalle.value?.empleadorNombre ?: "")
            startActivity(intent)
        }
    }

    /**
     * Calcula el tiempo transcurrido desde la fecha de publicación hasta ahora.
     *
     * @param fechaString La fecha de publicación en formato String.
     * @return Una cadena que representa el tiempo transcurrido (ej: "hace 2 días").
     */
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

    /**
     * Se ejecuta cuando la actividad vuelve al primer plano.
     * Verifica nuevamente si el usuario ya postuló al empleo.
     */
    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val token = tokenManager.getToken().first()
            if (token != null && empleoId > 0) {
                postulacionViewModel.verificarYaPostulo(token, empleoId)
            }
        }
    }
}