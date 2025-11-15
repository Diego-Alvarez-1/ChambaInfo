package com.chambainfo.app.ui.empleador

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chambainfo.app.data.model.PostulacionResponse
import com.chambainfo.app.databinding.ItemPostulacionBinding
import java.text.SimpleDateFormat
import java.util.*

class PostulacionesAdapter(
    private val onWhatsAppClick: (PostulacionResponse) -> Unit,
    private val onVerPerfilClick: (PostulacionResponse) -> Unit
) : ListAdapter<PostulacionResponse, PostulacionesAdapter.PostulacionViewHolder>(PostulacionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostulacionViewHolder {
        val binding = ItemPostulacionBinding.inflate(
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
        private val binding: ItemPostulacionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(postulacion: PostulacionResponse) {
            binding.tvNombreTrabajador.text = postulacion.trabajadorNombre
            binding.tvEstado.text = postulacion.estado
            binding.tvFecha.text = calcularTiempoTranscurrido(postulacion.fechaPostulacion)
            binding.tvCelular.text = "+51 ${postulacion.trabajadorCelular}"
            binding.tvMensaje.text = "Mensaje: ${postulacion.mensaje}"

            binding.btnResponderWhatsApp.setOnClickListener {
                onWhatsAppClick(postulacion)
            }

            binding.btnVerPerfil.setOnClickListener {
                onVerPerfilClick(postulacion)
            }
        }

        private fun calcularTiempoTranscurrido(fechaString: String): String {
            return try {
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val fecha = format.parse(fechaString)
                val ahora = Date()
                val diff = ahora.time - (fecha?.time ?: 0)

                val minutos = diff / (1000 * 60)
                val horas = diff / (1000 * 60 * 60)
                val dias = diff / (1000 * 60 * 60 * 24)

                when {
                    minutos < 60 -> "hace $minutos min"
                    horas < 24 -> "hace $horas h"
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