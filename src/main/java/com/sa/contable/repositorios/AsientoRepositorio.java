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

    // Método para obtener todos los asientos entre fechas (sin paginación)
    @Query("SELECT a FROM Asiento a LEFT JOIN FETCH a.cuentasAsientos WHERE a.fecha BETWEEN :fechaInicio AND :fechaFin")
    List<Asiento> findAllBetweenDates(@Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);
    
    // Método existente para obtener todos los asientos sin cuentas
    @Query("SELECT a FROM Asiento a")
    List<Asiento> findAllWithoutCuentasAsientos();

    // Método para obtener asientos entre fechas con paginación
    @Query("SELECT a FROM Asiento a WHERE a.fecha BETWEEN :inicio AND :fin")
    Page<Asiento> findAllBetweenDatesPaged(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin, Pageable pageable);
    
    // Método para obtener asientos entre fechas y con una cuenta específica, con paginación
    @Query("SELECT a FROM Asiento a JOIN a.cuentasAsientos ca WHERE ca.cuenta.codigo = :cuentaCodigo AND a.fecha BETWEEN :inicio AND :fin")
    Page<Asiento> findAllByCuentaAndDates(@Param("cuentaCodigo") Long cuentaCodigo, @Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin, Pageable pageable);    
}
