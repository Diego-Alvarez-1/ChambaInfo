package com.chambainfo.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.chambainfo.app.data.local.TokenManager
import com.chambainfo.app.data.model.LoginRequest
import com.chambainfo.app.databinding.ActivityLoginBinding
import com.chambainfo.app.ui.MainActivity
import com.chambainfo.app.ui.empleador.EmpleadorDashboardActivity
import com.chambainfo.app.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        authViewModel.loginResult.observe(this) { result ->
            result.onSuccess { authResponse ->
                // Guardar datos de sesión
                lifecycleScope.launch {
                    tokenManager.saveAuthData(
                        token = authResponse.token,
                        userId = authResponse.id,
                        dni = authResponse.dni,
                        nombre = authResponse.nombreCompleto,
                        usuario = authResponse.usuario,
                        celular = authResponse.celular,
                        rol = authResponse.rol ?: "TRABAJADOR"
                    )

                    Toast.makeText(
                        this@LoginActivity,
                        "Bienvenido ${authResponse.nombreCompleto}",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Redirigir según el rol
                    redirigirSegunRol(authResponse.rol)
                }
            }

            result.onFailure { error ->
                Toast.makeText(
                    this,
                    error.message ?: "Error al iniciar sesión",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        authViewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnLogin.isEnabled = !isLoading
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnLogin.setOnClickListener {
            val usuario = binding.etUsuario.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInputs(usuario, password)) {
                val loginRequest = LoginRequest(usuario, password)
                authViewModel.login(loginRequest)
            }
        }

        binding.tvRegistrarse.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.btnEmpleador.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.btnTrabajador.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.tvOlvidastePassword.setOnClickListener {
            Toast.makeText(this, "Funcionalidad próximamente", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInputs(usuario: String, password: String): Boolean {
        if (usuario.isEmpty()) {
            binding.etUsuario.error = "El usuario es obligatorio"
            return false
        }

        if (password.isEmpty()) {
            binding.etPassword.error = "La contraseña es obligatoria"
            return false
        }

        if (password.length < 6) {
            binding.etPassword.error = "La contraseña debe tener al menos 6 caracteres"
            return false
        }

        return true
    }

    private fun redirigirSegunRol(rol: String) {
        val intent = if (rol == "EMPLEADOR") {
            Intent(this, EmpleadorDashboardActivity::class.java)
        } else {
            Intent(this, MainActivity::class.java)
        }

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}