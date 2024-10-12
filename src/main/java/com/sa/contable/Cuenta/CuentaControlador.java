package com.sa.contable.Cuenta;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sa.contable.Configuracion.JwtUtil;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/cuentas")
public class CuentaControlador {

    @Autowired
    private CuentaServicio cuentaServicio;

    @Autowired
    private JwtUtil jwtUtil; // Inyecta JWTUtil

    // Solo los administradores pueden crear cuentas
    @PreAuthorize("hasRole('ROLE_Administrador')")
    @PostMapping
    public ResponseEntity<Cuenta> crearCuenta(@RequestBody Cuenta cuenta) {
        Cuenta nuevaCuenta = cuentaServicio.crearCuenta(cuenta);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaCuenta);
    }

    @GetMapping
    public ResponseEntity<List<Cuenta>> obtenerCuentas() {
        List<Cuenta> cuentas = cuentaServicio.obtenerTodasLasCuentas();
        return ResponseEntity.ok(cuentas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cuenta> obtenerCuenta(@PathVariable Long id) {
        return cuentaServicio.obtenerCuentaPorId(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // Solo los administradores pueden actualizar cuentas
    @PreAuthorize("hasRole('ROLE_Administrador')")
    @PutMapping("/{id}")
    public ResponseEntity<Cuenta> actualizarCuenta(@PathVariable Long id, @RequestBody Cuenta cuenta) {
        cuenta.setId(id);
        Cuenta cuentaActualizada = cuentaServicio.actualizarCuenta(id, cuenta);
        return ResponseEntity.ok(cuentaActualizada);
    }

    // Solo los administradores pueden eliminar cuentas
    @PreAuthorize("hasRole('ROLE_Administrador')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCuenta(@PathVariable Long id) {
        cuentaServicio.eliminarCuenta(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/padre/{id}")
    public ResponseEntity<List<Cuenta>> obtenerSubCuentas(@PathVariable Long id) {
        return cuentaServicio.obtenerCuentaPorId(id)
            .map(cuentaPadre -> {
                List<Cuenta> subCuentas = cuentaServicio.obtenerCuentasPorPadre(cuentaPadre);
                return ResponseEntity.ok(subCuentas);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    // Método para validar el token antes de permitir el acceso a ciertos recursos
    @SuppressWarnings("unused")
    private boolean validarToken(String token) {
        return jwtUtil.esTokenValido(token);
    }
}
