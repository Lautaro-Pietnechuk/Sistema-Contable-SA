package com.sa.contable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByNombreUsuarioAndContraseña(String nombreUsuario, String contraseña);
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);
}
