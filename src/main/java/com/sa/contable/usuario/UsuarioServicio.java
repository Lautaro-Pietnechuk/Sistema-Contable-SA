package com.sa.contable.usuario;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sa.contable.Rol;
import com.sa.contable.RolRepositorio;

@Service
public class UsuarioServicio {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    
    @Autowired
    private RolRepositorio rolRepositorio;
    

    public Usuario registrar(Usuario usuario) {
        return usuarioRepositorio.save(usuario);
    }

    public void agregarRolAUsuario(Long usuarioId, Long rolId) {
        Usuario usuario = usuarioRepositorio.findById(usuarioId).orElseThrow();
        Rol rol = rolRepositorio.findById(rolId).orElseThrow();
        usuario.getRoles().add(rol);
        usuarioRepositorio.save(usuario);
    }
    

    public Optional<Usuario> iniciarSesion(String nombreUsuario, String contraseña) {
        return usuarioRepositorio.findByNombreUsuarioAndContraseña(nombreUsuario, contraseña);
    }

    public boolean esNombreUsuarioTomado(String nombreUsuario) {
        return usuarioRepositorio.findByNombreUsuario(nombreUsuario).isPresent();
    }
}