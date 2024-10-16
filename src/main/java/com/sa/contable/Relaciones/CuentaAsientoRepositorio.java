package com.sa.contable.relaciones;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CuentaAsientoRepositorio extends JpaRepository<CuentaAsiento, Long> {
    // Puedes agregar métodos personalizados si es necesario
}
