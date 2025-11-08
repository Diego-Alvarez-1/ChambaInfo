package com.chambainfo.app.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.chambainfo.app.R
import com.chambainfo.app.data.local.TokenManager
import com.chambainfo.app.databinding.ActivityVerDocumentosBinding
import com.chambainfo.app.data.api.RetrofitClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

/**
 * Activity para visualizar y gestionar los documentos del usuario.
 * Permite subir fotos de DNI (anverso y reverso) y el Certificado Único Laboral (CUL).
 */
class VerDocumentosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerDocumentosBinding
    private lateinit var tokenManager: TokenManager
    private var documentoActual: String? = null

    /**
     * Launcher para seleccionar imágenes del DNI desde la galería.
     */
    private val seleccionarImagenDni = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                subirDocumento(uri, documentoActual ?: "")
            }
        }
    }

    /**
     * Launcher para seleccionar el archivo CUL (PDF o imagen) desde la galería/archivos.
     */
    private val seleccionarArchivoCul = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                subirDocumento(uri, "cul")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerDocumentosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        setupClickListeners()
        cargarDocumentos()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        // DNI Anverso
        binding.btnSubirDniAnverso.setOnClickListener {
            documentoActual = "dni_anverso"
            abrirSelectorImagen()
        }

        binding.btnEliminarDniAnverso.setOnClickListener {
            confirmarEliminacion("dni_anverso", "DNI Anverso")
        }

        // DNI Reverso
        binding.btnSubirDniReverso.setOnClickListener {
            documentoActual = "dni_reverso"
            abrirSelectorImagen()
        }

        binding.btnEliminarDniReverso.setOnClickListener {
            confirmarEliminacion("dni_reverso", "DNI Reverso")
        }

        // CUL
        binding.btnSubirCul.setOnClickListener {
            abrirSelectorArchivoCul()
        }

        binding.btnEliminarCul.setOnClickListener {
            confirmarEliminacion("cul", "CUL")
        }
    }

    private fun abrirSelectorImagen() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        seleccionarImagenDni.launch(intent)
    }

    private fun abrirSelectorArchivoCul() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            val mimeTypes = arrayOf("application/pdf", "image/jpeg", "image/jpg", "image/png")
            putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        }
        seleccionarArchivoCul.launch(intent)
    }

    private fun subirDocumento(uri: Uri, tipoDocumento: String) {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE

                val token = tokenManager.getToken().first()
                if (token == null) {
                    Toast.makeText(
                        this@VerDocumentosActivity,
                        "Debes iniciar sesión",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                    return@launch
                }

                val file = uriToFile(uri)
                val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                val response = when (tipoDocumento) {
                    "dni_anverso" -> RetrofitClient.apiService.subirDniAnverso("Bearer $token", body)
                    "dni_reverso" -> RetrofitClient.apiService.subirDniReverso("Bearer $token", body)
                    "cul" -> RetrofitClient.apiService.subirCUL("Bearer $token", body)
                    else -> throw IllegalArgumentException("Tipo de documento inválido")
                }

                if (response.isSuccessful) {
                    Toast.makeText(
                        this@VerDocumentosActivity,
                        "Documento subido exitosamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    cargarDocumentos()
                } else {
                    Toast.makeText(
                        this@VerDocumentosActivity,
                        "Error al subir documento",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                file.delete()

            } catch (e: Exception) {
                Toast.makeText(
                    this@VerDocumentosActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun cargarDocumentos() {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE

                val token = tokenManager.getToken().first()
                if (token == null) {
                    finish()
                    return@launch
                }

                val response = RetrofitClient.apiService.obtenerMisDocumentos("Bearer $token")

                if (response.isSuccessful && response.body() != null) {
                    val documentos = response.body()!!

                    // DNI Anverso
                    if (documentos["dniAnverso"] != null) {
                        binding.tvEstadoDniAnverso.text = "✓ Subido"
                        binding.tvEstadoDniAnverso.setTextColor(getColor(R.color.accent_green))
                        binding.btnEliminarDniAnverso.visibility = View.VISIBLE
                        binding.btnSubirDniAnverso.text = "Cambiar"
                    } else {
                        binding.tvEstadoDniAnverso.text = "No subido"
                        binding.tvEstadoDniAnverso.setTextColor(getColor(R.color.text_secondary))
                        binding.btnEliminarDniAnverso.visibility = View.GONE
                        binding.btnSubirDniAnverso.text = "Subir"
                    }

                    // DNI Reverso
                    if (documentos["dniReverso"] != null) {
                        binding.tvEstadoDniReverso.text = "✓ Subido"
                        binding.tvEstadoDniReverso.setTextColor(getColor(R.color.accent_green))
                        binding.btnEliminarDniReverso.visibility = View.VISIBLE
                        binding.btnSubirDniReverso.text = "Cambiar"
                    } else {
                        binding.tvEstadoDniReverso.text = "No subido"
                        binding.tvEstadoDniReverso.setTextColor(getColor(R.color.text_secondary))
                        binding.btnEliminarDniReverso.visibility = View.GONE
                        binding.btnSubirDniReverso.text = "Subir"
                    }

                    // CUL
                    if (documentos["cul"] != null) {
                        binding.tvEstadoCul.text = "✓ Subido"
                        binding.tvEstadoCul.setTextColor(getColor(R.color.accent_green))
                        binding.btnEliminarCul.visibility = View.VISIBLE
                        binding.btnSubirCul.text = "Cambiar"
                    } else {
                        binding.tvEstadoCul.text = "No subido"
                        binding.tvEstadoCul.setTextColor(getColor(R.color.text_secondary))
                        binding.btnEliminarCul.visibility = View.GONE
                        binding.btnSubirCul.text = "Subir"
                    }
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@VerDocumentosActivity,
                    "Error al cargar documentos: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun confirmarEliminacion(tipoDocumento: String, nombreDocumento: String) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar documento")
            .setMessage("¿Estás seguro de que deseas eliminar $nombreDocumento?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarDocumento(tipoDocumento)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarDocumento(tipoDocumento: String) {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE

                val token = tokenManager.getToken().first()
                if (token == null) {
                    finish()
                    return@launch
                }

                val response = RetrofitClient.apiService.eliminarDocumento(
                    "Bearer $token",
                    tipoDocumento
                )

                if (response.isSuccessful) {
                    Toast.makeText(
                        this@VerDocumentosActivity,
                        "Documento eliminado exitosamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    cargarDocumentos()
                } else {
                    Toast.makeText(
                        this@VerDocumentosActivity,
                        "Error al eliminar documento",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@VerDocumentosActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream = contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("No se pudo abrir el archivo")

        val extension = contentResolver.getType(uri)?.let { mimeType ->
            when (mimeType) {
                "image/jpeg", "image/jpg" -> ".jpg"
                "image/png" -> ".png"
                "application/pdf" -> ".pdf"
                else -> ".tmp"
            }
        } ?: ".tmp"

        val tempFile = File.createTempFile("upload_", extension, cacheDir)

        FileOutputStream(tempFile).use { outputStream ->
            inputStream.copyTo(outputStream)
        }

        inputStream.close()

        return tempFile
    }
}