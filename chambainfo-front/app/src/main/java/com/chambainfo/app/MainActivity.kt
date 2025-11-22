package com.chambainfo.app.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chambainfo.app.R
import com.chambainfo.app.data.local.TokenManager
import com.chambainfo.app.data.model.Empleo
import com.chambainfo.app.data.model.Notificacion
import com.chambainfo.app.databinding.ActivityMainBinding
import com.chambainfo.app.ui.auth.LoginActivity
import com.chambainfo.app.ui.empleo.DetalleEmpleoActivity
import com.chambainfo.app.ui.empleo.EmpleoAdapter
import com.chambainfo.app.ui.empleador.PostulacionesEmpleoActivity
import com.chambainfo.app.ui.empleador.EmpleadorDashboardActivity
import com.chambainfo.app.ui.profile.PerfilActivity
import com.chambainfo.app.utils.NotificacionManager
import com.chambainfo.app.viewmodel.EmpleoViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val empleoViewModel: EmpleoViewModel by viewModels()
    private lateinit var tokenManager: TokenManager
    private lateinit var notificacionManager: NotificacionManager

    private var todosLosEmpleos = listOf<Empleo>()
    private var categoriaSeleccionada: String = "Todas"
    private var ordenActual: String = "Recientes" // "Recientes" o "Antiguos"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)
        notificacionManager = NotificacionManager(this)

        verificarRolYRedirigir()

        setupRecyclerViews()
        setupObservers()
        setupClickListeners()
        setupBuscador()
        verificarSesion()

        empleoViewModel.cargarEmpleos()
    }

    private fun verificarSesion() {
        lifecycleScope.launch {
            val token = tokenManager.getToken().first()
            val userId = tokenManager.getUserId().first()
            val rol = tokenManager.getRol().first()

            if (token != null && userId != null) {
                binding.btnPerfil.visibility = View.VISIBLE

                // Cargar notificaciones
                actualizarBadgeNotificaciones(userId)

                // Mostrar botón publicar solo si es EMPLEADOR
                if (rol == "EMPLEADOR") {
                    binding.btnPublicarEmpleo.visibility = View.VISIBLE
                } else {
                    binding.btnPublicarEmpleo.visibility = View.GONE
                }
            } else {
                binding.btnPerfil.visibility = View.GONE
                binding.btnPublicarEmpleo.visibility = View.VISIBLE // Mostrar para incentivar registro
            }
        }
    }


    private fun verificarRolYRedirigir() {
        lifecycleScope.launch {
            val rol = tokenManager.getRol().first()
            if (rol == "EMPLEADOR") {
                val intent = Intent(this@MainActivity, EmpleadorDashboardActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }

    private fun setupRecyclerViews() {
        binding.rvEmpleosAtencion.layoutManager = LinearLayoutManager(
            this, LinearLayoutManager.HORIZONTAL, false
        )
        binding.rvEmpleosConstruccion.layoutManager = LinearLayoutManager(
            this, LinearLayoutManager.HORIZONTAL, false
        )
        binding.rvEmpleosCocina.layoutManager = LinearLayoutManager(
            this, LinearLayoutManager.HORIZONTAL, false
        )
        binding.rvEmpleosLimpieza.layoutManager = LinearLayoutManager(
            this, LinearLayoutManager.HORIZONTAL, false
        )
        binding.rvEmpleosDelivery.layoutManager = LinearLayoutManager(
            this, LinearLayoutManager.HORIZONTAL, false
        )
        binding.rvEmpleosOtros.layoutManager = LinearLayoutManager(
            this, LinearLayoutManager.HORIZONTAL, false
        )
    }

    private fun setupObservers() {
        empleoViewModel.empleos.observe(this) { empleos ->
            todosLosEmpleos = empleos
            aplicarFiltrosYOrden()
        }

        empleoViewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        empleoViewModel.error.observe(this) { errorMsg ->
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarEmpleosPorCategoria(empleos: List<Empleo>) {
        val atencion = empleos.filter { empleo ->
            val nombre = empleo.nombreEmpleo.lowercase()
            nombre.contains("cajero") ||
                    nombre.contains("vendedor") ||
                    nombre.contains("atencion") ||
                    nombre.contains("atención") ||
                    nombre.contains("recepcion") ||
                    nombre.contains("recepción") ||
                    nombre.contains("cliente")
        }

        val construccion = empleos.filter { empleo ->
            val nombre = empleo.nombreEmpleo.lowercase()
            nombre.contains("albañil") ||
                    nombre.contains("ayudante") ||
                    nombre.contains("construccion") ||
                    nombre.contains("construcción") ||
                    nombre.contains("obrero") ||
                    nombre.contains("maestro") ||
                    nombre.contains("carpintero") ||
                    nombre.contains("pintor") ||
                    nombre.contains("electricista") ||
                    nombre.contains("gasfitero") ||
                    nombre.contains("soldador")
        }

        val cocina = empleos.filter { empleo ->
            val nombre = empleo.nombreEmpleo.lowercase()
            nombre.contains("cocinero") ||
                    nombre.contains("mesero") ||
                    nombre.contains("chef") ||
                    nombre.contains("cocina") ||
                    nombre.contains("restaurante") ||
                    nombre.contains("parrillero") ||
                    nombre.contains("pizzero") ||
                    nombre.contains("repostero") ||
                    nombre.contains("barista") ||
                    nombre.contains("mozo")
        }

        val limpieza = empleos.filter { empleo ->
            val nombre = empleo.nombreEmpleo.lowercase()
            nombre.contains("limpieza") ||
                    nombre.contains("domestico") ||
                    nombre.contains("doméstico") ||
                    nombre.contains("empleada") ||
                    nombre.contains("ama de casa") ||
                    nombre.contains("niñera")
        }

        val delivery = empleos.filter { empleo ->
            val nombre = empleo.nombreEmpleo.lowercase()
            nombre.contains("delivery") ||
                    nombre.contains("repartidor") ||
                    nombre.contains("chofer") ||
                    nombre.contains("conductor") ||
                    nombre.contains("motorizado") ||
                    nombre.contains("transporte")
        }

        val categoriasPrincipales = atencion + construccion + cocina + limpieza + delivery
        val otros = empleos.filter { empleo ->
            !categoriasPrincipales.contains(empleo)
        }

        configurarCategoria(
            binding.rvEmpleosAtencion,
            binding.tvCategoriaAtencion,
            atencion,
            "Atención al Cliente"
        )

        configurarCategoria(
            binding.rvEmpleosConstruccion,
            binding.tvCategoriaConstruccion,
            construccion,
            "Construcción"
        )

        configurarCategoria(
            binding.rvEmpleosCocina,
            binding.tvCategoriaCocina,
            cocina,
            "Cocina y Restaurantes"
        )

        configurarCategoria(
            binding.rvEmpleosLimpieza,
            binding.tvCategoriaLimpieza,
            limpieza,
            "Limpieza"
        )

        configurarCategoria(
            binding.rvEmpleosDelivery,
            binding.tvCategoriaDelivery,
            delivery,
            "Delivery y Transporte"
        )

        configurarCategoria(
            binding.rvEmpleosOtros,
            binding.tvCategoriaOtros,
            otros,
            "Otros"
        )
    }

    private fun configurarCategoria(
        recyclerView: androidx.recyclerview.widget.RecyclerView,
        textView: android.widget.TextView,
        empleos: List<Empleo>,
        nombreCategoria: String
    ) {
        if (empleos.isNotEmpty()) {
            textView.visibility = View.VISIBLE
            recyclerView.visibility = View.VISIBLE
            textView.text = nombreCategoria
            recyclerView.adapter = EmpleoAdapter(empleos) { empleo ->
                abrirDetalleEmpleo(empleo.id)
            }
        } else {
            textView.visibility = View.GONE
            recyclerView.visibility = View.GONE
        }
    }

    private fun setupBuscador() {
        binding.etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                filtrarEmpleos(query)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filtrarEmpleos(query: String) {
        if (query.isEmpty()) {
            aplicarFiltrosYOrden()
        } else {
            val empleosFiltrados = todosLosEmpleos.filter { empleo ->
                val nombre = empleo.nombreEmpleo.lowercase()
                val empleador = empleo.empleadorNombre.lowercase()
                val ubicacion = empleo.ubicacion?.lowercase() ?: ""
                val descripcion = empleo.descripcionEmpleo.lowercase()
                val queryLower = query.lowercase()

                nombre.contains(queryLower) ||
                        empleador.contains(queryLower) ||
                        ubicacion.contains(queryLower) ||
                        descripcion.contains(queryLower)
            }

            if (empleosFiltrados.isEmpty()) {
                ocultarTodasLasCategorias()
                Toast.makeText(
                    this,
                    "No se encontraron empleos con: \"$query\"",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                mostrarResultadosBusqueda(empleosFiltrados)
            }
        }
    }

    private fun mostrarResultadosBusqueda(empleos: List<Empleo>) {
        ocultarTodasLasCategorias()

        binding.tvCategoriaOtros.visibility = View.VISIBLE
        binding.rvEmpleosOtros.visibility = View.VISIBLE
        binding.tvCategoriaOtros.text = "Resultados de búsqueda (${empleos.size})"
        binding.rvEmpleosOtros.adapter = EmpleoAdapter(empleos) { empleo ->
            abrirDetalleEmpleo(empleo.id)
        }
    }

    private fun ocultarTodasLasCategorias() {
        binding.tvCategoriaAtencion.visibility = View.GONE
        binding.rvEmpleosAtencion.visibility = View.GONE

        binding.tvCategoriaConstruccion.visibility = View.GONE
        binding.rvEmpleosConstruccion.visibility = View.GONE

        binding.tvCategoriaCocina.visibility = View.GONE
        binding.rvEmpleosCocina.visibility = View.GONE

        binding.tvCategoriaLimpieza.visibility = View.GONE
        binding.rvEmpleosLimpieza.visibility = View.GONE

        binding.tvCategoriaDelivery.visibility = View.GONE
        binding.rvEmpleosDelivery.visibility = View.GONE

        binding.tvCategoriaOtros.visibility = View.GONE
        binding.rvEmpleosOtros.visibility = View.GONE
    }

    private fun setupClickListeners() {
        binding.btnPerfil.setOnClickListener {
            mostrarMenuPerfil()
        }

        // Botón de notificaciones
        binding.btnNotificaciones.setOnClickListener {
            mostrarPopupNotificaciones()
        }

        // Filtro por categoría
        binding.btnFiltroCategoria.setOnClickListener {
            mostrarDialogoFiltroCategoria()
        }

        // Botón ordenar
        binding.btnOrdenar.setOnClickListener {
            cambiarOrden()
        }
    }

    private fun mostrarMenuPerfil() {
        val opciones = arrayOf("Ver mi perfil", "Cerrar sesión")

        AlertDialog.Builder(this)
            .setTitle("Mi cuenta")
            .setItems(opciones) { dialog, which ->
                when (which) {
                    0 -> {
                        startActivity(Intent(this, PerfilActivity::class.java))
                    }
                    1 -> {
                        mostrarDialogoCerrarSesion()
                    }
                }
            }
            .show()
    }

    private fun mostrarDialogoCerrarSesion() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Estás seguro de que deseas cerrar sesión?")
            .setPositiveButton("Sí") { _, _ ->
                cerrarSesion()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun cerrarSesion() {
        lifecycleScope.launch {
            tokenManager.clearAllData()

            Toast.makeText(
                this@MainActivity,
                "Sesión cerrada exitosamente",
                Toast.LENGTH_SHORT
            ).show()

            binding.btnPerfil.visibility = View.GONE
            binding.btnNotificaciones.visibility = View.GONE

            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun abrirDetalleEmpleo(empleoId: Long) {
        val intent = Intent(this, DetalleEmpleoActivity::class.java)
        intent.putExtra("EMPLEO_ID", empleoId)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        empleoViewModel.cargarEmpleos()
        verificarSesion()

        // Actualizar badge si hay usuario logueado
        lifecycleScope.launch {
            val userId = tokenManager.getUserId().first()
            if (userId != null) {
                actualizarBadgeNotificaciones(userId)
            }
        }
    }

    /**
     * Muestra un diálogo para seleccionar la categoría de empleos a filtrar.
     */
    private fun mostrarDialogoFiltroCategoria() {
        val categorias = arrayOf(
            "Todas",
            "Atención al Cliente",
            "Construcción",
            "Cocina y Restaurantes",
            "Limpieza",
            "Delivery y Transporte",
            "Otros"
        )

        val categoriaActualIndex = categorias.indexOf(categoriaSeleccionada)

        AlertDialog.Builder(this)
            .setTitle("Filtrar por categoría")
            .setSingleChoiceItems(categorias, categoriaActualIndex) { dialog, which ->
                categoriaSeleccionada = categorias[which]
                binding.btnFiltroCategoria.text = categoriaSeleccionada
                aplicarFiltrosYOrden()
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Cambia el orden de los empleos entre recientes y antiguos.
     */
    private fun cambiarOrden() {
        ordenActual = if (ordenActual == "Recientes") "Antiguos" else "Recientes"
        binding.btnOrdenar.text = ordenActual

        // Cambiar el ícono según el orden
        val iconoRes = if (ordenActual == "Recientes") {
            R.drawable.ic_sort
        } else {
            R.drawable.ic_sort_reverse
        }
        binding.btnOrdenar.setIconResource(iconoRes)

        aplicarFiltrosYOrden()
    }

    /**
     * Aplica los filtros y el orden seleccionados a los empleos.
     */
    private fun aplicarFiltrosYOrden() {
        var empleosFiltrados = todosLosEmpleos

        // Filtrar por categoría
        if (categoriaSeleccionada != "Todas") {
            empleosFiltrados = when (categoriaSeleccionada) {
                "Atención al Cliente" -> filtrarPorCategoria(empleosFiltrados, listOf(
                    "cajero", "vendedor", "atencion", "atención", "recepcion", "recepción", "cliente"
                ))
                "Construcción" -> filtrarPorCategoria(empleosFiltrados, listOf(
                    "albañil", "ayudante", "construccion", "construcción", "obrero", "maestro",
                    "carpintero", "pintor", "electricista", "gasfitero", "soldador"
                ))
                "Cocina y Restaurantes" -> filtrarPorCategoria(empleosFiltrados, listOf(
                    "cocinero", "mesero", "chef", "cocina", "restaurante", "parrillero",
                    "pizzero", "repostero", "barista", "mozo"
                ))
                "Limpieza" -> filtrarPorCategoria(empleosFiltrados, listOf(
                    "limpieza", "domestico", "doméstico", "empleada", "ama de casa", "niñera"
                ))
                "Delivery y Transporte" -> filtrarPorCategoria(empleosFiltrados, listOf(
                    "delivery", "repartidor", "chofer", "conductor", "motorizado", "transporte"
                ))
                "Otros" -> {
                    val todasLasCategorias = listOf(
                        "cajero", "vendedor", "atencion", "atención", "recepcion", "recepción", "cliente",
                        "albañil", "ayudante", "construccion", "construcción", "obrero", "maestro",
                        "carpintero", "pintor", "electricista", "gasfitero", "soldador",
                        "cocinero", "mesero", "chef", "cocina", "restaurante", "parrillero",
                        "pizzero", "repostero", "barista", "mozo",
                        "limpieza", "domestico", "doméstico", "empleada", "ama de casa", "niñera",
                        "delivery", "repartidor", "chofer", "conductor", "motorizado", "transporte"
                    )
                    empleosFiltrados.filter { empleo ->
                        val nombre = empleo.nombreEmpleo.lowercase()
                        todasLasCategorias.none { nombre.contains(it) }
                    }
                }
                else -> empleosFiltrados
            }
        }

        // Ordenar por fecha
        empleosFiltrados = if (ordenActual == "Recientes") {
            empleosFiltrados.sortedByDescending { it.fechaPublicacion }
        } else {
            empleosFiltrados.sortedBy { it.fechaPublicacion }
        }

        // Mostrar resultados
        mostrarEmpleosPorCategoria(empleosFiltrados)
    }

    /**
     * Filtra empleos por palabras clave de categoría.
     */
    private fun filtrarPorCategoria(empleos: List<Empleo>, palabrasClave: List<String>): List<Empleo> {
        return empleos.filter { empleo ->
            val nombre = empleo.nombreEmpleo.lowercase()
            palabrasClave.any { nombre.contains(it) }
        }
    }

    /**
     * Muestra un popup con las notificaciones del usuario.
     */
    private fun mostrarPopupNotificaciones() {
        val popupView = layoutInflater.inflate(R.layout.popup_notificaciones, null)
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        val rvNotificaciones = popupView.findViewById<RecyclerView>(R.id.rvNotificaciones)
        val tvSinNotificaciones = popupView.findViewById<TextView>(R.id.tvSinNotificaciones)
        val tvMarcarTodasLeidas = popupView.findViewById<TextView>(R.id.tvMarcarTodasLeidas)

        lifecycleScope.launch {
            val userId = tokenManager.getUserId().first() ?: return@launch

            notificacionManager.obtenerNotificaciones(userId).collect { notificaciones ->
                if (notificaciones.isEmpty()) {
                    rvNotificaciones.visibility = View.GONE
                    tvSinNotificaciones.visibility = View.VISIBLE
                } else {
                    rvNotificaciones.visibility = View.VISIBLE
                    tvSinNotificaciones.visibility = View.GONE

                    val adapter = NotificacionesAdapter(notificaciones) { notificacion ->
                        lifecycleScope.launch {
                            notificacionManager.marcarComoLeida(userId, notificacion.id)
                            // TODO: Navegar a la pantalla correspondiente
                            if (notificacion.empleoId != null) {
                                val intent = Intent(this@MainActivity, PostulacionesEmpleoActivity::class.java)
                                intent.putExtra("EMPLEO_ID", notificacion.empleoId)
                                startActivity(intent)
                            }
                            popupWindow.dismiss()
                        }
                    }

                    rvNotificaciones.layoutManager = LinearLayoutManager(this@MainActivity)
                    rvNotificaciones.adapter = adapter
                }
            }
        }

        tvMarcarTodasLeidas.setOnClickListener {
            lifecycleScope.launch {
                val userId = tokenManager.getUserId().first() ?: return@launch
                notificacionManager.marcarTodasComoLeidas(userId)
                popupWindow.dismiss()
            }
        }

        popupWindow.showAsDropDown(binding.btnNotificaciones, 0, 0, Gravity.END)
    }
    /**
     * Actualiza el badge de notificaciones no leídas.
     */
    private fun actualizarBadgeNotificaciones(userId: Long) {
        lifecycleScope.launch {
            notificacionManager.contarNoLeidas(userId).collect { cantidad ->
                if (cantidad > 0) {
                    binding.tvBadgeNotificaciones.visibility = View.VISIBLE
                    binding.tvBadgeNotificaciones.text = if (cantidad > 9) "9+" else cantidad.toString()
                } else {
                    binding.tvBadgeNotificaciones.visibility = View.GONE
                }
            }
        }
    }

    /**
     * Adapter para mostrar notificaciones en el RecyclerView.
     */
    inner class NotificacionesAdapter(
        private val notificaciones: List<Notificacion>,
        private val onClick: (Notificacion) -> Unit
    ) : RecyclerView.Adapter<NotificacionesAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_notificacion, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(notificaciones[position])
        }

        override fun getItemCount(): Int = notificaciones.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val indicador: View = itemView.findViewById(R.id.indicadorNoLeida)
            private val tvTitulo: TextView = itemView.findViewById(R.id.tvTituloNotificacion)
            private val tvMensaje: TextView = itemView.findViewById(R.id.tvMensajeNotificacion)
            private val tvFecha: TextView = itemView.findViewById(R.id.tvFechaNotificacion)

            fun bind(notificacion: Notificacion) {
                tvTitulo.text = notificacion.titulo
                tvMensaje.text = notificacion.mensaje
                tvFecha.text = calcularTiempoTranscurrido(notificacion.fecha)

                indicador.visibility = if (notificacion.leida) View.INVISIBLE else View.VISIBLE

                itemView.setOnClickListener {
                    onClick(notificacion)
                }
            }

            private fun calcularTiempoTranscurrido(fecha: Date): String {
                val ahora = Date()
                val diff = ahora.time - fecha.time

                val minutos = diff / (1000 * 60)
                val horas = diff / (1000 * 60 * 60)
                val dias = diff / (1000 * 60 * 60 * 24)

                return when {
                    minutos < 1 -> "justo ahora"
                    minutos < 60 -> "hace $minutos min"
                    horas < 24 -> "hace $horas h"
                    dias < 7 -> "hace $dias días"
                    else -> "hace ${dias / 7} semanas"
                }
            }
        }
    }
}