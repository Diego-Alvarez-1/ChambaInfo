package com.chambainfo.app.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.chambainfo.app.data.api.RetrofitClient
import com.chambainfo.app.data.local.TokenManager
import com.chambainfo.app.data.model.ActualizarPerfilRequest
import com.chambainfo.app.databinding.ActivityPerfilBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilBinding
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        setupClickListeners()
        cargarDatosUsuario()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnVerDocumentos.setOnClickListener {
            startActivity(Intent(this, VerDocumentosActivity::class.java))
        }

        binding.btnGuardarCambios.setOnClickListener {
            guardarCambiosPerfil()
        }
    }

    private fun cargarDatosUsuario() {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE

                val nombre = tokenManager.getNombre().first()
                val dni = tokenManager.getDni().first()
                val celular = tokenManager.getCelular().first()
                val token = tokenManager.getToken().first()

                binding.tvNombreCompleto.text = nombre ?: "Usuario"
                binding.tvDni.text = dni ?: "-"
                binding.tvCelular.text = celular?.let { "+51 $it" } ?: "-"

                // Cargar datos adicionales desde el servidor
                if (token != null) {
                    val response = RetrofitClient.apiService.obtenerPerfil("Bearer $token")
                    if (response.isSuccessful && response.body() != null) {
                        val usuario = response.body()!!
                        binding.etEmail.setText(usuario.email ?: "")
                        binding.etHabilidades.setText(usuario.habilidades ?: "")
                        binding.etExperiencia.setText(usuario.experienciaLaboral ?: "")
                    }
                }

            } catch (e: Exception) {
                // Corregido: Se agregó .show() y se usó this@PerfilActivity para el contexto correcto.
                Toast.makeText(this@PerfilActivity, "Error al cargar perfil: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun guardarCambiosPerfil() {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                binding.btnGuardarCambios.isEnabled = false

                val token = tokenManager.getToken().first()
                if (token == null) {
                    Toast.makeText(this@PerfilActivity, "Sesión expirada", Toast.LENGTH_SHORT).show()
                    finish()
                    return@launch
                }

                val email = binding.etEmail.text.toString().trim().ifEmpty { null }
                val habilidades = binding.etHabilidades.text.toString().trim().ifEmpty { null }
                val experiencia = binding.etExperiencia.text.toString().trim().ifEmpty { null }

                val request = ActualizarPerfilRequest(
                    email = email,
                    habilidades = habilidades,
                    experienciaLaboral = experiencia
                )

                val response = RetrofitClient.apiService.actualizarPerfil("Bearer $token", request)

                if (response.isSuccessful) {
                    Toast.makeText(this@PerfilActivity, "Perfil actualizado exitosamente", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@PerfilActivity, "Error al actualizar perfil", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@PerfilActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnGuardarCambios.isEnabled = true
            }
        }
    }
}