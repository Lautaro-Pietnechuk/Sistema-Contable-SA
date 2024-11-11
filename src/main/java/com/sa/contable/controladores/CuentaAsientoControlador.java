package com.sa.contable.controladores;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sa.contable.dto.CuentaAsientoDTO;
import com.sa.contable.entidades.Cuenta;
import com.sa.contable.entidades.CuentaAsiento;
import com.sa.contable.repositorios.CuentaAsientoRepositorio;
import com.sa.contable.repositorios.CuentaRepositorio;
import com.sa.contable.servicios.AsientoServicio;
import com.sa.contable.servicios.CuentaServicio;

import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/api/movimientos")
public class CuentaAsientoControlador {

    @Autowired
    private CuentaServicio cuentaServicio;

    @Autowired
    private AsientoServicio asientoServicio;

    @Autowired
    private CuentaAsientoRepositorio cuentaAsientoRepositorio;  // Inyectar correctamente el repositorio

    @Autowired
    private CuentaRepositorio cuentaRepositorio;

    

    private static final Logger logger = LoggerFactory.getLogger(CuentaAsientoControlador.class);

    @PostMapping("/crear")
public ResponseEntity<?> crearMovimiento(@RequestBody CuentaAsientoDTO movimientoDTO) {
    logger.info("Iniciando la creación de movimiento: {}", movimientoDTO);
    try {
        // Crear y configurar el nuevo movimiento
        CuentaAsiento nuevoMovimiento = new CuentaAsiento();

        if (movimientoDTO.getCuentaCodigo() == null) {
            return ResponseEntity.badRequest().body("El ID de la cuenta no puede ser null");
        }
        
        // Buscar la cuenta por su código
        Cuenta cuenta = cuentaServicio.buscarPorCodigo(movimientoDTO.getCuentaCodigo());
        if (cuenta == null) {
            return ResponseEntity.badRequest().body("La cuenta con el código proporcionado no existe.");
        }
        
        logger.info("Cuenta encontrada: {}", cuenta.getCodigo());
        nuevoMovimiento.setCuenta(cuenta);
        nuevoMovimiento.setDebe(movimientoDTO.getDebe());
        logger.info("Debe: {}", nuevoMovimiento.getDebe());
        nuevoMovimiento.setHaber(movimientoDTO.getHaber());
        logger.info("Haber: {}", nuevoMovimiento.getHaber());
        nuevoMovimiento.setAsiento(asientoServicio.buscarPorId(movimientoDTO.getAsientoId()));
        logger.info("Asiento encontrado: {}", nuevoMovimiento.getAsiento());

        // Calcular el saldo de la cuenta tras el movimiento
        BigDecimal saldoActual = cuenta.getSaldoActual();
        BigDecimal nuevoSaldo = saldoActual.add(movimientoDTO.getDebe()).subtract(movimientoDTO.getHaber());

        // Validar si el nuevo saldo es negativo
        if (nuevoSaldo.compareTo(BigDecimal.ZERO) < 0) {
            return ResponseEntity.badRequest().body("No se puede crear el movimiento porque el saldo de la cuenta resultaría negativo.");
        }

        // Si el saldo es válido, establecer el nuevo saldo en el movimiento
        nuevoMovimiento.setSaldo(nuevoSaldo);
        logger.info("Nuevo saldo calculado: {}", nuevoMovimiento.getSaldo());

        // Guardar el movimiento en la base de datos
        cuentaAsientoRepositorio.save(nuevoMovimiento);

        // Actualizar el saldo de la cuenta
        cuenta.setSaldoActual(nuevoSaldo);
        
        logger.info("Movimiento creado con éxito: {}", nuevoMovimiento);
        return ResponseEntity.ok("Movimiento creado con éxito");
    } catch (Exception e) {
        logger.error("Error al crear el movimiento: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
}

    


    @Transactional
public Cuenta actualizarSaldoCuenta(Long codigo, BigDecimal nuevoSaldo) {
    // Buscar la cuenta existente
    Cuenta cuentaExistente = cuentaRepositorio.findById(codigo)
            .orElseThrow(() -> new RuntimeException("Cuenta no encontrada con ID: " + codigo));
    
    // Actualizamos el saldo de la cuenta
    cuentaExistente.setSaldoActual(nuevoSaldo);

    // Guardamos la cuenta con el saldo actualizado
    return cuentaRepositorio.save(cuentaExistente);
}
}
