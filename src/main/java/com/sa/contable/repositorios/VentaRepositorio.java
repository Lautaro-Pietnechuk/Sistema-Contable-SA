package com.sa.contable.repositorios;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sa.contable.DTO.DeudaClienteDTO;
import com.sa.contable.DTO.MovimientoCuentaDTO;
import com.sa.contable.entidades.Venta;

@Repository
public interface VentaRepositorio extends JpaRepository<Venta, Long> {

    Optional<Venta> findByNumeroComprobante(String numeroComprobante);

    List<Venta> findByClienteId(Long clienteId);

    List<Venta> findByFechaBetween(LocalDateTime desde, LocalDateTime hasta);

    boolean existsByNumeroComprobante(String numeroComprobante);

    // 1. QUERY GENERAL: Limpiada sin el atributo 'anulada'
    @Query("SELECT new com.sa.contable.DTO.DeudaClienteDTO(v.cliente.id, v.cliente.nombre, SUM(v.total)) "
            + "FROM Venta v "
            + "WHERE v.estado = 'PENDIENTE' "
            + "GROUP BY v.cliente.id, v.cliente.nombre")
    List<DeudaClienteDTO> findSaldosDeudores();

    // 2. QUERY ESPECÍFICA: Corregida para filtrar solo por el String de estado
// Cambiamos el tipo de retorno a List<MovimientoCuentaDTO>
    @Query("SELECT new com.sa.contable.DTO.MovimientoCuentaDTO(v.id, v.fecha, v.numeroComprobante, v.total) "
            + "FROM Venta v "
            + "WHERE v.cliente.id = :clienteId AND v.estado = 'PENDIENTE' AND v.fecha BETWEEN :desde AND :hasta")
    List<MovimientoCuentaDTO> findVentasPendientesByClienteId(
            @Param("clienteId") Long clienteId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);       

    List<Venta> findByClienteIdAndEstadoOrderByFechaAsc(Long clienteId, String estado);
}
