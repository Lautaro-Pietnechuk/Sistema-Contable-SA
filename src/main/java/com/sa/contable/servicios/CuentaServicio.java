package com.sa.contable.servicios;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sa.contable.entidades.Cuenta;
import com.sa.contable.repositorios.CuentaRepositorio;

@Service
public class CuentaServicio {

    private static final Logger logger = LoggerFactory.getLogger(CuentaServicio.class);

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
    public Optional<Cuenta> obtenerCuentaPorCodigo(Long codigo) {
        return cuentaRepositorio.findById(codigo);
    }

    @Transactional
    public Cuenta actualizarCuenta(Long codigo, Cuenta cuentaActualizada) {
        // Buscamos la cuenta existente
        Cuenta cuentaExistente = cuentaRepositorio.findById(codigo)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada con ID: " + codigo));
        
        // Actualizamos los campos necesarios
        cuentaExistente.setNombre(cuentaActualizada.getNombre());
        cuentaExistente.setCodigo(cuentaActualizada.getCodigo());
        cuentaExistente.setTipo(cuentaActualizada.getTipo());
        cuentaExistente.setRecibeSaldo(cuentaActualizada.getRecibeSaldo());
        cuentaExistente.setSaldoActual(cuentaActualizada.getSaldoActual());
        
        return cuentaRepositorio.save(cuentaExistente);
    }

    @Transactional
    public void eliminarCuenta(Long codigo) {
        // Verificamos si la cuenta existe antes de eliminar
        if (!cuentaRepositorio.existsById(codigo)) {
            throw new RuntimeException("Cuenta no encontrada con ID: " + codigo);
        }
        cuentaRepositorio.deleteById(codigo);
    }

    @Transactional(readOnly = true)
    public List<Cuenta> obtenerCuentasPorPadre(Cuenta cuentaPadre) {
        return cuentaRepositorio.findByCuentaPadre(cuentaPadre);
    }

    @Transactional(readOnly = true)
    public List<Cuenta> obtenerCuentasRecibeSaldo() {
    return cuentaRepositorio.findAll()
            .stream()
            .filter(Cuenta::getRecibeSaldo) // Filtrar solo las cuentas que reciben saldo
            .collect(Collectors.toList());
    }

    public Cuenta buscarPorCodigo(Long codigo) {
        return cuentaRepositorio.findById(codigo)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada con ID: " + codigo));
    }
}
