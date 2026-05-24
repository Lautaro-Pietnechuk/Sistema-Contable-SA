package com.sa.contable.repositorios;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sa.contable.entidades.Cobro;
import com.sa.contable.entidades.Venta;

@Repository
public interface CobroRepositorio extends JpaRepository<Cobro, Long> {

    @Query("SELECT c FROM Cobro c WHERE c.cliente.id = :clienteId ORDER BY c.fecha DESC")
    List<Cobro> findByClienteIdOrderByFechaDesc(@Param("clienteId") Long clienteId);

    // Agregá esta línea adentro de tu VentaRepositorio.java
    List<Venta> findByClienteIdAndAnuladoOrderByFechaAsc(Long clienteId, Boolean anulado);

    List<Cobro> findByClienteIdAndFechaBetweenOrderByFechaDesc(Long clienteId, LocalDateTime desde, LocalDateTime hasta);
}
