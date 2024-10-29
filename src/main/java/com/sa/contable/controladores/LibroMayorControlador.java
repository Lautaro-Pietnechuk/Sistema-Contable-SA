package com.sa.contable.controladores;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sa.contable.dto.LibroMayorDTO;
import com.sa.contable.servicios.LibroMayorServicio;

@RestController
@RequestMapping("/api")
public class LibroMayorControlador {

    @Autowired
    private LibroMayorServicio libroMayorServicio;

    @GetMapping("/libroMayor")
    public ResponseEntity<List<LibroMayorDTO>> obtenerLibroMayor(
            @RequestParam String codigoCuenta,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        try {
            List<LibroMayorDTO> libroMayor = libroMayorServicio.generarLibroMayor(codigoCuenta, fechaInicio, fechaFin);
            return ResponseEntity.ok(libroMayor);
        } catch (Exception e) {
            // Manejo del error: devuelve una respuesta adecuada
            return ResponseEntity.badRequest().body(null); // Cambia null por un objeto adecuado si es necesario
        }
    }
}
