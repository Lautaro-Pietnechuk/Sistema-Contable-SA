package com.sa.contable.Cuenta;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CuentaServicio {

    @Autowired
    private CuentaRepositorio cuentaRepositorio;

    public Cuenta crearCuenta(Cuenta cuenta) {
        return cuentaRepositorio.save(cuenta);
    }

    public List<Cuenta> obtenerTodasLasCuentas() {
        return cuentaRepositorio.findAll();
    }

    public Optional<Cuenta> obtenerCuentaPorId(Long id) {
        return cuentaRepositorio.findById(id);
    }

    public Cuenta actualizarCuenta(Cuenta cuenta) {
        return cuentaRepositorio.save(cuenta);
    }

    public void eliminarCuenta(Long id) {
        cuentaRepositorio.deleteById(id);
    }

    public List<Cuenta> obtenerCuentasPorPadre(Cuenta cuentaPadre) {
        return cuentaRepositorio.findByCuentaPadre(cuentaPadre);
    }
}
