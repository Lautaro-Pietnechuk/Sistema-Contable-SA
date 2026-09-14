package com.sa.contable.repositorios;


import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sa.contable.entidades.Nota;

@Repository
public interface NotaRepositorio extends JpaRepository<Nota, Long> {
    
    // Método para traer todas las notas (débitos/créditos) de una venta
    List<Nota> findByIdVenta(Long idVenta);

            @org.springframework.data.jpa.repository.Query("SELECT n FROM Nota n JOIN Venta v ON v.id = n.idVenta "
                + "WHERE v.cliente.id = :clienteId AND v.estado <> 'ANULADA' "
                + "AND n.fecha BETWEEN :desde AND :hasta")
        List<Nota> findByClienteIdAndFechaBetween(
            @org.springframework.data.repository.query.Param("clienteId") Long clienteId,
            @org.springframework.data.repository.query.Param("desde") LocalDate desde,
            @org.springframework.data.repository.query.Param("hasta") LocalDate hasta);
}