package com.chambainfo.app.ui.trabajador

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chambainfo.app.data.model.PostulacionResponse
import com.chambainfo.app.databinding.ItemMiPostulacionBinding
import java.text.SimpleDateFormat
import java.util.*

class MisPostulacionesAdapter(
    private val onEmpleoClick: (PostulacionResponse) -> Unit,
    private val onWhatsAppClick: (PostulacionResponse) -> Unit
) : ListAdapter<PostulacionResponse, MisPostulacionesAdapter.PostulacionViewHolder>(PostulacionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostulacionViewHolder {
        val binding = ItemMiPostulacionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostulacionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostulacionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PostulacionViewHolder(
        private val binding: ItemMiPostulacionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(postulacion: PostulacionResponse) {
            binding.tvNombreEmpleo.text = postulacion.nombreEmpleo
            binding.tvFechaPostulacion.text = "Postulaste ${calcularTiempoTranscurrido(postulacion.fechaPostulacion)}"
            binding.tvEstado.text = postulacion.estado
            binding.tvMensaje.text = postulacion.mensaje

            binding.root.setOnClickListener {
                onEmpleoClick(postulacion)
            }

            binding.btnVerDetalle.setOnClickListener {
                onEmpleoClick(postulacion)
            }

            binding.btnContactar.setOnClickListener {
                println("DEBUG: Adapter - Click en contactar para empleo ID: ${postulacion.empleoId}")
                onWhatsAppClick(postulacion)
            }
        }

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
                    else -> "hace ${dias / 7} semanas"
                }
            } catch (e: Exception) {
                "recientemente"
            }
        }
    }

    class PostulacionDiffCallback : DiffUtil.ItemCallback<PostulacionResponse>() {
        override fun areItemsTheSame(oldItem: PostulacionResponse, newItem: PostulacionResponse): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PostulacionResponse, newItem: PostulacionResponse): Boolean {
            return oldItem == newItem
        }
    }
}