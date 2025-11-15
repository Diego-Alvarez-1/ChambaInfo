package com.chambainfo.backend.service;

import com.chambainfo.backend.dto.PostulacionRequest;
import com.chambainfo.backend.dto.PostulacionResponse;
import com.chambainfo.backend.entity.Empleo;
import com.chambainfo.backend.entity.Postulacion;
import com.chambainfo.backend.entity.Usuario;
import com.chambainfo.backend.repository.EmpleoRepository;
import com.chambainfo.backend.repository.PostulacionRepository;
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
public class PostulacionServiceImpl implements PostulacionService {

    private final PostulacionRepository postulacionRepository;
    private final EmpleoRepository empleoRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Envía una postulación para un empleo específico.
     *
     * @param request Los datos de la postulación.
     * @param usuarioAutenticado El nombre de usuario del trabajador autenticado.
     * @return Los datos de la postulación creada.
     * @throws RuntimeException Si el empleo o usuario no se encuentran, o si ya postuló al empleo.
     */
    @Override
    @Transactional
    public PostulacionResponse postular(PostulacionRequest request, String usuarioAutenticado) {
        log.info("Postulación recibida para empleo ID: {}", request.getEmpleoId());

        Empleo empleo = empleoRepository.findById(request.getEmpleoId())
                .orElseThrow(() -> new RuntimeException("Empleo no encontrado"));

        Usuario trabajador = usuarioRepository.findByUsuario(usuarioAutenticado)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (postulacionRepository.existsByEmpleoIdAndTrabajadorId(
                empleo.getId(), trabajador.getId())) {
            throw new RuntimeException("Ya has postulado a este empleo");
        }

        Postulacion postulacion = new Postulacion();
        postulacion.setEmpleo(empleo);
        postulacion.setTrabajador(trabajador);
        postulacion.setMensaje(request.getMensaje());
        postulacion.setEstado("PENDIENTE");

        Postulacion guardada = postulacionRepository.save(postulacion);

        log.info("Postulación guardada - ID: {}", guardada.getId());

        return convertirADTO(guardada);
    }

    /**
     * Obtiene todas las postulaciones de un empleo específico.
     *
     * @param empleoId El ID del empleo.
     * @return Una lista con las postulaciones del empleo ordenadas por fecha descendente.
     */
    @Override
    @Transactional(readOnly = true)
    public List<PostulacionResponse> obtenerPostulacionesPorEmpleo(Long empleoId) {
        log.info("Obteniendo postulaciones para empleo ID: {}", empleoId);

        List<Postulacion> postulaciones =
                postulacionRepository.findByEmpleoIdOrderByFechaPostulacionDesc(empleoId);

        return postulaciones.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todas las postulaciones del usuario autenticado.
     *
     * @param usuarioAutenticado El nombre de usuario del trabajador autenticado.
     * @return Una lista con las postulaciones del usuario ordenadas por fecha descendente.
     * @throws RuntimeException Si el usuario no se encuentra.
     */
    @Override
    @Transactional(readOnly = true)
    public List<PostulacionResponse> obtenerMisPostulaciones(String usuarioAutenticado) {
        Usuario trabajador = usuarioRepository.findByUsuario(usuarioAutenticado)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<Postulacion> postulaciones =
                postulacionRepository.findByTrabajadorIdOrderByFechaPostulacionDesc(trabajador.getId());

        return postulaciones.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Verifica si el usuario ya postuló a un empleo específico.
     *
     * @param empleoId El ID del empleo a verificar.
     * @param usuarioAutenticado El nombre de usuario del trabajador autenticado.
     * @return true si ya postuló, false en caso contrario.
     * @throws RuntimeException Si el usuario no se encuentra.
     */
    @Override
    @Transactional(readOnly = true)
    public boolean yaPostulo(Long empleoId, String usuarioAutenticado) {
        Usuario trabajador = usuarioRepository.findByUsuario(usuarioAutenticado)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return postulacionRepository.existsByEmpleoIdAndTrabajadorId(
                empleoId, trabajador.getId());
    }

    /**
     * Actualiza el estado de una postulación.
     *
     * @param id El ID de la postulación.
     * @param nuevoEstado El nuevo estado de la postulación.
     * @return Los datos actualizados de la postulación.
     * @throws RuntimeException Si la postulación no se encuentra.
     */
    @Override
    @Transactional
    public PostulacionResponse actualizarEstado(Long id, String nuevoEstado) {
        log.info("Actualizando estado de postulación ID: {} a estado: {}", id, nuevoEstado);

        Postulacion postulacion = postulacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Postulación no encontrada"));

        postulacion.setEstado(nuevoEstado);
        Postulacion actualizada = postulacionRepository.save(postulacion);

        log.info("Estado de postulación actualizado - ID: {}, Nuevo estado: {}", actualizada.getId(), actualizada.getEstado());

        return convertirADTO(actualizada);
    }

    /**
     * Convierte una entidad Postulacion a su DTO correspondiente.
     *
     * @param postulacion La entidad Postulacion a convertir.
     * @return El DTO con los datos de la postulación.
     */
    private PostulacionResponse convertirADTO(Postulacion postulacion) {
        return PostulacionResponse.builder()
                .id(postulacion.getId())
                .empleoId(postulacion.getEmpleo().getId())
                .nombreEmpleo(postulacion.getEmpleo().getNombreEmpleo())
                .trabajadorId(postulacion.getTrabajador().getId())
                .trabajadorNombre(postulacion.getTrabajador().getNombreCompleto())
                .trabajadorDni(postulacion.getTrabajador().getDni())
                .trabajadorCelular(postulacion.getTrabajador().getCelular())
                .mensaje(postulacion.getMensaje())
                .estado(postulacion.getEstado())
                .fechaPostulacion(postulacion.getFechaPostulacion())
                .build();
    }
}