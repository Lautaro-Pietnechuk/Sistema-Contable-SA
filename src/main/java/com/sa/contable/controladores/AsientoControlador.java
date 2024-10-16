package com.sa.contable.controladores;



import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sa.contable.dto.AsientoDTO;
import com.sa.contable.entidades.Asiento;
import com.sa.contable.servicios.AsientoServicio;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/asientos")
public class AsientoControlador {

    @Autowired
    private AsientoServicio asientoServicio;

    // Endpoint para crear un nuevo asiento contable
    @PostMapping("/crear")
    public ResponseEntity<Asiento> crearAsiento(@RequestBody AsientoDTO asientoDTO) {
        Asiento nuevoAsiento = asientoServicio.crearAsiento(asientoDTO);
        return ResponseEntity.ok(nuevoAsiento);
    }

    // Endpoint para listar todos los asientos
    @GetMapping("/listar")
    public ResponseEntity<List<Asiento>> listarAsientos() {
        List<Asiento> asientos = asientoServicio.listarAsientos();
        return ResponseEntity.ok(asientos);
    }
}


