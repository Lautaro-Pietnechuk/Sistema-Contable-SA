package com.sa.contable.servicios;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sa.contable.entidades.Rol;
import com.sa.contable.repositorios.RolRepositorio;

@Service
public class RolServicio {

    @Autowired
    private RolRepositorio rolRepositorio;

    public List<Rol> listarRoles() {
        return rolRepositorio.findAll();
    }

    public Rol obtenerRolPorId(Long id) {
        return rolRepositorio.findById(id).orElse(null);
    }

    public Rol guardarRol(Rol rol) {
        return rolRepositorio.save(rol);
    }

    public void eliminarRol(Long id) {
        rolRepositorio.deleteById(id);
    }

    public boolean existeRol(Long rolId) {
        return rolRepositorio.existsById(rolId);
    }
}
