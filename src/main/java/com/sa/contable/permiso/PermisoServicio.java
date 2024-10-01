package com.sa.contable.permiso;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PermisoServicio {

    @Autowired
    private PermisoRepositorio permisoRepositorio;

    public List<Permiso> obtenerPermisos() {
        return permisoRepositorio.findAll();
    }

    public Optional<Permiso> obtenerPermisoPorId(Long id) {
        return permisoRepositorio.findById(id);
    }

    public Permiso crearPermiso(Permiso permiso) {
        return permisoRepositorio.save(permiso);
    }

    public void eliminarPermiso(Long id) {
        permisoRepositorio.deleteById(id);
    }

    public List<Permiso> listarPermisos() {
        return permisoRepositorio.findAll();
    }

    public Permiso guardarPermiso(Permiso permiso) {
        return permisoRepositorio.save(permiso);
    }
}