package com.chambainfo.app.ui.empleador

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.chambainfo.app.data.local.TokenManager
import com.chambainfo.app.databinding.ActivityPerfilTrabajadorBinding
import kotlinx.coroutines.launch

class PerfilTrabajadorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilTrabajadorBinding
    private lateinit var tokenManager: TokenManager
    private var trabajadorId: Long = 0
    private var postulacionId: Long = 0
    private var nombreTrabajador: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilTrabajadorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        trabajadorId = intent.getLongExtra("TRABAJADOR_ID", 0)
        postulacionId = intent.getLongExtra("POSTULACION_ID", 0)
        nombreTrabajador = intent.getStringExtra("TRABAJADOR_NOMBRE") ?: ""
        val dni = intent.getStringExtra("TRABAJADOR_DNI") ?: ""
        val celular = intent.getStringExtra("TRABAJADOR_CELULAR") ?: ""

        binding.tvNombreCompleto.text = nombreTrabajador
        binding.tvDni.text = dni
        binding.tvCelular.text = "+51 $celular"

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnArchivar.setOnClickListener {
            mostrarDialogoArchivar()
        }
    }

    private fun mostrarDialogoArchivar() {
        AlertDialog.Builder(this)
            .setTitle("Archivar postulación")
            .setMessage("¿Deseas archivar esta postulación?\n\nPodrás verla en el apartado de archivadas más adelante.")
            .setPositiveButton("Archivar") { _, _ ->
                archivarPostulacion()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun archivarPostulacion() {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE

                // Simular delay de red
                kotlinx.coroutines.delay(500)

                // TODO: Cuando tengas el endpoint, descomenta esto:
                /*
                val token = tokenManager.getToken().first()
                val response = RetrofitClient.apiService.actualizarEstadoPostulacion(
                    "Bearer $token",
                    postulacionId,
                    mapOf("estado" to "ARCHIVADO")
                )

                if (!response.isSuccessful) {
                    throw Exception("Error al archivar")
                }
                */

                Toast.makeText(
                    this@PerfilTrabajadorActivity,
                    "Postulación archivada exitosamente",
                    Toast.LENGTH_SHORT
                ).show()

                setResult(RESULT_OK)
                finish()
            } catch (e: Exception) {
                Toast.makeText(
                    this@PerfilTrabajadorActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
}