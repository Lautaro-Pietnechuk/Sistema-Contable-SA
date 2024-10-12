package com.sa.contable.usuario;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sa.contable.rol.Rol;
import com.sa.contable.rol.RolRepositorio;

@Service
public class UsuarioServicio {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private RolRepositorio rolRepositorio;

    public Usuario registrar(Usuario usuario) {
        Rol UsuarioRol = rolRepositorio.findByNombre("Usuario")
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
        // Busca el usuario por nombre de usuario
        Optional<Usuario> usuarioOpt = usuarioRepositorio.findByNombreUsuario(nombreUsuario);
        
        // Verifica si el usuario existe y si la contraseña coincide
        if (usuarioOpt.isPresent() && usuarioOpt.get().getContraseña().equals(contraseña)) {
            return usuarioOpt; // Retorna el usuario si el inicio de sesión es exitoso
        }
        return Optional.empty(); // Retorna vacío si no coincide
    }

    public boolean esNombreUsuarioTomado(String nombreUsuario) {
        return usuarioRepositorio.findByNombreUsuario(nombreUsuario).isPresent();
    }

    public void eliminarUsuario(Long id) {
        usuarioRepositorio.deleteById(id);
    }

    public boolean existeUsuario(Long usuarioId) {
        return usuarioRepositorio.existsById(usuarioId);
    }
}
