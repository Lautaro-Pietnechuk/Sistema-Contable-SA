package com.sa.contable.Cuenta;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cuentas")
public class CuentaControlador {

    @Autowired
    private CuentaServicio cuentaServicio;

    @PreAuthorize("hasRole('Administrador')")
    @GetMapping
    public List<Cuenta> listarCuentas() {
        System.out.println("Solicitud recibida para listar cuentas."); // Log de depuración
        List<Cuenta> cuentas = cuentaServicio.obtenerTodasLasCuentas();
        System.out.println("Cuentas obtenidas: " + cuentas); // Log de depuración
        return cuentas;
    }

    @PreAuthorize("hasRole('Administrador')")
    @PostMapping
    public Cuenta crearCuenta(@RequestBody Cuenta cuenta) {
        return cuentaServicio.crearCuenta(cuenta);
    }

    @PreAuthorize("hasRole('Administrador')")
    @PutMapping("/{id}")
    public Cuenta actualizarCuenta(@PathVariable Long id, @RequestBody Cuenta cuenta) {
        return cuentaServicio.actualizarCuenta(id, cuenta);
    }

    @PreAuthorize("hasRole('Administrador')")
    @DeleteMapping("/{id}")
    public void eliminarCuenta(@PathVariable Long id) {
        cuentaServicio.eliminarCuenta(id);
    }
}