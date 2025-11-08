package com.chambainfo.backend.repository;

import com.chambainfo.backend.entity.Postulacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostulacionRepository extends JpaRepository<Postulacion, Long> {

    List<Postulacion> findByEmpleoIdOrderByFechaPostulacionDesc(Long empleoId);

    List<Postulacion> findByTrabajadorIdOrderByFechaPostulacionDesc(Long trabajadorId);

    Optional<Postulacion> findByEmpleoIdAndTrabajadorId(Long empleoId, Long trabajadorId);

    boolean existsByEmpleoIdAndTrabajadorId(Long empleoId, Long trabajadorId);
}