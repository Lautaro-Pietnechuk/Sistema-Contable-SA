package com.sa.contable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RolControlador {

    @Autowired
    private RolServicio rolServicio;

    @PostMapping
    public ResponseEntity<Rol> crearRol(@RequestBody Rol rol) {
        return ResponseEntity.ok(rolServicio.crearRol(rol));
    }

    @GetMapping
    public ResponseEntity<List<Rol>> obtenerRoles() {
        return ResponseEntity.ok(rolServicio.obtenerTodosLosRoles());
    }
}

