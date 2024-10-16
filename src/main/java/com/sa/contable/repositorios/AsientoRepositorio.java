package com.sa.contable.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sa.contable.entidades.Asiento;

@Repository
public interface AsientoRepositorio extends JpaRepository<Asiento, Long> {
    // Aquí puedes agregar métodos personalizados si es necesario
}
