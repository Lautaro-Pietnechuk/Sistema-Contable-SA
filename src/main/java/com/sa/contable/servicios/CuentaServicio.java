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
import com.sa.contable.repositorios.CuentaAsientoRepositorio;
import com.sa.contable.repositorios.CuentaRepositorio;

@Service
public class CuentaServicio {

    private static final Logger logger = LoggerFactory.getLogger(CuentaServicio.class);

    @Autowired
    private CuentaAsientoRepositorio cuentaAsientoRepositorio;

    @Autowired
    private CuentaRepositorio cuentaRepositorio;

    @Transactional
    public Cuenta crearCuenta(Cuenta cuenta) {
        // Validaciones antes de guardar
        if (cuenta.getNombre() == null || cuenta.getNombre().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la cuenta no puede estar vacío.");
        }
        // Aquí puedes agregar más validaciones si es necesario
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

    public boolean haSidoUtilizadaEnAsiento(Long cuentaId) {
        return cuentaAsientoRepositorio.existsByCuentaCodigo(cuentaId);
    }

    public void editarNombreCuenta(Long cuentaId, String nuevoNombre) {
        Cuenta cuenta = cuentaRepositorio.findById(cuentaId)
            .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
        
        cuenta.setNombre(nuevoNombre); // Asumiendo que 'nombre' es el atributo de la cuenta
        cuentaRepositorio.save(cuenta);
    }
}
