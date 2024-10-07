package com.sa.contable.Rol;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RolRepositorio extends JpaRepository<Rol, Long> {
    Optional<Rol> findByName(String name);
}
