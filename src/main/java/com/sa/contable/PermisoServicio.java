package com.sa.contable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PermisoServicio {

    @Autowired
    private PermisoRepositorio permisoRepositorio;

    public Permiso crearPermiso(Permiso permiso) {
        return permisoRepositorio.save(permiso);
    }

    public List<Permiso> obtenerTodosLosPermisos() {
        return permisoRepositorio.findAll();
    }
}