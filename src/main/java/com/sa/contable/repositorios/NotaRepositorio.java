package com.sa.contable.repositorios;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sa.contable.entidades.Nota;

@Repository
public interface NotaRepositorio extends JpaRepository<Nota, Long> {
    
    // Método para traer todas las notas (débitos/créditos) de una venta
    List<Nota> findByIdVenta(Long idVenta);
}