package com.chambainfo.app.ui.empleador

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chambainfo.app.R
import com.chambainfo.app.data.model.EmpleoConPostulaciones
import com.chambainfo.app.databinding.ItemMiEmpleoBinding

class MisEmpleosAdapter(
    private val onEmpleoClick: (EmpleoConPostulaciones) -> Unit
) : ListAdapter<EmpleoConPostulaciones, MisEmpleosAdapter.EmpleoViewHolder>(EmpleoDiffCallback()) {

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

        fun bind(empleo: EmpleoConPostulaciones) {
            binding.tvNombreEmpleo.text = empleo.nombreEmpleo
            binding.tvCantidadPostulaciones.text = "${empleo.cantidadPostulaciones} postulaciones"
            binding.tvDiasRestantes.text = "${empleo.diasRestantes} días restantes"
            
            // Badge de nuevas postulaciones
            if (empleo.nuevasPostulaciones > 0) {
                binding.tvNuevas.text = "+${empleo.nuevasPostulaciones} nuevas"
                binding.tvNuevas.visibility = android.view.View.VISIBLE
            } else {
                binding.tvNuevas.visibility = android.view.View.GONE
            }

            // Estado activo/finalizado
            if (empleo.activo) {
                binding.chipEstado.text = "Activo"
                binding.chipEstado.setChipBackgroundColorResource(R.color.accent_green)
            } else {
                binding.chipEstado.text = "Finalizado"
                binding.chipEstado.setChipBackgroundColorResource(R.color.text_secondary)
            }

            binding.root.setOnClickListener {
                onEmpleoClick(empleo)
            }

            binding.btnVerPostulantes.setOnClickListener {
                onEmpleoClick(empleo)
            }
        }
    }

    class EmpleoDiffCallback : DiffUtil.ItemCallback<EmpleoConPostulaciones>() {
        override fun areItemsTheSame(
            oldItem: EmpleoConPostulaciones,
            newItem: EmpleoConPostulaciones
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: EmpleoConPostulaciones,
            newItem: EmpleoConPostulaciones
        ): Boolean = oldItem == newItem
    }
}