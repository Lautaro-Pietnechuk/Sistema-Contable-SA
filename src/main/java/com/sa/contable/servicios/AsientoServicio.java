package com.sa.contable.servicios;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import com.sa.contable.dto.AsientoDTO;
import com.sa.contable.dto.CuentaAsientoDTO;
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

    @Transactional
public Asiento crearAsiento(AsientoDTO asientoDTO, Long usuarioId) {
    Usuario usuario = usuarioRepositorio.findById(usuarioId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

    Asiento asiento = new Asiento();

    // Restar un día a la fecha
    LocalDate fechaAjustada = asientoDTO.getFecha().minusDays(1);
    asiento.setFecha(fechaAjustada);  // Usa la fecha ajustada aquí

    asiento.setDescripcion(asientoDTO.getDescripcion());
    asiento.setId_usuario(usuario.getId());

    if (asientoDTO.getMovimientos().isEmpty()) {
        throw new IllegalArgumentException("El asiento debe tener al menos un movimiento.");
    }

    // Guardar el asiento primero
    Asiento asientoGuardado = asientoRepositorio.save(asiento);

    // Lista para almacenar los movimientos
    List<CuentaAsiento> movimientos = new ArrayList<>();

    // Procesar cada movimiento
    for (CuentaAsientoDTO movimientoDTO : asientoDTO.getMovimientos()) {
        Cuenta cuenta = cuentaRepositorio.findByCodigo(movimientoDTO.getCuentaCodigo());
        if (cuenta == null) {
            throw new IllegalArgumentException("La cuenta con código " + movimientoDTO.getCuentaCodigo() + " no existe.");
        }

        // Crear y guardar movimiento
        CuentaAsiento nuevoMovimiento = new CuentaAsiento();
        nuevoMovimiento.setCuenta(cuenta);
        nuevoMovimiento.setAsiento(asientoGuardado);
        nuevoMovimiento.setDebe(movimientoDTO.getDebe());
        nuevoMovimiento.setHaber(movimientoDTO.getHaber());

        BigDecimal saldoActual = Optional.ofNullable(cuenta.getSaldoActual()).orElse(BigDecimal.ZERO);
        BigDecimal nuevoSaldo = calcularNuevoSaldo(cuenta, saldoActual, movimientoDTO);

        nuevoMovimiento.setSaldo(nuevoSaldo);
        cuentaAsientoRepositorio.save(nuevoMovimiento);

        // Agregar movimiento a la lista de movimientos del asiento
        movimientos.add(nuevoMovimiento);

        // Actualizar el saldo de la cuenta
        cuenta.setSaldoActual(nuevoSaldo);
        cuentaRepositorio.save(cuenta);
    }

    // Asignar la lista de movimientos al asiento guardado, convirtiendo a Set
    asientoGuardado.setCuentasAsientos(new HashSet<>(movimientos)); // Aquí convertimos a Set
    asientoRepositorio.save(asientoGuardado); // Guardar de nuevo para actualizar las relaciones

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
