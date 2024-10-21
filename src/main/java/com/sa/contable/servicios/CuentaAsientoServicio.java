package com.sa.contable.servicios;


import org.hibernate.boot.model.internal.Nullability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sa.contable.entidades.CuentaAsiento;
import com.sa.contable.repositorios.CuentaAsientoRepositorio;

@Service
public class CuentaAsientoServicio {

    @Autowired
    private CuentaAsientoRepositorio cuentaAsientoRepositorio;

    private static final Logger logger = LoggerFactory.getLogger(CuentaAsientoServicio.class);


    public void crearMovimiento(CuentaAsiento movimiento) {
        logger.info("Iniciando la creación de movimiento: {}", movimiento);
        // Asegúrate de que los datos sean válidos antes de guardar
        if (movimiento.getCuenta() == null || movimiento.getSaldo() < 0 || movimiento.getAsiento() == null) {
            logger.warn("Datos inválidos para el movimiento: {}", movimiento);
            throw new IllegalArgumentException("Datos inválidos para el movimiento");
        }
        cuentaAsientoRepositorio.save(movimiento);
        logger.info("Movimiento guardado con éxito: {}", movimiento);
    }
    
}
