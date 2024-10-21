package com.sa.contable.controladores;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sa.contable.entidades.CuentaAsiento;
import com.sa.contable.repositorios.CuentaAsientoRepositorio;
import com.sa.contable.servicios.CuentaServicio;
import com.sa.contable.servicios.AsientoServicio;
import com.sa.contable.dto.CuentaAsientoDTO;

@RestController
@RequestMapping("/api/movimientos")
public class CuentaAsientoControlador {

    @Autowired
    private CuentaServicio cuentaServicio;

    @Autowired
    private AsientoServicio asientoServicio;

    @Autowired
    private CuentaAsientoRepositorio cuentaAsientoRepositorio;  // Inyectar correctamente el repositorio

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
        
        nuevoMovimiento.setCuenta(cuentaServicio.buscarPorCodigo(movimientoDTO.getCuentaCodigo()));
        logger.info("Cuenta encontrada: {}", nuevoMovimiento.getCuenta().getCodigo());
        nuevoMovimiento.setDebe(movimientoDTO.getDebe());
        logger.info("Debe: {}", nuevoMovimiento.getDebe());
        nuevoMovimiento.setHaber(movimientoDTO.getHaber());
        logger.info("Haber: {}", nuevoMovimiento.getHaber());
        nuevoMovimiento.setAsiento(asientoServicio.buscarPorId(movimientoDTO.getAsientoId()));
        logger.info("Asiento encontrado: {}", nuevoMovimiento.getAsiento());
        nuevoMovimiento.setSaldo(movimientoDTO.getSaldo());  // Establecer el saldo
        logger.info("Saldo: {}", nuevoMovimiento.getSaldo());

        // Guardar el movimiento en la base de datos
        cuentaAsientoRepositorio.save(nuevoMovimiento);

        logger.info("Movimiento creado con éxito: {}", nuevoMovimiento);
        return ResponseEntity.ok("Movimiento creado con éxito");
    } catch (Exception e) {
        logger.error("Error al crear el movimiento: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
}

}
