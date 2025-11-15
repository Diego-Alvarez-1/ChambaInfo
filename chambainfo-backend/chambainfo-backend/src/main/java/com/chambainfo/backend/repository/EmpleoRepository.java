package com.chambainfo.backend.repository;

import com.chambainfo.backend.entity.Empleo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmpleoRepository extends JpaRepository<Empleo, Long> {

    // Obtener todos los empleos activos ordenados por fecha (más recientes primero)
    List<Empleo> findByActivoTrueOrderByFechaPublicacionDesc();

    // Obtener empleos por empleador
    List<Empleo> findByEmpleadorIdOrderByFechaPublicacionDesc(Long empleadorId);

    // Buscar empleos por nombre (para búsquedas futuras)
    @Query("SELECT e FROM Empleo e WHERE e.activo = true AND LOWER(e.nombreEmpleo) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Empleo> buscarPorNombre(String keyword);
}