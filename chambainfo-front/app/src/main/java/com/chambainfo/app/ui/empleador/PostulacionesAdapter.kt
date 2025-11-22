package com.chambainfo.app.ui.empleador

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chambainfo.app.R
import com.chambainfo.app.data.model.PostulacionResponse
import java.text.SimpleDateFormat
import java.util.*

class PostulacionesAdapter(
    private val postulaciones: List<PostulacionResponse>,
    private val onArchivarClick: (PostulacionResponse) -> Unit
) : RecyclerView.Adapter<PostulacionesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_postulacion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(postulaciones[position])
    }

    override fun getItemCount(): Int = postulaciones.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre: TextView = itemView.findViewById(R.id.tvNombreTrabajador)
        private val tvDni: TextView = itemView.findViewById(R.id.tvDniTrabajador)
        private val tvEstado: TextView = itemView.findViewById(R.id.tvEstado)
        private val tvFecha: TextView = itemView.findViewById(R.id.tvFecha)
        private val tvCelular: TextView = itemView.findViewById(R.id.tvCelular)
        private val tvMensaje: TextView = itemView.findViewById(R.id.tvMensaje)
        private val btnArchivar: ImageButton = itemView.findViewById(R.id.btnArchivar)
        private val btnResponderWhatsApp: Button = itemView.findViewById(R.id.btnResponderWhatsApp)
        private val btnVerPerfil: Button = itemView.findViewById(R.id.btnVerPerfil)

        fun bind(postulacion: PostulacionResponse) {
            tvNombre.text = postulacion.trabajadorNombre
            tvDni.text = "DNI: ${postulacion.trabajadorDni}"
            tvEstado.text = postulacion.estado
            tvFecha.text = calcularTiempoTranscurrido(postulacion.fechaPostulacion)
            tvCelular.text = "Cel: +51 ${postulacion.trabajadorCelular}"
            tvMensaje.text = postulacion.mensaje

            // Color del estado
            when (postulacion.estado) {
                "PENDIENTE" -> tvEstado.setTextColor(itemView.context.getColor(R.color.accent_orange))
                "ACEPTADO" -> tvEstado.setTextColor(itemView.context.getColor(R.color.accent_green))
                "RECHAZADO" -> tvEstado.setTextColor(itemView.context.getColor(R.color.accent_red))
            }

            btnArchivar.setOnClickListener {
                onArchivarClick(postulacion)
            }

            btnResponderWhatsApp.setOnClickListener {
                val mensaje = "Hola ${postulacion.trabajadorNombre}, vi tu postulación para el empleo de ${postulacion.nombreEmpleo}"
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://wa.me/51${postulacion.trabajadorCelular}?text=${Uri.encode(mensaje)}")
                }
                itemView.context.startActivity(intent)
            }

            btnVerPerfil.setOnClickListener {
                // TODO: Navegar al perfil del trabajador
                android.widget.Toast.makeText(
                    itemView.context,
                    "Ver perfil de ${postulacion.trabajadorNombre}",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
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
                    minutos < 1 -> "justo ahora"
                    minutos < 60 -> "hace $minutos min"
                    horas < 24 -> "hace $horas h"
                    dias < 7 -> "hace $dias días"
                    dias < 30 -> "hace ${dias / 7} sem"
                    else -> "hace ${dias / 30} meses"
                }
            } catch (e: Exception) {
                "recientemente"
            }
        }
    }
}