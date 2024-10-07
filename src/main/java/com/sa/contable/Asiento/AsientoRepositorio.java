package com.sa.contable.Asiento;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AsientoRepositorio extends JpaRepository<Asiento, Long> {
    // Aquí puedes agregar métodos personalizados si es necesario
}
