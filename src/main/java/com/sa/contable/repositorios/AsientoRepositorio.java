package com.sa.contable.repositorios;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sa.contable.entidades.Asiento;


@Repository
public interface AsientoRepositorio extends JpaRepository<Asiento, Long> {

    @Query("SELECT a FROM Asiento a LEFT JOIN FETCH a.cuentasAsientos WHERE a.fecha BETWEEN :fechaInicio AND :fechaFin")
    List<Asiento> findAllBetweenDates(@Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);
    
    // Método existente
    @Query("SELECT a FROM Asiento a")
    List<Asiento> findAllWithoutCuentasAsientos();

    @Query("SELECT a FROM Asiento a WHERE a.fecha BETWEEN :inicio AND :fin")
    Page<Asiento> findAllBetweenDates(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin, Pageable pageable);
}
