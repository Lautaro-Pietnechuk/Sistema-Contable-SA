package com.sa.contable.servicios;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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

        if (asientoDTO.getMovimientos() == null || asientoDTO.getMovimientos().isEmpty()) {
            throw new IllegalArgumentException("El asiento debe tener al menos un movimiento asociado.");
        }

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

        return asientoRepositorio.save(asiento);
    }

    public Page<Asiento> listarAsientos(LocalDate inicio, LocalDate fin, Pageable pageable) {
        return asientoRepositorio.findAllBetweenDatesPaged(inicio, fin, pageable);
    }

    public Page<Asiento> listarAsientosPorCuenta(LocalDate inicio, LocalDate fin, Long cuentaCodigo, Pageable pageable) {
        return asientoRepositorio.findAllByCuentaAndDates(cuentaCodigo, inicio, fin, pageable);
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
