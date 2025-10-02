package com.chambainfo.repository;

import com.chambainfo.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByDni(String dni);
    Optional<Usuario> findByCelular(String celular);
    Optional<Usuario> findByEmail(String email);
    boolean existsByDni(String dni);
    boolean existsByCelular(String celular);
    boolean existsByEmail(String email);
}