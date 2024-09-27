package com.sa.contable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RolServicio {

    @Autowired
    private RolRepositorio rolRepositorio;

    public Rol crearRol(Rol rol) {
        return rolRepositorio.save(rol);
    }

    public List<Rol> obtenerTodosLosRoles() {
        return rolRepositorio.findAll();
    }
}
