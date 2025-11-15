package com.chambainfo.app.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.chambainfo.app.R
import com.chambainfo.app.data.local.TokenManager
import com.chambainfo.app.data.model.Empleo
import com.chambainfo.app.databinding.ActivityMainBinding
import com.chambainfo.app.ui.auth.LoginActivity
import com.chambainfo.app.ui.empleo.DetalleEmpleoActivity
import com.chambainfo.app.ui.empleo.EmpleoAdapter
import com.chambainfo.app.ui.empleo.PublicarEmpleoActivity
import com.chambainfo.app.ui.profile.PerfilActivity
import com.chambainfo.app.viewmodel.EmpleoViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val empleoViewModel: EmpleoViewModel by viewModels()
    private lateinit var tokenManager: TokenManager

    private var todosLosEmpleos = listOf<Empleo>()

    /**
     * Inicializa la actividad y configura los componentes principales.
     *
     * @param savedInstanceState El estado guardado de la actividad, si existe.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configurar edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        setupRecyclerViews()
        setupObservers()
        setupClickListeners()
        setupBuscador()
        verificarSesion()

        // Cargar empleos
        empleoViewModel.cargarEmpleos()
    }

    /**
     * Verifica si el usuario tiene una sesión activa y muestra u oculta el botón de perfil.
     */
    private fun verificarSesion() {
        lifecycleScope.launch {
            val token = tokenManager.getToken().first()
            if (token != null) {
                // Usuario logueado, mostrar botón de perfil
                binding.btnPerfil.visibility = View.VISIBLE
            } else {
                // Usuario no logueado, ocultar botón de perfil
                binding.btnPerfil.visibility = View.GONE
            }
        }
    }

    /**
     * Configura los RecyclerViews para mostrar empleos por categoría.
     */
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

    /**
     * Configura los observadores para los LiveData del ViewModel.
     */
    private fun setupObservers() {
        empleoViewModel.empleos.observe(this) { empleos ->
            todosLosEmpleos = empleos
            mostrarEmpleosPorCategoria(empleos)
        }

        empleoViewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        empleoViewModel.error.observe(this) { errorMsg ->
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Organiza y muestra los empleos agrupados por categorías.
     *
     * @param empleos La lista de empleos a categorizar y mostrar.
     */
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

    /**
     * Configura la visualización de una categoría de empleos en el RecyclerView.
     *
     * @param recyclerView El RecyclerView donde se mostrarán los empleos.
     * @param textView El TextView que muestra el nombre de la categoría.
     * @param empleos La lista de empleos de esta categoría.
     * @param nombreCategoria El nombre de la categoría a mostrar.
     */
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

    /**
     * Configura el buscador de empleos con un TextWatcher.
     */
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

    /**
     * Filtra los empleos según el texto de búsqueda ingresado.
     *
     * @param query El texto de búsqueda ingresado por el usuario.
     */
    private fun filtrarEmpleos(query: String) {
        if (query.isEmpty()) {
            mostrarEmpleosPorCategoria(todosLosEmpleos)
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

    /**
     * Muestra los resultados de búsqueda en el RecyclerView.
     *
     * @param empleos La lista de empleos filtrados que coinciden con la búsqueda.
     */
    private fun mostrarResultadosBusqueda(empleos: List<Empleo>) {
        ocultarTodasLasCategorias()

        binding.tvCategoriaOtros.visibility = View.VISIBLE
        binding.rvEmpleosOtros.visibility = View.VISIBLE
        binding.tvCategoriaOtros.text = "Resultados de búsqueda (${empleos.size})"
        binding.rvEmpleosOtros.adapter = EmpleoAdapter(empleos) { empleo ->
            abrirDetalleEmpleo(empleo.id)
        }
    }

    /**
     * Oculta todas las categorías de empleos en la interfaz.
     */
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

    /**
     * Configura los listeners de clic para los botones principales.
     */
    private fun setupClickListeners() {
        // Botón de perfil
        binding.btnPerfil.setOnClickListener {
            mostrarMenuPerfil()
        }
        binding.btnPublicarEmpleo.setOnClickListener {
            lifecycleScope.launch {
                tokenManager.getToken().collect { token ->
                    if (token != null) {
                        startActivity(Intent(this@MainActivity, PublicarEmpleoActivity::class.java))
                    } else {
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                    }
                }
            }
        }

        binding.btnComenzarAhora.setOnClickListener {
            lifecycleScope.launch {
                tokenManager.getToken().collect { token ->
                    if (token != null) {
                        startActivity(Intent(this@MainActivity, PublicarEmpleoActivity::class.java))
                    } else {
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                    }
                }
            }
        }
    }

    /**
     * Muestra un menú de diálogo con opciones del perfil del usuario.
     */
    private fun mostrarMenuPerfil() {
    val opciones = arrayOf("Ver mi perfil", "Mis empleos", "Cerrar sesión")

    AlertDialog.Builder(this)
        .setTitle("Mi cuenta")
        .setItems(opciones) { dialog, which ->
            when (which) {
                0 -> {
                    // Ver perfil
                    startActivity(Intent(this, PerfilActivity::class.java))
                }
                1 -> {
                    // Mis empleos (Empleador)
                    startActivity(Intent(this, com.chambainfo.app.ui.empleador.MisEmpleosActivity::class.java))
                }
                2 -> {
                    // Cerrar sesión
                    mostrarDialogoCerrarSesion()
                }
            }
        }
        .show()
}

    /**
     * Muestra un diálogo de confirmación para cerrar sesión.
     */
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

    /**
     * Cierra la sesión del usuario, limpia los datos guardados y redirige al login.
     */
    private fun cerrarSesion() {
        lifecycleScope.launch {
            tokenManager.clearAllData()

            Toast.makeText(
                this@MainActivity,
                "Sesión cerrada exitosamente",
                Toast.LENGTH_SHORT
            ).show()

            // Ocultar botón de perfil
            binding.btnPerfil.visibility = View.GONE

            // Opcional: Ir a LoginActivity
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    /**
     * Abre la actividad de detalle de un empleo específico.
     *
     * @param empleoId El ID del empleo del cual se mostrará el detalle.
     */
    private fun abrirDetalleEmpleo(empleoId: Long) {
        val intent = Intent(this, DetalleEmpleoActivity::class.java)
        intent.putExtra("EMPLEO_ID", empleoId)
        startActivity(intent)
    }

    /**
     * Se ejecuta cuando la actividad vuelve al primer plano.
     * Recarga los empleos y verifica la sesión del usuario.
     */
    override fun onResume() {
        super.onResume()
        // Recargar empleos cuando se vuelve a la actividad
        empleoViewModel.cargarEmpleos()
        // Verificar sesión
        verificarSesion()
    }
}