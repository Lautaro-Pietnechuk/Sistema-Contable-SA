package com.sa.contable.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sa.contable.entidades.CuentaAsiento;

@Repository
public interface CuentaAsientoRepositorio extends JpaRepository<CuentaAsiento, Long> {
    // Puedes agregar métodos personalizados si es necesario
}
