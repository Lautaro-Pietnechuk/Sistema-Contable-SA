package com.sa.contable.servicios;

import java.sql.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sa.contable.dto.AsientoDTO;
import com.sa.contable.entidades.Asiento;
import com.sa.contable.entidades.CuentaAsiento;
import com.sa.contable.entidades.Usuario;
import com.sa.contable.repositorios.AsientoRepositorio;
import com.sa.contable.repositorios.CuentaAsientoRepositorio;
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

    @Transactional
    public Asiento crearAsiento(AsientoDTO asientoDTO, Long usuarioId) {
        // Validar que el ID del usuario no sea nulo
        if (usuarioId == null) {
            throw new IllegalArgumentException("El ID del usuario no puede ser nulo");
        }

        // Crear el asiento y asignar los datos
        Asiento asiento = new Asiento();
        asiento.setFecha(asientoDTO.getFecha());
        asiento.setDescripcion(asientoDTO.getDescripcion());

        // Buscar el usuario por ID
        Usuario usuario = usuarioRepositorio.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Asignar el usuario al asiento
        asiento.setId_usuario(usuario.getId()); // Asumiendo que hay un método setUsuario en Asiento

        // Guardar el asiento en la base de datos
        return asientoRepositorio.save(asiento);
    }

    public List<Asiento> listarAsientos(Date fechaInicio, Date fechaFin) {  
    List<Asiento> asientos = asientoRepositorio.findAllBetweenDates(fechaInicio, fechaFin);
    for (Asiento asiento : asientos) {
        System.out.println("Asiento ID: " + asiento.getId());
        for (CuentaAsiento cuentaAsiento : asiento.getCuentasAsientos()) {
            System.out.println("Movimiento: " + cuentaAsiento);
        }
    }
    return asientos;
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
        // Verificamos si la cuenta existe antes de eliminar
        if (!asientoRepositorio.existsById(id)) {
            throw new RuntimeException("Asiento no encontrado con ID: " + id);
        }
        asientoRepositorio.deleteById(id);
    }
}
