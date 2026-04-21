package com.sa.contable.repositorios;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sa.contable.entidades.Venta;

@Repository
public interface VentaRepositorio extends JpaRepository<Venta, Long> {

    Optional<Venta> findByNumeroComprobante(String numeroComprobante);

    List<Venta> findByClienteId(Long clienteId);

    List<Venta> findByFechaBetween(LocalDateTime desde, LocalDateTime hasta);

    boolean existsByNumeroComprobante(String numeroComprobante);
}