package com.sa.contable.Cuenta;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CuentaServicio {

    @Autowired
    private CuentaRepositorio cuentaRepositorio;

    @Transactional
    public Cuenta crearCuenta(Cuenta cuenta) {
        // Aquí puedes agregar validaciones si es necesario
        return cuentaRepositorio.save(cuenta);
    }

    @Transactional(readOnly = true)
    public List<Cuenta> obtenerTodasLasCuentas() {
        return cuentaRepositorio.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Cuenta> obtenerCuentaPorId(Long id) {
        return cuentaRepositorio.findById(id);
    }

    @Transactional
    public Cuenta actualizarCuenta(Long id, Cuenta cuentaActualizada) {
        // Buscamos la cuenta existente
        Cuenta cuentaExistente = cuentaRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada con ID: " + id));
        
        // Actualizamos los campos necesarios
        cuentaExistente.setNombre(cuentaActualizada.getNombre());
        cuentaExistente.setCodigo(cuentaActualizada.getCodigo());
        cuentaExistente.setTipo(cuentaActualizada.getTipo());
        cuentaExistente.setRecibeSaldo(cuentaActualizada.getRecibeSaldo());
        cuentaExistente.setSaldoActual(cuentaActualizada.getSaldoActual());
        
        return cuentaRepositorio.save(cuentaExistente);
    }

    @Transactional
    public void eliminarCuenta(Long id) {
        // Verificamos si la cuenta existe antes de eliminar
        if (!cuentaRepositorio.existsById(id)) {
            throw new RuntimeException("Cuenta no encontrada con ID: " + id);
        }
        cuentaRepositorio.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Cuenta> obtenerCuentasPorPadre(Cuenta cuentaPadre) {
        return cuentaRepositorio.findByCuentaPadre(cuentaPadre);
    }
}
