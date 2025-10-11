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

    @Override
    @Transactional(readOnly = true)
    public List<EmpleoResponseDTO> obtenerEmpleosPorEmpleador(Long empleadorId) {
        log.info("Obteniendo empleos del empleador ID: {}", empleadorId);

        List<Empleo> empleos = empleoRepository.findByEmpleadorIdOrderByFechaPublicacionDesc(empleadorId);

        return empleos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EmpleoResponseDTO obtenerEmpleoPorId(Long id) {
        log.info("Buscando empleo ID: {}", id);

        Empleo empleo = empleoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleo no encontrado"));

        return convertirADTO(empleo);
    }

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

    // Método auxiliar para convertir Entidad a DTO
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