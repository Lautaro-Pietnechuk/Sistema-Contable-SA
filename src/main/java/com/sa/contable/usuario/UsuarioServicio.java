package com.sa.contable.Usuario;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sa.contable.Rol.Rol;
import com.sa.contable.Rol.RolRepositorio;

@Service
public class UsuarioServicio {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    
    @Autowired
    private RolRepositorio rolRepositorio;
    

    /* public Usuario registrar(Usuario usuario) {
        return usuarioRepositorio.save(usuario);
    } */

    public Usuario registrar(Usuario usuario) {
        Rol UsuarioRol = rolRepositorio.findByName("Usuario")
            .orElseThrow(() -> new RuntimeException("Error: El rol 'Usuario' no se encuentra."));
        usuario.setRol(UsuarioRol);
        return usuarioRepositorio.save(usuario);
    }

    public void agregarRolAUsuario(Long usuarioId, Long rolId) {
        Usuario usuario = usuarioRepositorio.findById(usuarioId).orElseThrow();
        Rol rol = rolRepositorio.findById(rolId).orElseThrow();
        usuario.setRol(rol);
        usuarioRepositorio.save(usuario);
    }
    

    public Optional<Usuario> iniciarSesion(String nombreUsuario, String contraseña) {
        return usuarioRepositorio.findByNombreUsuarioAndContraseña(nombreUsuario, contraseña);
    }

    public boolean esNombreUsuarioTomado(String nombreUsuario) {
        return usuarioRepositorio.findByNombreUsuario(nombreUsuario).isPresent();
    }

    public void eliminarUsuario(Long id) {
        usuarioRepositorio.deleteById(id);
    }
}