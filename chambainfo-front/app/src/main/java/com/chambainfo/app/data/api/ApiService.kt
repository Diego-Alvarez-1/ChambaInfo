package com.chambainfo.app.data.api

import com.chambainfo.app.data.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Auth Endpoints
    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param request Los datos de registro del usuario.
     * @return Una respuesta con los datos de autenticación del usuario registrado.
     */
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    /**
     * Inicia sesión con las credenciales del usuario.
     *
     * @param request Los datos de login (usuario y contraseña).
     * @return Una respuesta con los datos de autenticación del usuario.
     */
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    /**
     * Endpoint de prueba para verificar la conexión con el servidor.
     *
     * @return Una respuesta con un mensaje de prueba.
     */
    @GET("auth/test")
    suspend fun test(): Response<String>

    /**
     * Verifica un DNI consultando la base de datos de RENIEC.
     *
     * @param dni El número de DNI a verificar (8 dígitos).
     * @return Una respuesta con los datos del DNI obtenidos de RENIEC.
     */
    @GET("auth/verificar-dni/{dni}")
    suspend fun verificarDni(@Path("dni") dni: String): Response<ReniecResponse>

    // Empleos Endpoints
    /**
     * Obtiene todos los empleos disponibles en el sistema.
     *
     * @return Una respuesta con la lista de todos los empleos activos.
     */
    @GET("empleos")
    suspend fun obtenerTodosLosEmpleos(): Response<List<Empleo>>

    /**
     * Obtiene los detalles de un empleo específico por su ID.
     *
     * @param id El ID del empleo a obtener.
     * @return Una respuesta con los detalles del empleo.
     */
    @GET("empleos/{id}")
    suspend fun obtenerEmpleoPorId(@Path("id") id: Long): Response<Empleo>

    /**
     * Obtiene todos los empleos publicados por un empleador específico.
     *
     * @param empleadorId El ID del empleador.
     * @return Una respuesta con la lista de empleos del empleador.
     */
    @GET("empleos/empleador/{empleadorId}")
    suspend fun obtenerEmpleosPorEmpleador(@Path("empleadorId") empleadorId: Long): Response<List<Empleo>>

    /**
     * Publica un nuevo empleo en el sistema.
     *
     * @param token El token de autenticación del usuario (formato: "Bearer {token}").
     * @param request Los datos del empleo a publicar.
     * @return Una respuesta con los datos del empleo publicado.
     */
    @POST("empleos/publicar")
    suspend fun publicarEmpleo(
        @Header("Authorization") token: String,
        @Body request: PublicarEmpleoRequest
    ): Response<Empleo>

    /**
     * Desactiva un empleo publicado.
     *
     * @param token El token de autenticación del usuario (formato: "Bearer {token}").
     * @param id El ID del empleo a desactivar.
     * @return Una respuesta con un mensaje de confirmación.
     */
    @DELETE("empleos/{id}")
    suspend fun desactivarEmpleo(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<String>

    // Postulaciones Endpoints
    /**
     * Envía una postulación para un empleo específico.
     *
     * @param token El token de autenticación del usuario (formato: "Bearer {token}").
     * @param request Los datos de la postulación.
     * @return Una respuesta con los datos de la postulación creada.
     */
    @POST("postulaciones")
    suspend fun postular(
        @Header("Authorization") token: String,
        @Body request: PostulacionRequest
    ): Response<PostulacionResponse>

    /**
     * Obtiene todas las postulaciones de un empleo específico.
     *
     * @param token El token de autenticación del usuario (formato: "Bearer {token}").
     * @param empleoId El ID del empleo.
     * @return Una respuesta con la lista de postulaciones del empleo.
     */
    @GET("postulaciones/empleo/{empleoId}")
    suspend fun obtenerPostulacionesPorEmpleo(
        @Header("Authorization") token: String,
        @Path("empleoId") empleoId: Long
    ): Response<List<PostulacionResponse>>

    /**
     * Obtiene todas las postulaciones del usuario autenticado.
     *
     * @param token El token de autenticación del usuario (formato: "Bearer {token}").
     * @return Una respuesta con la lista de postulaciones del usuario.
     */
    @GET("postulaciones/mis-postulaciones")
    suspend fun obtenerMisPostulaciones(
        @Header("Authorization") token: String
    ): Response<List<PostulacionResponse>>

    /**
     * Verifica si el usuario ya postuló a un empleo específico.
     *
     * @param token El token de autenticación del usuario (formato: "Bearer {token}").
     * @param empleoId El ID del empleo a verificar.
     * @return Una respuesta con true si ya postuló, false en caso contrario.
     */
    @GET("postulaciones/ya-postulo/{empleoId}")
    suspend fun yaPostulo(
        @Header("Authorization") token: String,
        @Path("empleoId") empleoId: Long
    ): Response<Boolean>

    // Documentos Endpoints
    /**
     * Sube la foto del anverso del DNI del usuario.
     *
     * @param token El token de autenticación del usuario (formato: "Bearer {token}").
     * @param file El archivo de imagen del DNI (anverso).
     * @return Una respuesta con la URL del archivo subido.
     */
    @Multipart
    @POST("documentos/dni/anverso")
    suspend fun subirDniAnverso(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part
    ): Response<Map<String, String>>

    /**
     * Sube la foto del reverso del DNI del usuario.
     *
     * @param token El token de autenticación del usuario (formato: "Bearer {token}").
     * @param file El archivo de imagen del DNI (reverso).
     * @return Una respuesta con la URL del archivo subido.
     */
    @Multipart
    @POST("documentos/dni/reverso")
    suspend fun subirDniReverso(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part
    ): Response<Map<String, String>>

    /**
     * Sube el Certificado Único Laboral (CUL) del usuario.
     *
     * @param token El token de autenticación del usuario (formato: "Bearer {token}").
     * @param file El archivo del CUL (PDF o imagen).
     * @return Una respuesta con la URL del archivo subido.
     */
    @Multipart
    @POST("documentos/cul")
    suspend fun subirCUL(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part
    ): Response<Map<String, String>>

    /**
     * Obtiene los documentos del usuario autenticado.
     *
     * @param token El token de autenticación del usuario (formato: "Bearer {token}").
     * @return Una respuesta con las URLs de los documentos del usuario.
     */
    @GET("documentos/mis-documentos")
    suspend fun obtenerMisDocumentos(
        @Header("Authorization") token: String
    ): Response<Map<String, String>>

    /**
     * Elimina un documento específico del usuario.
     *
     * @param token El token de autenticación del usuario (formato: "Bearer {token}").
     * @param tipoDocumento El tipo de documento a eliminar (dni_anverso, dni_reverso, cul).
     * @return Una respuesta con un mensaje de confirmación.
     */
    @DELETE("documentos/{tipoDocumento}")
    suspend fun eliminarDocumento(
        @Header("Authorization") token: String,
        @Path("tipoDocumento") tipoDocumento: String
    ): Response<Map<String, String>>

}