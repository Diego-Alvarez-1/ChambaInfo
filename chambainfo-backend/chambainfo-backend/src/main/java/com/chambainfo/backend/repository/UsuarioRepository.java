
package com.chambainfo.backend.repository;

import com.chambainfo.backend.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    Optional<Usuario> findByUsuario(String usuario);
    
    Optional<Usuario> findByDni(String dni);

    Optional<Usuario> findByCelular(String celular);

    // implementar después porque no se guarda el email todavía
    // Optional<Usuario> findByEmail(String email);

    boolean existsByUsuario(String usuario);
    
    boolean existsByDni(String dni);
}