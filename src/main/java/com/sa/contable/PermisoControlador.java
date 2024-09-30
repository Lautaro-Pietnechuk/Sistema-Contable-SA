package com.sa.contable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permisos")
public class PermisoControlador {

    @Autowired
    private PermisoServicio permisoServicio;

    @PostMapping
    public ResponseEntity<Permiso> crearPermiso(@RequestBody Permiso permiso) {
        return ResponseEntity.ok(permisoServicio.crearPermiso(permiso));
    }

    @GetMapping
    public ResponseEntity<List<Permiso>> obtenerPermisos() {
        return ResponseEntity.ok(permisoServicio.obtenerTodosLosPermisos());
    }
}