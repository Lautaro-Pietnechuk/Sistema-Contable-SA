package com.sa.contable.rol;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RolRepositorio extends JpaRepository<Rol, Long> {
    Optional<Rol> findByNombre(String nombre); // Cambiar a 'findByNombre'
}

