package com.sa.contable.servicios;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.sa.contable.DTO.AsientoDTO;
import com.sa.contable.DTO.CuentaAsientoDTO;
import com.sa.contable.entidades.Asiento;
import com.sa.contable.entidades.Cuenta;
import com.sa.contable.entidades.CuentaAsiento;
import com.sa.contable.entidades.Usuario;
import com.sa.contable.repositorios.AsientoRepositorio;
import com.sa.contable.repositorios.CuentaAsientoRepositorio;
import com.sa.contable.repositorios.CuentaRepositorio;
import com.sa.contable.repositorios.UsuarioRepositorio;

import jakarta.transaction.Transactional;

@Service
public class AsientoServicio {

    private static final Logger logger = LoggerFactory.getLogger(AsientoServicio.class);

    @Autowired
    private CuentaAsientoRepositorio cuentaAsientoRepositorio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private AsientoRepositorio asientoRepositorio;

    @Autowired
    private CuentaRepositorio cuentaRepositorio;

    @Autowired
    private CuentaAsientoServicio cuentaAsientoServicio;

    @Transactional
    public Asiento crearAsiento(AsientoDTO asientoDTO, Long usuarioId) {
        logger.info("Iniciando creación de asiento contable. Usuario ID: {}, Descripción: '{}'", usuarioId, asientoDTO.getDescripcion());

        Usuario usuario = usuarioRepositorio.findById(usuarioId)
                .orElseThrow(() -> {
                    logger.error("Error al crear asiento: Usuario con ID {} no encontrado.", usuarioId);
                    return new IllegalArgumentException("Usuario no encontrado");
                });

        if (asientoDTO.getFecha() == null) {
            logger.error("Error al crear asiento: La fecha enviada en el DTO es nula.");
            throw new IllegalArgumentException("La fecha del asiento no puede ser nula.");
        }

        Asiento asiento = new Asiento();

        // Restar un día a la fecha
        LocalDate fechaAjustada = asientoDTO.getFecha().minusDays(1);
        logger.debug("Ajuste de fecha aplicado. Original: {} | Ajustada (-1 día): {}", asientoDTO.getFecha(), fechaAjustada);
        asiento.setFecha(fechaAjustada);  // Usa la fecha ajustada aquí

        asiento.setDescripcion(asientoDTO.getDescripcion());
        asiento.setId_usuario(usuario.getId());

        if (asientoDTO.getMovimientos() == null || asientoDTO.getMovimientos().isEmpty()) {
            logger.error("Error al crear asiento: La lista de movimientos está vacía.");
            throw new IllegalArgumentException("El asiento debe tener al menos un movimiento.");
        }

        // Guardar el asiento primero
        Asiento asientoGuardado = asientoRepositorio.save(asiento);
        logger.info("Cabecera del asiento guardada exitosamente con ID: {}", asientoGuardado.getId());

        // Lista para almacenar los movimientos
        List<CuentaAsiento> movimientos = new ArrayList<>();
        
        // Acumuladores para chequear partida doble en los logs
        BigDecimal totalDebe = BigDecimal.ZERO;
        BigDecimal totalHaber = BigDecimal.ZERO;

        logger.info("Procesando {} movimientos para el asiento ID: {}", asientoDTO.getMovimientos().size(), asientoGuardado.getId());

        // Procesar cada movimiento
        for (CuentaAsientoDTO movimientoDTO : asientoDTO.getMovimientos()) {
            if (movimientoDTO.getDebe() == null && movimientoDTO.getHaber() == null) {
                logger.error("Movimiento rechazado en asiento ID {}: Debe y Haber son nulos.", asientoGuardado.getId());
                throw new IllegalArgumentException("Debe y Haber no pueden ser ambos nulos para un movimiento.");
            }

            Cuenta cuenta = cuentaRepositorio.findByCodigo(movimientoDTO.getCuentaCodigo());
            if (cuenta == null) {
                logger.error("Movimiento rechazado: La cuenta con código {} no existe en el plan de cuentas.", movimientoDTO.getCuentaCodigo());
                throw new IllegalArgumentException("La cuenta con código " + movimientoDTO.getCuentaCodigo() + " no existe.");
            }

            if (cuenta.getTipo() == null) {
                logger.error("Movimiento rechazado: La cuenta '{}' (Código: {}) no tiene un tipo definido.", cuenta.getNombre(), cuenta.getCodigo());
                throw new IllegalArgumentException("El tipo de cuenta no puede ser nulo para la cuenta con código: " + movimientoDTO.getCuentaCodigo());
            }

            // Sanitizar nulos a cero para la matemática
            BigDecimal debeMonto = Optional.ofNullable(movimientoDTO.getDebe()).orElse(BigDecimal.ZERO);
            BigDecimal haberMonto = Optional.ofNullable(movimientoDTO.getHaber()).orElse(BigDecimal.ZERO);

            totalDebe = totalDebe.add(debeMonto);
            totalHaber = totalHaber.add(haberMonto);

            // Crear y guardar movimiento
            CuentaAsiento nuevoMovimiento = new CuentaAsiento();
            nuevoMovimiento.setCuenta(cuenta);
            nuevoMovimiento.setAsiento(asientoGuardado);
            nuevoMovimiento.setDebe(movimientoDTO.getDebe());
            nuevoMovimiento.setHaber(movimientoDTO.getHaber());

            BigDecimal saldoActual = Optional.ofNullable(cuenta.getSaldoActual()).orElse(BigDecimal.ZERO);
            BigDecimal nuevoSaldo = calcularNuevoSaldo(cuenta, saldoActual, movimientoDTO);

            nuevoMovimiento.setSaldo(nuevoSaldo);

            // Agregar movimiento a la lista de movimientos del asiento
            movimientos.add(nuevoMovimiento);
            cuentaAsientoServicio.crearMovimiento(nuevoMovimiento);

            // Actualizar el saldo de la cuenta
            cuenta.setSaldoActual(nuevoSaldo);
            cuentaRepositorio.save(cuenta);

            logger.info("Movimiento registrado -> Cuenta: {} | Debe: ${} | Haber: ${} | Nuevo Saldo: ${}", 
                    cuenta.getCodigo(), debeMonto, haberMonto, nuevoSaldo);
        }

        // Mini auditoría de partida doble exclusiva para la consola
        if (totalDebe.compareTo(totalHaber) != 0) {
            logger.warn("¡ATENCIÓN! El asiento ID {} está descuadrado. Total Debe: ${} | Total Haber: ${}", asientoGuardado.getId(), totalDebe, totalHaber);
        } else {
            logger.info("Asiento ID {} cuadrado perfectamente. Balance: ${}", asientoGuardado.getId(), totalDebe);
        }

        // Asignar la lista de movimientos al asiento guardado, convirtiendo a Set
        asientoGuardado.setCuentasAsientos(new HashSet<>(movimientos)); // Aquí convertimos a Set
        asientoRepositorio.save(asientoGuardado); // Guardar de nuevo para actualizar las relaciones

        logger.info("Transacción completada: Asiento ID {} generado y vinculado con éxito.", asientoGuardado.getId());

        return asientoGuardado;
    }

    private BigDecimal calcularNuevoSaldo(Cuenta cuenta, BigDecimal saldoActual, CuentaAsientoDTO movimientoDTO) {
        BigDecimal debe = Optional.ofNullable(movimientoDTO.getDebe()).orElse(BigDecimal.ZERO);
        BigDecimal haber = Optional.ofNullable(movimientoDTO.getHaber()).orElse(BigDecimal.ZERO);

        switch (cuenta.getTipo().toLowerCase()) {
            case "activo":
            case "egreso":
                return saldoActual.add(debe).subtract(haber);
            case "pasivo":
            case "patrimonio":
            case "ingreso":
                return saldoActual.subtract(debe).add(haber);
            default:
                throw new IllegalArgumentException("Tipo de cuenta desconocido: " + cuenta.getTipo());
        }
    }

    // Método con Paginación para Listar Asientos
    public Page<Asiento> listarAsientos(LocalDate inicio, LocalDate fin, Pageable pageable) {
        return asientoRepositorio.findAllBetweenDates(inicio, fin, pageable);
    }

    public List<Asiento> listarTodosAsientosOrdenados(LocalDate inicio, LocalDate fin, boolean ascendente) {
        Comparator<Asiento> comparador = Comparator.comparing(Asiento::getId);
        if (!ascendente) {
            comparador = comparador.reversed();
        }

        return asientoRepositorio.findAllBetweenDates(inicio, fin)
                .stream()
                .sorted(comparador)
                .toList();
    }

    public Asiento buscarPorId(Long id) {
        return asientoRepositorio.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Asiento no encontrado"));
    }

    public boolean tieneCuentaAsientos(Long asientoId) {
        return cuentaAsientoRepositorio.existsByAsientoId(asientoId);
    }

    @Transactional
    public void eliminarAsiento(Long id) {
        if (!asientoRepositorio.existsById(id)) {
            throw new RuntimeException("Asiento no encontrado con ID: " + id);
        }
        asientoRepositorio.deleteById(id);
    }
}
