package com.chambainfo.app.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.chambainfo.app.data.local.TokenManager
import com.chambainfo.app.databinding.ActivityPerfilBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilBinding
    private lateinit var tokenManager: TokenManager

    /**
     * Inicializa la actividad de perfil y configura los componentes principales.
     *
     * @param savedInstanceState El estado guardado de la actividad, si existe.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        setupClickListeners()
        cargarDatosUsuario()
    }

    /**
     * Configura los listeners de clic para los botones.
     */
    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnVerDocumentos.setOnClickListener {
            startActivity(Intent(this, VerDocumentosActivity::class.java))

        }

        binding.btnGuardarCambios.setOnClickListener {
            // Funcionalidad futura
        }
    }

    /**
     * Carga los datos del usuario desde el almacenamiento local y los muestra en la interfaz.
     */
    private fun cargarDatosUsuario() {
        lifecycleScope.launch {
            val nombre = tokenManager.getNombre().first()
            val dni = tokenManager.getDni().first()
            val celular = tokenManager.getCelular().first()

            // Mostrar datos
            binding.tvNombreCompleto.text = nombre ?: "Usuario"
            binding.tvDni.text = dni ?: "-"
            binding.tvCelular.text = celular?.let { "+51 $it" } ?: "-"
            binding.tvEmail.text = "-" // Por ahora no guardamos email
            binding.tvHabilidades.text = "-"
            binding.tvExperiencia.text = "-"
        }
    }
}