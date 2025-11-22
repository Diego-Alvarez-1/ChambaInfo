package com.chambainfo.app.ui.empleador

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.chambainfo.app.data.local.TokenManager
import com.chambainfo.app.data.model.PostulacionResponse
import com.chambainfo.app.databinding.ActivityPostulacionesEmpleoBinding
import com.chambainfo.app.data.api.RetrofitClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PostulacionesEmpleoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostulacionesEmpleoBinding
    private lateinit var tokenManager: TokenManager
    private var empleoId: Long = 0
    private var nombreEmpleo: String = ""
    private val postulaciones = mutableListOf<PostulacionResponse>()
    private val postulacionesArchivadas = mutableListOf<Long>()
    private lateinit var adapter: PostulacionesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostulacionesEmpleoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)
        empleoId = intent.getLongExtra("EMPLEO_ID", 0)
        nombreEmpleo = intent.getStringExtra("NOMBRE_EMPLEO") ?: ""

        binding.tvNombreEmpleoHeader.text = nombreEmpleo

        setupRecyclerView()
        setupClickListeners()
        cargarPostulaciones()
    }

    private fun setupRecyclerView() {
        adapter = PostulacionesAdapter(
            postulaciones,
            onArchivarClick = { postulacion ->
                mostrarDialogoArchivar(postulacion)
            }
        )
        binding.rvPostulaciones.layoutManager = LinearLayoutManager(this)
        binding.rvPostulaciones.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnVerArchivadas.setOnClickListener {
            Toast.makeText(this, "Funcionalidad próximamente", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarPostulaciones() {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE

                val token = tokenManager.getToken().first()
                if (token == null) {
                    finish()
                    return@launch
                }

                val response = RetrofitClient.apiService.obtenerPostulacionesPorEmpleo(
                    "Bearer $token",
                    empleoId
                )

                if (response.isSuccessful && response.body() != null) {
                    postulaciones.clear()
                    postulaciones.addAll(
                        response.body()!!.filter { !postulacionesArchivadas.contains(it.id) }
                    )
                    adapter.notifyDataSetChanged()

                    actualizarUI()
                } else {
                    Toast.makeText(
                        this@PostulacionesEmpleoActivity,
                        "Error al cargar postulaciones",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@PostulacionesEmpleoActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun actualizarUI() {
        if (postulaciones.isEmpty()) {
            binding.rvPostulaciones.visibility = View.GONE
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.tvCantidadPostulaciones.text = "0 postulaciones"
        } else {
            binding.rvPostulaciones.visibility = View.VISIBLE
            binding.layoutEmptyState.visibility = View.GONE
            binding.tvCantidadPostulaciones.text = "${postulaciones.size} postulaciones"
        }
    }

    private fun mostrarDialogoArchivar(postulacion: PostulacionResponse) {
        AlertDialog.Builder(this)
            .setTitle("Archivar postulación")
            .setMessage("¿Deseas archivar la postulación de ${postulacion.trabajadorNombre}?\n\nPodrás verla en \"Ver archivadas\".")
            .setPositiveButton("Archivar") { _, _ ->
                archivarPostulacion(postulacion)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun archivarPostulacion(postulacion: PostulacionResponse) {
        postulacionesArchivadas.add(postulacion.id)
        postulaciones.remove(postulacion)
        adapter.notifyDataSetChanged()

        actualizarUI()

        Toast.makeText(this, "Postulación archivada", Toast.LENGTH_SHORT).show()
    }
}