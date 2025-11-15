package com.chambainfo.backend.service;

import com.chambainfo.backend.dto.EmpleoResponseDTO;
import com.chambainfo.backend.dto.PublicarEmpleoRequestDTO;
import com.chambainfo.backend.entity.Empleo;
import com.chambainfo.backend.entity.Usuario;
import com.chambainfo.backend.repository.EmpleoRepository;
import com.chambainfo.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmpleoServiceImpl implements EmpleoService {

    private final EmpleoRepository empleoRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Publica un nuevo empleo en el sistema.
     *
     * @param request Los datos del empleo a publicar.
     * @param usuarioAutenticado El nombre de usuario del empleador autenticado.
     * @return Los datos del empleo publicado.
     * @throws RuntimeException Si el usuario no se encuentra.
     */
    @Override
    @Transactional
    public EmpleoResponseDTO publicarEmpleo(PublicarEmpleoRequestDTO request, String usuarioAutenticado) {
        log.info("Publicando empleo: {}", request.getNombreEmpleo());

        // Buscar el usuario empleador
        Usuario empleador = usuarioRepository.findByUsuario(usuarioAutenticado)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Crear el empleo
        Empleo empleo = new Empleo();
        empleo.setNombreEmpleo(request.getNombreEmpleo());
        empleo.setDescripcionEmpleo(request.getDescripcionEmpleo());
        empleo.setCelularContacto(request.getCelularContacto());
        empleo.setMostrarNumero(request.getMostrarNumero() != null ? request.getMostrarNumero() : true);
        empleo.setUbicacion(request.getUbicacion());
        empleo.setSalario(request.getSalario());
        empleo.setRuc(request.getRuc());
        empleo.setAdjuntos(request.getAdjuntos());
        empleo.setEmpleador(empleador);
        empleo.setActivo(true);

        Empleo empleoGuardado = empleoRepository.save(empleo);

        log.info("Empleo publicado exitosamente - ID: {}", empleoGuardado.getId());

        return convertirADTO(empleoGuardado);
    }

    /**
     * Obtiene todos los empleos activos disponibles en el sistema.
     *
     * @return Una lista con todos los empleos activos ordenados por fecha de publicación descendente.
     */
    @Override
    @Transactional(readOnly = true)
    public List<EmpleoResponseDTO> obtenerTodosLosEmpleos() {
        log.info("Obteniendo todos los empleos activos");

        List<Empleo> empleos = empleoRepository.findByActivoTrueOrderByFechaPublicacionDesc();

        log.info("Se encontraron {} empleos activos", empleos.size());

        return empleos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todos los empleos publicados por un empleador específico.
     *
     * @param empleadorId El ID del empleador.
     * @return Una lista con los empleos del empleador ordenados por fecha de publicación descendente.
     */
    @Override
    @Transactional(readOnly = true)
    public List<EmpleoResponseDTO> obtenerEmpleosPorEmpleador(Long empleadorId) {
        log.info("Obteniendo empleos del empleador ID: {}", empleadorId);

        List<Empleo> empleos = empleoRepository.findByEmpleadorIdOrderByFechaPublicacionDesc(empleadorId);

        log.info("Se encontraron {} empleos para el empleador ID: {}", empleos.size(), empleadorId);

        return empleos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene los detalles de un empleo específico por su ID.
     *
     * @param id El ID del empleo a obtener.
     * @return Los detalles del empleo.
     * @throws RuntimeException Si el empleo no se encuentra.
     */
    @Override
    @Transactional(readOnly = true)
    public EmpleoResponseDTO obtenerEmpleoPorId(Long id) {
        log.info("Buscando empleo ID: {}", id);

        Empleo empleo = empleoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleo no encontrado"));

        return convertirADTO(empleo);
    }

    /**
     * Desactiva un empleo publicado (solo el empleador puede desactivar sus propios empleos).
     *
     * @param id El ID del empleo a desactivar.
     * @param usuarioAutenticado El nombre de usuario del empleador autenticado.
     * @throws RuntimeException Si el empleo no se encuentra o el usuario no tiene permisos.
     */
    @Override
    @Transactional
    public void desactivarEmpleo(Long id, String usuarioAutenticado) {
        log.info("Desactivando empleo ID: {}", id);

        Empleo empleo = empleoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleo no encontrado"));

        // Verificar que el usuario sea el empleador del empleo
        if (!empleo.getEmpleador().getUsuario().equals(usuarioAutenticado)) {
            throw new RuntimeException("No tienes permiso para desactivar este empleo");
        }

        empleo.setActivo(false);
        empleoRepository.save(empleo);

        log.info("Empleo desactivado exitosamente");
    }

    /**
     * Convierte una entidad Empleo a su DTO correspondiente.
     *
     * @param empleo La entidad Empleo a convertir.
     * @return El DTO con los datos del empleo.
     */
    private EmpleoResponseDTO convertirADTO(Empleo empleo) {
        return EmpleoResponseDTO.builder()
                .id(empleo.getId())
                .nombreEmpleo(empleo.getNombreEmpleo())
                .descripcionEmpleo(empleo.getDescripcionEmpleo())
                .celularContacto(empleo.getMostrarNumero() ? empleo.getCelularContacto() : "Número oculto")
                .mostrarNumero(empleo.getMostrarNumero())
                .ubicacion(empleo.getUbicacion())
                .salario(empleo.getSalario())
                .ruc(empleo.getRuc())
                .adjuntos(empleo.getAdjuntos())
                .empleadorId(empleo.getEmpleador().getId())
                .empleadorNombre(empleo.getEmpleador().getNombreCompleto())
                .empleadorUsuario(empleo.getEmpleador().getUsuario())
                .fechaPublicacion(empleo.getFechaPublicacion())
                .ultimaActualizacion(empleo.getUltimaActualizacion())
                .activo(empleo.getActivo())
                .build();
    }
}