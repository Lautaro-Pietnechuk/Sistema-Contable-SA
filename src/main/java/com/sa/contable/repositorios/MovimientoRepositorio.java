package com.sa.contable.repositorios;

import com.sa.contable.entidades.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovimientoRepositorio extends JpaRepository<Movimiento, Long> {
    // Puedes agregar métodos personalizados si es necesario
}
