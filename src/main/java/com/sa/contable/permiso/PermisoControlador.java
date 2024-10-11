package com.sa.contable.permiso;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/permisos")
public class PermisoControlador {

    @Autowired
    private PermisoServicio permisoServicio;

    @GetMapping
    public List<Permiso> listarPermisos() {
        return permisoServicio.listarPermisos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Permiso> obtenerPermisoPorId(@PathVariable Long id) {
        Optional<Permiso> permisoOptional = permisoServicio.obtenerPermisoPorId(id);
        if (permisoOptional.isPresent()) {
            return ResponseEntity.ok(permisoOptional.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public Permiso crearPermiso(@RequestBody Permiso permiso) {
        return permisoServicio.guardarPermiso(permiso);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPermiso(@PathVariable Long id) {
        permisoServicio.eliminarPermiso(id);
        return ResponseEntity.noContent().build();
    }
}