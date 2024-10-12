package com.sa.contable.usuario;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sa.contable.rol.Rol;
import com.sa.contable.rol.RolRepositorio;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class UsuarioServicio {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private RolRepositorio rolRepositorio;

    private final String SECRET_KEY = "mi_clave_secreta"; // Cambia esto por una clave más segura

    public Usuario registrar(Usuario usuario) {
        // Aquí no se encripta la contraseña
        // Se puede guardar directamente la contraseña proporcionada
        Rol usuarioRol = rolRepositorio.findByNombre("Usuario")
            .orElseThrow(() -> new RuntimeException("Error: El rol 'Usuario' no se encuentra."));
        usuario.setRol(usuarioRol);
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

    @SuppressWarnings("deprecation")
    public String generarToken(Usuario usuario) {
        return Jwts.builder()
                .setSubject(usuario.getNombreUsuario())
                .signWith(SignatureAlgorithm.HS512, Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                .compact();
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
