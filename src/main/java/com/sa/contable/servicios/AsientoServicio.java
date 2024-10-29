package com.sa.contable.servicios;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.sa.contable.dto.AsientoDTO;
import com.sa.contable.entidades.Asiento;
import com.sa.contable.entidades.CuentaAsiento;
import com.sa.contable.entidades.Usuario;
import com.sa.contable.repositorios.AsientoRepositorio;
import com.sa.contable.repositorios.CuentaAsientoRepositorio;
import com.sa.contable.repositorios.CuentaRepositorio;
import com.sa.contable.repositorios.UsuarioRepositorio;

import jakarta.transaction.Transactional;

@Service
public class AsientoServicio {

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
    if (usuarioId == null) {
        throw new IllegalArgumentException("El ID del usuario no puede ser nulo");
    }

    Usuario usuario = usuarioRepositorio.findById(usuarioId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

    Asiento asiento = new Asiento();
    asiento.setFecha(asientoDTO.getFecha());
    asiento.setDescripcion(asientoDTO.getDescripcion());
    asiento.setId_usuario(usuario.getId());

    // Verificar que el DTO incluya al menos un movimiento válido
    if (asientoDTO.getMovimientos() == null || asientoDTO.getMovimientos().isEmpty()) {
        throw new IllegalArgumentException("El asiento debe tener al menos un movimiento asociado.");
    }

    // Guardar los movimientos y asociarlos al asiento
    asientoDTO.getMovimientos().forEach(movimientoDTO -> {
        var cuentaAsiento = new CuentaAsiento();
        cuentaAsiento.setCuenta(cuentaRepositorio.findByCodigo(movimientoDTO.getCuentaCodigo()));
        cuentaAsiento.setSaldo(movimientoDTO.getSaldo());
        cuentaAsiento.setAsiento(asiento);

        if (cuentaAsiento.getSaldo() == null || cuentaAsiento.getSaldo().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El saldo de cada movimiento debe ser mayor o igual a cero.");
        }

        cuentaAsientoRepositorio.save(cuentaAsiento);
    });

    // Guardar el asiento con los movimientos
    return asientoRepositorio.save(asiento);
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
