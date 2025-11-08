package com.chambainfo.app.ui.empleo

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chambainfo.app.R
import com.chambainfo.app.data.model.Empleo
import com.chambainfo.app.databinding.ItemEmpleoBinding
import java.text.SimpleDateFormat
import java.util.*

class EmpleoAdapter(
    private val empleos: List<Empleo>,
    private val onEmpleoClick: (Empleo) -> Unit
) : RecyclerView.Adapter<EmpleoAdapter.EmpleoViewHolder>() {

    /**
     * Crea un nuevo ViewHolder para un elemento de empleo.
     *
     * @param parent El ViewGroup padre.
     * @param viewType El tipo de vista.
     * @return Un nuevo EmpleoViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmpleoViewHolder {
        val binding = ItemEmpleoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EmpleoViewHolder(binding)
    }

    /**
     * Vincula los datos de un empleo con el ViewHolder en la posición especificada.
     *
     * @param holder El ViewHolder a vincular.
     * @param position La posición del elemento en la lista.
     */
    override fun onBindViewHolder(holder: EmpleoViewHolder, position: Int) {
        holder.bind(empleos[position])
    }

    /**
     * Obtiene el número total de elementos en la lista.
     *
     * @return El número de empleos en la lista.
     */
    override fun getItemCount(): Int = empleos.size

    inner class EmpleoViewHolder(
        private val binding: ItemEmpleoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Vincula los datos del empleo con las vistas del ViewHolder.
         *
         * @param empleo El empleo a mostrar.
         */
        fun bind(empleo: Empleo) {
            binding.tvNombreEmpleo.text = empleo.nombreEmpleo
            binding.tvEmpleador.text = empleo.empleadorNombre
            binding.tvFecha.text = calcularTiempoTranscurrido(empleo.fechaPublicacion)
            binding.tvSalario.text = empleo.salario ?: "A convenir"

            // Color del icono según categoría
            val colorIcono = obtenerColorIcono(empleo.nombreEmpleo)
            binding.iconoTrabajo.setBackgroundColor(colorIcono)

            binding.btnVer.setOnClickListener {
                onEmpleoClick(empleo)
            }

            binding.root.setOnClickListener {
                onEmpleoClick(empleo)
            }
        }

        /**
         * Calcula el tiempo transcurrido desde la fecha de publicación hasta ahora.
         *
         * @param fechaString La fecha de publicación en formato String.
         * @return Una cadena que representa el tiempo transcurrido (ej: "hace 2 días").
         */
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
                    dias < 30 -> "hace ${dias / 7} sem"
                    else -> "hace ${dias / 30} meses"
                }
            } catch (e: Exception) {
                "reciente"
            }
        }

        /**
         * Obtiene el color del icono según la categoría del empleo.
         *
         * @param nombreEmpleo El nombre del empleo para determinar la categoría.
         * @return El color del icono como un Int.
         */
        private fun obtenerColorIcono(nombreEmpleo: String): Int {
            return when {
                nombreEmpleo.contains("cajero", true) ||
                        nombreEmpleo.contains("vendedor", true) ->
                    Color.parseColor("#DBEAFE") // Azul claro

                nombreEmpleo.contains("albañil", true) ||
                        nombreEmpleo.contains("ayudante", true) ->
                    Color.parseColor("#FED7AA") // Naranja claro

                nombreEmpleo.contains("cocinero", true) ||
                        nombreEmpleo.contains("mesero", true) ->
                    Color.parseColor("#BBF7D0") // Verde claro

                nombreEmpleo.contains("limpieza", true) ->
                    Color.parseColor("#E9D5FF") // Púrpura claro

                nombreEmpleo.contains("delivery", true) ->
                    Color.parseColor("#FECACA") // Rojo claro

                else -> Color.parseColor("#E5E7EB") // Gris claro
            }
        }
    }
}