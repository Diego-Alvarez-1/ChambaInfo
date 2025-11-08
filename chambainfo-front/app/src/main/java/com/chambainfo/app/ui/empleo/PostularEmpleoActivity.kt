package com.chambainfo.app.ui.empleo

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.chambainfo.app.R
import com.chambainfo.app.data.local.TokenManager
import com.chambainfo.app.data.model.PostulacionRequest
import com.chambainfo.app.databinding.ActivityPostularEmpleoBinding
import com.chambainfo.app.viewmodel.PostulacionViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PostularEmpleoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostularEmpleoBinding
    private val postulacionViewModel: PostulacionViewModel by viewModels()
    private lateinit var tokenManager: TokenManager
    private var empleoId: Long = 0
    private var nombreEmpleo: String = ""
    private var empleadorNombre: String = ""

    /**
     * Inicializa la actividad de postular empleo y configura los componentes principales.
     *
     * @param savedInstanceState El estado guardado de la actividad, si existe.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostularEmpleoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        // Obtener datos del intent
        empleoId = intent.getLongExtra("EMPLEO_ID", 0)
        nombreEmpleo = intent.getStringExtra("NOMBRE_EMPLEO") ?: ""
        empleadorNombre = intent.getStringExtra("EMPLEADOR_NOMBRE") ?: ""

        // Mostrar en header
        binding.tvNombreEmpleoHeader.text = "$nombreEmpleo - $empleadorNombre"

        setupObservers()
        setupClickListeners()
        cargarDatosUsuario()
    }

    /**
     * Carga los datos del usuario desde el almacenamiento local.
     */
    private fun cargarDatosUsuario() {
        lifecycleScope.launch {
            // Cargar DNI
            val dni = tokenManager.getDni().first()
            binding.tvDni.text = dni ?: "No disponible"

            // Cargar celular
            val celular = tokenManager.getCelular().first()
            binding.etCelular.setText(celular ?: "")
        }
    }

    /**
     * Configura los observadores para los LiveData del ViewModel.
     */
    private fun setupObservers() {
        postulacionViewModel.postularResult.observe(this) { result ->
            result.onSuccess { postulacion ->
                mostrarDialogoExito()
            }

            result.onFailure { error ->
                Toast.makeText(
                    this,
                    error.message ?: "Error al enviar postulación",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        postulacionViewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnEnviarPostulacion.isEnabled = !isLoading
        }
    }

    /**
     * Configura los listeners de clic para los botones y enlaces.
     */
    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnEnviarPostulacion.setOnClickListener {
            mostrarDialogoConfirmacion()
        }

        binding.tvEnlaceCul.setOnClickListener {
            Toast.makeText(this, "Abriendo empleosperu.gob.pe...", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Muestra un diálogo de confirmación antes de enviar la postulación.
     */
    private fun mostrarDialogoConfirmacion() {
        val mensaje = binding.etMensaje.text.toString().trim()

        // Crear el diálogo personalizado
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enviar postulación para \"$nombreEmpleo\"")
        builder.setMessage("El empleador recibirá tu información de contacto y los documentos adjuntos.")

        builder.setPositiveButton("Confirmar") { dialog, _ ->
            dialog.dismiss()
            enviarPostulacion(mensaje)
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()

        // Personalizar colores de los botones
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(
            resources.getColor(R.color.primary_blue, theme)
        )
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
            resources.getColor(R.color.text_secondary, theme)
        )
    }

    /**
     * Envía la postulación al servidor con los datos del formulario.
     *
     * @param mensaje El mensaje opcional de la postulación.
     */
    private fun enviarPostulacion(mensaje: String) {
        lifecycleScope.launch {
            val token = tokenManager.getToken().first()

            if (token == null) {
                Toast.makeText(
                    this@PostularEmpleoActivity,
                    "Debes iniciar sesión para postular",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
                return@launch
            }

            // Si el mensaje está vacío, usar un mensaje por defecto
            val mensajeFinal = if (mensaje.isEmpty()) {
                "Estoy interesado en este empleo y me gustaría aplicar."
            } else {
                mensaje
            }

            val request = PostulacionRequest(
                empleoId = empleoId,
                mensaje = mensajeFinal
            )

            postulacionViewModel.postular(token, request)
        }
    }

    /**
     * Muestra un diálogo de éxito después de enviar la postulación.
     */
    private fun mostrarDialogoExito() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("¡Postulación enviada!")
            .setMessage("Tu postulación ha sido enviada exitosamente.\n\nEl empleador recibirá tu información y se pondrá en contacto contigo si está interesado.")
            .setPositiveButton("Entendido") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setCancelable(false)
            .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(
            resources.getColor(R.color.primary_blue, theme)
        )
    }
}