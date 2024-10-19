package com.sa.contable.servicios;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sa.contable.entidades.Movimiento;
import com.sa.contable.repositorios.MovimientoRepositorio;

@Service
public class MovimientoServicio {

    @Autowired
    private MovimientoRepositorio movimientoRepositorio;

    public void crearMovimiento(Movimiento movimiento) {
        // Aquí puedes realizar validaciones o lógica adicional antes de guardar
        movimientoRepositorio.save(movimiento);
    }
}
