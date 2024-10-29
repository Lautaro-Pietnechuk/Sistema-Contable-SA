package com.sa.contable.servicios;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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

        return movimientos.stream()
                .map(mov -> new LibroMayorDTO(mov.getAsiento().getFecha(), mov.getAsiento().getDescripcion(), mov.getSaldo()))
                .collect(Collectors.toList());
    }
}
