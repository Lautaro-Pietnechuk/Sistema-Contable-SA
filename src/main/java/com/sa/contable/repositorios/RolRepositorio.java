package com.sa.contable.repositorios;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sa.contable.entidades.Rol;

public interface RolRepositorio extends JpaRepository<Rol, Long> {
    Optional<Rol> findByNombre(String nombre); // Cambiar a 'findByNombre'
}

