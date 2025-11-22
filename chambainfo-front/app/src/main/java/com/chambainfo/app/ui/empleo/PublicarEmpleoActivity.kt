package com.chambainfo.app.ui.empleo

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.chambainfo.app.data.local.TokenManager
import com.chambainfo.app.data.model.PublicarEmpleoRequest
import com.chambainfo.app.data.model.TipoNotificacion
import com.chambainfo.app.databinding.ActivityPublicarEmpleoBinding
import com.chambainfo.app.utils.NotificacionManager
import com.chambainfo.app.viewmodel.EmpleoViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PublicarEmpleoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPublicarEmpleoBinding
    private val empleoViewModel: EmpleoViewModel by viewModels()
    private lateinit var tokenManager: TokenManager
    private lateinit var notificacionManager: NotificacionManager

    /**
     * Inicializa la actividad de publicar empleo y configura los componentes principales.
     *
     * @param savedInstanceState El estado guardado de la actividad, si existe.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPublicarEmpleoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)
        notificacionManager = NotificacionManager(this)

        setupObservers()
        setupClickListeners()

        // Cargar celular del usuario
        cargarDatosUsuario()
    }

    /**
     * Carga los datos del usuario desde el almacenamiento local.
     */
    private fun cargarDatosUsuario() {
        lifecycleScope.launch {
            val celular = tokenManager.getCelular().first()
            celular?.let {
                binding.etCelular.setText(it)
            }
        }
    }

    /**
     * Configura los observadores para los LiveData del ViewModel.
     */
    private fun setupObservers() {
        empleoViewModel.publicarResult.observe(this) { result ->
            result.onSuccess { empleo ->
                // Agregar notificación
                lifecycleScope.launch {
                    notificacionManager.agregarNotificacion(
                        tipo = TipoNotificacion.EMPLEO_PUBLICADO,
                        titulo = "Empleo publicado",
                        mensaje = "Tu empleo \"${empleo.nombreEmpleo}\" ha sido publicado exitosamente",
                        empleoId = empleo.id
                    )
                }

                mostrarDialogoExito()
            }

            result.onFailure { error ->
                Toast.makeText(
                    this,
                    error.message ?: "Error al publicar empleo",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        empleoViewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnPublicar.isEnabled = !isLoading
        }
    }

    /**
     * Configura los listeners de clic para los botones.
     */
    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnPublicar.setOnClickListener {
            if (validarFormulario()) {
                publicarEmpleo()
            }
        }
    }

    /**
     * Valida todos los campos del formulario de publicación de empleo.
     *
     * @return true si todos los campos son válidos, false en caso contrario.
     */
    private fun validarFormulario(): Boolean {
        val nombreEmpleo = binding.etNombreEmpleo.text.toString().trim()
        val descripcion = binding.etDescripcion.text.toString().trim()
        val celular = binding.etCelular.text.toString().trim()

        if (nombreEmpleo.isEmpty()) {
            binding.etNombreEmpleo.error = "El nombre del empleo es obligatorio"
            return false
        }

        if (descripcion.isEmpty()) {
            binding.etDescripcion.error = "La descripción es obligatoria"
            return false
        }

        if (celular.isEmpty() || celular.length != 9) {
            binding.etCelular.error = "El celular debe tener 9 dígitos"
            return false
        }

        return true
    }

    /**
     * Publica un nuevo empleo en el sistema con los datos del formulario.
     */
    private fun publicarEmpleo() {
        lifecycleScope.launch {
            val token = tokenManager.getToken().first()

            if (token == null) {
                Toast.makeText(
                    this@PublicarEmpleoActivity,
                    "Debes iniciar sesión para publicar empleos",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
                return@launch
            }

            val request = PublicarEmpleoRequest(
                nombreEmpleo = binding.etNombreEmpleo.text.toString().trim(),
                descripcionEmpleo = binding.etDescripcion.text.toString().trim(),
                celularContacto = binding.etCelular.text.toString().trim(),
                mostrarNumero = binding.switchMostrarNumero.isChecked,
                ubicacion = binding.etUbicacion.text.toString().trim().ifEmpty { null },
                salario = binding.etSalario.text.toString().trim().ifEmpty { null },
                ruc = binding.etRuc.text.toString().trim().ifEmpty { null }
            )

            empleoViewModel.publicarEmpleo(token, request)
        }
    }

    /**
     * Muestra un diálogo de éxito después de publicar el empleo.
     */
    private fun mostrarDialogoExito() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("¡Operación exitosa!")
            .setMessage("Empleo publicado correctamente\n\nTu oferta ya está visible para todos los trabajadores. Te notificaremos cuando recibas postulaciones.")
            .setPositiveButton("Entendido") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setCancelable(false)
            .create()

        dialog.show()
    }
}