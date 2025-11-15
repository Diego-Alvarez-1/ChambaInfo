package com.chambainfo.backend.service;

import com.chambainfo.backend.dto.EmpleoConPostulacionesDTO;
import com.chambainfo.backend.dto.EstadisticasEmpleadorDTO;
import com.chambainfo.backend.dto.PostulanteDTO;
import com.chambainfo.backend.entity.Empleo;
import com.chambainfo.backend.entity.Postulacion;
import com.chambainfo.backend.entity.Usuario;
import com.chambainfo.backend.repository.EmpleoRepository;
import com.chambainfo.backend.repository.PostulacionRepository;
import com.chambainfo.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmpleadorServiceImpl implements EmpleadorService {

    private final EmpleoRepository empleoRepository;
    private final PostulacionRepository postulacionRepository;
    private final UsuarioRepository usuarioRepository;

    @Value("${whatsapp.api.url:https://wa.me}")
    private String whatsappApiUrl;

    @Value("${whatsapp.country.code:51}")
    private String whatsappCountryCode;

    /**
     * Obtiene las estadísticas generales del empleador.
     */
    @Override
    @Transactional(readOnly = true)
    public EstadisticasEmpleadorDTO obtenerEstadisticas(String usuarioAutenticado) {
        Usuario usuario = usuarioRepository.findByUsuario(usuarioAutenticado)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<Empleo> empleos = empleoRepository.findByEmpleadorIdOrderByFechaPublicacionDesc(
                usuario.getId());

        int empleosActivos = (int) empleos.stream()
                .filter(Empleo::getActivo)
                .count();

        int empleosFinalizados = empleos.size() - empleosActivos;

        int totalPostulaciones = empleos.stream()
                .mapToInt(empleo -> postulacionRepository
                        .findByEmpleoIdOrderByFechaPostulacionDesc(empleo.getId())
                        .size())
                .sum();

        // Contar postulaciones nuevas (últimas 24 horas)
        LocalDateTime hace24Horas = LocalDateTime.now().minus(24, ChronoUnit.HOURS);
        int nuevasPostulaciones = 0;
        for (Empleo empleo : empleos) {
            List<Postulacion> postulaciones = postulacionRepository
                    .findByEmpleoIdOrderByFechaPostulacionDesc(empleo.getId());
            nuevasPostulaciones += postulaciones.stream()
                    .filter(p -> p.getFechaPostulacion().isAfter(hace24Horas))
                    .count();
        }

        return EstadisticasEmpleadorDTO.builder()
                .empleosActivos(empleosActivos)
                .empleosFinalizados(empleosFinalizados)
                .totalPostulaciones(totalPostulaciones)
                .nuevasPostulaciones(nuevasPostulaciones)
                .build();
    }

    /**
     * Obtiene todos los empleos del empleador con sus postulaciones.
     */
    @Override
    @Transactional(readOnly = true)
    public List<EmpleoConPostulacionesDTO> obtenerMisEmpleosConPostulaciones(
            String usuarioAutenticado) {

        Usuario usuario = usuarioRepository.findByUsuario(usuarioAutenticado)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<Empleo> empleos = empleoRepository.findByEmpleadorIdOrderByFechaPublicacionDesc(
                usuario.getId());

        LocalDateTime hace24Horas = LocalDateTime.now().minus(24, ChronoUnit.HOURS);

        return empleos.stream()
                .map(empleo -> {
                    List<Postulacion> postulaciones = postulacionRepository
                            .findByEmpleoIdOrderByFechaPostulacionDesc(empleo.getId());

                    int nuevas = (int) postulaciones.stream()
                            .filter(p -> p.getFechaPostulacion().isAfter(hace24Horas))
                            .count();

                    // Calcular días restantes (ejemplo: 30 días desde publicación)
                    long diasDesdePublicacion = ChronoUnit.DAYS.between(
                            empleo.getFechaPublicacion(), LocalDateTime.now());
                    int diasRestantes = Math.max(0, 30 - (int) diasDesdePublicacion);

                    return EmpleoConPostulacionesDTO.builder()
                            .id(empleo.getId())
                            .nombreEmpleo(empleo.getNombreEmpleo())
                            .descripcionEmpleo(empleo.getDescripcionEmpleo())
                            .cantidadPostulaciones(postulaciones.size())
                            .nuevasPostulaciones(nuevas)
                            .fechaPublicacion(empleo.getFechaPublicacion())
                            .activo(empleo.getActivo())
                            .diasRestantes(diasRestantes)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Obtiene los postulantes de un empleo específico.
     */
    @Override
    @Transactional(readOnly = true)
    public List<PostulanteDTO> obtenerPostulantesDeEmpleo(Long empleoId, String usuarioAutenticado) {
        Usuario usuario = usuarioRepository.findByUsuario(usuarioAutenticado)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Empleo empleo = empleoRepository.findById(empleoId)
                .orElseThrow(() -> new RuntimeException("Empleo no encontrado"));

        // Verificar que el empleo pertenezca al empleador
        if (!empleo.getEmpleador().getId().equals(usuario.getId())) {
            throw new RuntimeException("No tienes permiso para ver este empleo");
        }

        List<Postulacion> postulaciones = postulacionRepository
                .findByEmpleoIdOrderByFechaPostulacionDesc(empleoId);

        LocalDateTime hace24Horas = LocalDateTime.now().minus(24, ChronoUnit.HOURS);

        return postulaciones.stream()
                .map(postulacion -> PostulanteDTO.builder()
                        .postulacionId(postulacion.getId())
                        .trabajadorId(postulacion.getTrabajador().getId())
                        .nombreCompleto(postulacion.getTrabajador().getNombreCompleto())
                        .celular(postulacion.getTrabajador().getCelular())
                        .mensaje(postulacion.getMensaje())
                        .estado(postulacion.getEstado())
                        .fechaPostulacion(postulacion.getFechaPostulacion())
                        .tiempoTranscurrido(calcularTiempoTranscurrido(
                                postulacion.getFechaPostulacion()))
                        .esNuevo(postulacion.getFechaPostulacion().isAfter(hace24Horas))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Marca una postulación como vista.
     */
    @Override
    @Transactional
    public void marcarPostulacionComoVista(Long postulacionId, String usuarioAutenticado) {
        Usuario usuario = usuarioRepository.findByUsuario(usuarioAutenticado)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Postulacion postulacion = postulacionRepository.findById(postulacionId)
                .orElseThrow(() -> new RuntimeException("Postulación no encontrada"));

        // Verificar que el empleo pertenezca al empleador
        if (!postulacion.getEmpleo().getEmpleador().getId().equals(usuario.getId())) {
            throw new RuntimeException("No tienes permiso para modificar esta postulación");
        }

        // Aquí podrías agregar un campo "vista" en la entidad Postulacion si lo necesitas
        log.info("Postulación {} marcada como vista", postulacionId);
    }

    

    /**
     * Cambia el estado de una postulación.
     */
    @Override
    @Transactional
    public void cambiarEstadoPostulacion(Long postulacionId, String nuevoEstado, 
                                         String usuarioAutenticado) {
        Usuario usuario = usuarioRepository.findByUsuario(usuarioAutenticado)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Postulacion postulacion = postulacionRepository.findById(postulacionId)
                .orElseThrow(() -> new RuntimeException("Postulación no encontrada"));

        // Verificar que el empleo pertenezca al empleador
        if (!postulacion.getEmpleo().getEmpleador().getId().equals(usuario.getId())) {
            throw new RuntimeException("No tienes permiso para modificar esta postulación");
        }

        // Validar estados permitidos
        List<String> estadosPermitidos = Arrays.asList("PENDIENTE", "CONTACTADO", "RECHAZADO");
        if (!estadosPermitidos.contains(nuevoEstado.toUpperCase())) {
            throw new RuntimeException("Estado no válido");
        }

        postulacion.setEstado(nuevoEstado.toUpperCase());
        postulacionRepository.save(postulacion);

        log.info("Estado de postulación {} cambiado a {}", postulacionId, nuevoEstado);
    }

    /**
     * Finaliza un empleo.
     */
    @Override
    @Transactional
    public void finalizarEmpleo(Long empleoId, String usuarioAutenticado) {
        Usuario usuario = usuarioRepository.findByUsuario(usuarioAutenticado)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Empleo empleo = empleoRepository.findById(empleoId)
                .orElseThrow(() -> new RuntimeException("Empleo no encontrado"));

        // Verificar que el empleo pertenezca al empleador
        if (!empleo.getEmpleador().getId().equals(usuario.getId())) {
            throw new RuntimeException("No tienes permiso para finalizar este empleo");
        }

        empleo.setActivo(false);
        empleoRepository.save(empleo);

        log.info("Empleo {} finalizado", empleoId);
    }

    /**
     * Calcula el tiempo transcurrido desde una fecha en formato legible.
     */
    private String calcularTiempoTranscurrido(LocalDateTime fecha) {
        LocalDateTime ahora = LocalDateTime.now();

        long segundos = ChronoUnit.SECONDS.between(fecha, ahora);
        long minutos = ChronoUnit.MINUTES.between(fecha, ahora);
        long horas = ChronoUnit.HOURS.between(fecha, ahora);
        long dias = ChronoUnit.DAYS.between(fecha, ahora);

        if (segundos < 60) {
            return "hace unos segundos";
        } else if (minutos < 60) {
            return "hace " + minutos + (minutos == 1 ? " minuto" : " minutos");
        } else if (horas < 24) {
            return "hace " + horas + (horas == 1 ? " hora" : " horas");
        } else if (dias < 30) {
            return "hace " + dias + (dias == 1 ? " día" : " días");
        } else {
            return "hace más de un mes";
        }
    }
}