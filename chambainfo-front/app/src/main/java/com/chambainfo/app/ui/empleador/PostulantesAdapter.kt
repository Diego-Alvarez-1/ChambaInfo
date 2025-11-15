package com.chambainfo.app.ui.empleador

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chambainfo.app.R
import com.chambainfo.app.data.model.Postulante
import com.chambainfo.app.databinding.ItemPostulanteBinding

class PostulantesAdapter(
    private val onWhatsAppClick: (Postulante) -> Unit,
    private val onCambiarEstadoClick: (Postulante) -> Unit
) : ListAdapter<Postulante, PostulantesAdapter.PostulanteViewHolder>(PostulanteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostulanteViewHolder {
        val binding = ItemPostulanteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostulanteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostulanteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PostulanteViewHolder(
        private val binding: ItemPostulanteBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(postulante: Postulante) {
            binding.tvNombreCompleto.text = postulante.nombreCompleto
            binding.tvTiempoTranscurrido.text = postulante.tiempoTranscurrido
            binding.tvMensaje.text = postulante.mensaje
            binding.tvCelular.text = "+51 ${postulante.celular}"

            // Badge "Nuevo"
            if (postulante.esNuevo) {
                binding.chipNuevo.visibility = android.view.View.VISIBLE
            } else {
                binding.chipNuevo.visibility = android.view.View.GONE
            }

            // Estado
            when (postulante.estado) {
                "PENDIENTE" -> {
                    binding.chipEstado.text = "Pendiente"
                    binding.chipEstado.setChipBackgroundColorResource(R.color.accent_orange)
                }
                "CONTACTADO" -> {
                    binding.chipEstado.text = "Contactado"
                    binding.chipEstado.setChipBackgroundColorResource(R.color.accent_green)
                }
                "RECHAZADO" -> {
                    binding.chipEstado.text = "Rechazado"
                    binding.chipEstado.setChipBackgroundColorResource(R.color.accent_red)
                }
            }

            binding.btnWhatsApp.setOnClickListener {
                onWhatsAppClick(postulante)
            }

            binding.btnCambiarEstado.setOnClickListener {
                onCambiarEstadoClick(postulante)
            }

            binding.root.setOnClickListener {
                // Expandir/contraer mensaje
                if (binding.tvMensaje.maxLines == 2) {
                    binding.tvMensaje.maxLines = Int.MAX_VALUE
                } else {
                    binding.tvMensaje.maxLines = 2
                }
            }
        }
    }

    class PostulanteDiffCallback : DiffUtil.ItemCallback<Postulante>() {
        override fun areItemsTheSame(oldItem: Postulante, newItem: Postulante): Boolean =
            oldItem.postulacionId == newItem.postulacionId

        override fun areContentsTheSame(oldItem: Postulante, newItem: Postulante): Boolean =
            oldItem == newItem
    }
}