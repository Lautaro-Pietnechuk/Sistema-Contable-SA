package com.sa.contable.controladores;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sa.contable.entidades.Movimiento;
import com.sa.contable.servicios.MovimientoServicio;

@RestController
@RequestMapping("/api/movimientos")
public class MovimientoControlador {

    @Autowired
    private MovimientoServicio movimientoServicio;

    @PostMapping("/crear")
    public ResponseEntity<String> crearMovimiento(@RequestBody Movimiento nuevoMovimiento) {
        try {
            movimientoServicio.crearMovimiento(nuevoMovimiento);
            return ResponseEntity.ok("Movimiento creado con éxito.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al crear el movimiento: " + e.getMessage());
        }
    }
}
