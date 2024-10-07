package com.sa.contable.Asiento;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/asientos")
public class AsientoController {

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
