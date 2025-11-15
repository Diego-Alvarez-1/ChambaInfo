package com.chambainfo.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.chambainfo.app.R
import com.chambainfo.app.data.local.TokenManager
import com.chambainfo.app.data.model.RegisterRequest
import com.chambainfo.app.databinding.ActivityRegisterBinding
import com.chambainfo.app.ui.MainActivity
import com.chambainfo.app.ui.empleador.EmpleadorDashboardActivity
import com.chambainfo.app.viewmodel.AuthViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var tokenManager: TokenManager

    private var dniVerificado = false
    private var nombresReniec = ""
    private var apellidoPaternoReniec = ""
    private var apellidoMaternoReniec = ""
    private var nombreCompletoReniec = ""
    private var rolSeleccionado = "TRABAJADOR" // Por defecto TRABAJADOR

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        setupTerminosText()
        setupObservers()
        setupClickListeners()
        setupRolButtons()
    }

    private fun setupRolButtons() {
        // Por defecto seleccionar TRABAJADOR
        actualizarEstadoBotonRol(true)

        binding.btnTrabajador.setOnClickListener {
            rolSeleccionado = "TRABAJADOR"
            actualizarEstadoBotonRol(true)
        }

        binding.btnEmpleador.setOnClickListener {
            rolSeleccionado = "EMPLEADOR"
            actualizarEstadoBotonRol(false)
        }
    }

    private fun actualizarEstadoBotonRol(esTrabajador: Boolean) {
        if (esTrabajador) {
            // Trabajador seleccionado
            binding.btnTrabajador.setBackgroundColor(
                ContextCompat.getColor(this, R.color.primary_blue)
            )
            binding.btnTrabajador.setTextColor(
                ContextCompat.getColor(this, R.color.white)
            )

            binding.btnEmpleador.setBackgroundColor(
                ContextCompat.getColor(this, R.color.white)
            )
            binding.btnEmpleador.setTextColor(
                ContextCompat.getColor(this, R.color.primary_blue)
            )
        } else {
            // Empleador seleccionado
            binding.btnEmpleador.setBackgroundColor(
                ContextCompat.getColor(this, R.color.primary_blue)
            )
            binding.btnEmpleador.setTextColor(
                ContextCompat.getColor(this, R.color.white)
            )

            binding.btnTrabajador.setBackgroundColor(
                ContextCompat.getColor(this, R.color.white)
            )
            binding.btnTrabajador.setTextColor(
                ContextCompat.getColor(this, R.color.primary_blue)
            )
        }
    }

    private fun setupTerminosText() {
        val textoCompleto = "Al registrarte aceptas los Términos y Condiciones"
        val spannable = SpannableString(textoCompleto)

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@RegisterActivity, TermsActivity::class.java))
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(this@RegisterActivity, R.color.primary_blue)
                ds.isUnderlineText = true
            }
        }

        val inicio = textoCompleto.indexOf("Términos y Condiciones")
        spannable.setSpan(clickableSpan, inicio, textoCompleto.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.tvTerminos.text = spannable
        binding.tvTerminos.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun setupObservers() {
        authViewModel.verificarDniResult.observe(this) { result ->
            result.onSuccess { reniecData ->
                dniVerificado = true

                nombresReniec = reniecData.firstName
                apellidoPaternoReniec = reniecData.firstLastName
                apellidoMaternoReniec = reniecData.secondLastName
                nombreCompletoReniec = reniecData.fullName

                binding.tvVerificado.visibility = View.VISIBLE
                binding.tvDatosReniec.visibility = View.VISIBLE

                binding.etNombres.setText(nombresReniec)
                binding.etApellidos.setText("$apellidoPaternoReniec $apellidoMaternoReniec")

                binding.btnCrearCuenta.isEnabled = true

                Toast.makeText(this, "DNI verificado correctamente", Toast.LENGTH_SHORT).show()
            }

            result.onFailure { error ->
                dniVerificado = false
                binding.tvVerificado.visibility = View.GONE
                binding.tvDatosReniec.visibility = View.GONE
                binding.btnCrearCuenta.isEnabled = false

                Toast.makeText(
                    this,
                    "Error: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        authViewModel.registerResult.observe(this) { result ->
            result.onSuccess { authResponse ->
                lifecycleScope.launch {
                    tokenManager.saveAuthData(
                        token = authResponse.token,
                        userId = authResponse.id,
                        dni = authResponse.dni,
                        nombre = authResponse.nombreCompleto,
                        usuario = authResponse.usuario,
                        celular = authResponse.celular,
                        rol = authResponse.rol
                    )

                    mostrarDialogoExito()
                }
            }

            result.onFailure { error ->
                val mensaje = when {
                    error.message?.contains("contraseñas no coinciden") == true ->
                        "Las contraseñas no coinciden"
                    error.message?.contains("ya está registrado") == true ->
                        "El DNI o usuario ya está registrado"
                    else -> error.message ?: "Error al crear cuenta"
                }

                if (mensaje == "Las contraseñas no coinciden") {
                    mostrarDialogoError(mensaje)
                } else {
                    Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
                }
            }
        }

        authViewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnCrearCuenta.isEnabled = !isLoading && dniVerificado
            binding.btnVerificar.isEnabled = !isLoading
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnVerificar.setOnClickListener {
            val dni = binding.etDni.text.toString().trim()

            if (dni.length != 8) {
                Toast.makeText(this, "El DNI debe tener 8 dígitos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authViewModel.verificarDni(dni)
        }

        binding.btnCrearCuenta.setOnClickListener {
            if (validarFormulario()) {
                registrarUsuario()
            }
        }
    }

    private fun validarFormulario(): Boolean {
        val dni = binding.etDni.text.toString().trim()
        val celular = binding.etCelular.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        if (!dniVerificado) {
            Toast.makeText(this, "Debes verificar tu DNI primero", Toast.LENGTH_SHORT).show()
            return false
        }

        if (celular.length != 9) {
            binding.etCelular.error = "El celular debe tener 9 dígitos"
            return false
        }

        if (password.length < 6) {
            binding.etPassword.error = "La contraseña debe tener al menos 6 caracteres"
            return false
        }

        if (password != confirmPassword) {
            mostrarDialogoError("Las contraseñas no coinciden")
            return false
        }

        return true
    }

    private fun registrarUsuario() {
        val dni = binding.etDni.text.toString().trim()
        val celular = binding.etCelular.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        val usuario = nombresReniec.lowercase().replace(" ", ".") + "." +
                apellidoPaternoReniec.lowercase()

        val registerRequest = RegisterRequest(
            dni = dni,
            usuario = usuario,
            password = password,
            confirmPassword = confirmPassword,
            celular = celular,
            rol = rolSeleccionado // NUEVO: incluir el rol
        )

        authViewModel.register(registerRequest)
    }

    private fun mostrarDialogoError(mensaje: String) {
        AlertDialog.Builder(this)
            .setTitle("Error de validación")
            .setMessage("$mensaje\n\nPor favor verifica que ambas contraseñas sean idénticas.")
            .setPositiveButton("Entendido") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun mostrarDialogoExito() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("¡Operación exitosa!")
            .setMessage("Cuenta creada exitosamente\n\nBienvenido a ChambaInfo")
            .setPositiveButton("Entendido") { dialog, _ ->
                dialog.dismiss()
                redirigirSegunRol()
            }
            .setCancelable(false)
            .create()

        dialog.show()
    }

    private fun redirigirSegunRol() {
        lifecycleScope.launch {
            val rol = tokenManager.getRol().first()

            val intent = if (rol == "EMPLEADOR") {
                Intent(this@RegisterActivity, EmpleadorDashboardActivity::class.java)
            } else {
                Intent(this@RegisterActivity, MainActivity::class.java)
            }

            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}