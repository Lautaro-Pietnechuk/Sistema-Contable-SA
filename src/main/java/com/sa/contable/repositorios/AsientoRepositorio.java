package com.sa.contable.repositorios;

import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sa.contable.entidades.Asiento;

@Repository
public interface AsientoRepositorio extends JpaRepository<Asiento, Long> {
    @Query("SELECT a FROM Asiento a WHERE a.fecha BETWEEN :fechaInicio AND :fechaFin")
    List<Asiento> findAllBetweenDates(@Param("fechaInicio") Date fechaInicio, @Param("fechaFin") Date fechaFin);
    
    // Método existente
    @Query("SELECT a FROM Asiento a")
    List<Asiento> findAllWithoutCuentasAsientos();
}
