package com.sa.contable.servicios;


import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sa.contable.entidades.CuentaAsiento;
import com.sa.contable.repositorios.CuentaAsientoRepositorio;

import jakarta.transaction.Transactional;

@Service
public class CuentaAsientoServicio {

    @Autowired
    private CuentaAsientoRepositorio cuentaAsientoRepositorio;

    private static final Logger logger = LoggerFactory.getLogger(CuentaAsientoServicio.class);


    @Transactional
    public void crearMovimiento(CuentaAsiento movimiento) {
        logger.info("Iniciando la creación de movimiento: {}", movimiento);
    
        // Validar que la cuenta asociada no sea nula
        if (movimiento.getCuenta() == null) {
            logger.error("El movimiento debe tener una cuenta asociada: {}", movimiento);
            throw new IllegalArgumentException("El movimiento debe tener una cuenta válida.");
        }
    
        // Validar que el saldo sea mayor o igual a cero
        if (movimiento.getSaldo() == null || movimiento.getSaldo().compareTo(BigDecimal.ZERO) < 0) {
            logger.error("El saldo no puede ser nulo o negativo: {}", movimiento.getSaldo());
            throw new IllegalArgumentException("El saldo no puede ser negativo.");
        }
    
        // Validar que el asiento asociado no sea nulo
        if (movimiento.getAsiento() == null) {
            logger.error("El movimiento debe pertenecer a un asiento: {}", movimiento);
            throw new IllegalArgumentException("El movimiento debe tener un asiento válido.");
        }
    
        // Guardar movimiento si todas las validaciones son exitosas
        try {
            cuentaAsientoRepositorio.save(movimiento);
            logger.info("Movimiento guardado con éxito: {}", movimiento);
        } catch (Exception e) {
            logger.error("Error al guardar el movimiento: {}", e.getMessage(), e);
            throw new RuntimeException("Hubo un error al guardar el movimiento. Inténtalo de nuevo.");
        }
    }
    
    
    
}
