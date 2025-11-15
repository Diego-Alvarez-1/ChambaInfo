package com.chambainfo.app.ui.empleador

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chambainfo.app.data.model.Empleo
import com.chambainfo.app.databinding.ItemMiEmpleoBinding
import java.text.SimpleDateFormat
import java.util.*

class MisEmpleosAdapter(
    private val onEmpleoClick: (Empleo) -> Unit,
    private val onVerPostulacionesClick: (Empleo) -> Unit
) : ListAdapter<Empleo, MisEmpleosAdapter.EmpleoViewHolder>(EmpleoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmpleoViewHolder {
        val binding = ItemMiEmpleoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EmpleoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EmpleoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EmpleoViewHolder(
        private val binding: ItemMiEmpleoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(empleo: Empleo) {
            binding.tvNombreEmpleo.text = empleo.nombreEmpleo
            binding.tvFechaPublicacion.text = calcularTiempoTranscurrido(empleo.fechaPublicacion)
            binding.tvEstado.text = if (empleo.activo) "Activo" else "Inactivo"

            binding.root.setOnClickListener {
                onEmpleoClick(empleo)
            }

            binding.btnVerPostulantes.setOnClickListener {
                onVerPostulacionesClick(empleo)
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
                    dias == 0L -> "Publicado hoy"
                    dias == 1L -> "Publicado hace 1 día"
                    dias < 7 -> "Publicado hace $dias días"
                    dias < 30 -> "Publicado hace ${dias / 7} semanas"
                    else -> "Publicado hace ${dias / 30} meses"
                }
            } catch (e: Exception) {
                "Recientemente"
            }
        }
    }

    class EmpleoDiffCallback : DiffUtil.ItemCallback<Empleo>() {
        override fun areItemsTheSame(oldItem: Empleo, newItem: Empleo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Empleo, newItem: Empleo): Boolean {
            return oldItem == newItem
        }
    }
}