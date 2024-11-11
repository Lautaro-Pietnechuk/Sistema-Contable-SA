package com.sa.contable.servicios;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sa.contable.dto.LibroMayorDTO;
import com.sa.contable.entidades.CuentaAsiento;
import com.sa.contable.repositorios.CuentaAsientoRepositorio;

@Service
public class LibroMayorServicio {

    @Autowired
    private CuentaAsientoRepositorio cuentaAsientoRepositorio;

    public List<LibroMayorDTO> generarLibroMayor(String codigoCuenta, LocalDate fechaInicio, LocalDate fechaFin) {
        List<CuentaAsiento> movimientos = cuentaAsientoRepositorio.findByCuentaAndFechaBetween(codigoCuenta, fechaInicio, fechaFin);

        final Logger logger = LoggerFactory.getLogger(LibroMayorServicio.class);
    
        // Agregar un log que muestra el saldo de cada movimiento
        movimientos.stream().forEach(mov -> 
            logger.info("Fecha: {}, Descripción: {}, Debe: {}, Haber: {}, Saldo: {}, TipoCuenta: {}", 
                        mov.getAsiento().getFecha(), 
                        mov.getAsiento().getDescripcion(), 
                        mov.getDebe(), 
                        mov.getHaber(), 
                        mov.getSaldo(),
                        mov.getCuenta().getTipo()) // Mostrar tipoCuenta
        );
    
        return movimientos.stream()
                .map(mov -> new LibroMayorDTO(
                        mov.getAsiento().getFecha(),
                        mov.getAsiento().getDescripcion(),
                        mov.getDebe(), // Obtener el debe
                        mov.getHaber(), // Obtener el haber
                        mov.getSaldo(), // Obtener el saldo
                        mov.getCuenta().getTipo())) // Obtener el tipoCuenta
                .collect(Collectors.toList());
    } 

}
