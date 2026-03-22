package com.sa.contable.controladores;



import com.sa.contable.entidades.Nota;
import com.sa.contable.servicios.NotaServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notas")
@CrossOrigin(origins = "*") // Ajustá esto según tu config de seguridad
public class NotaControlador {

    @Autowired
    private NotaServicio notaServicio;

    @GetMapping
    public ResponseEntity<List<Nota>> obtenerTodas() {
        return ResponseEntity.ok(notaServicio.obtenerTodas());
    }

    @GetMapping("/venta/{idVenta}")
    public ResponseEntity<List<Nota>> obtenerPorVenta(@PathVariable Long idVenta) {
        List<Nota> notas = notaServicio.obtenerPorVenta(idVenta);
        return ResponseEntity.ok(notas);
    }

    @PostMapping
    public ResponseEntity<Nota> crearNota(@RequestBody Nota nota) {
        Nota nuevaNota = notaServicio.crearNota(nota);
        return new ResponseEntity<>(nuevaNota, HttpStatus.CREATED);
    }
}