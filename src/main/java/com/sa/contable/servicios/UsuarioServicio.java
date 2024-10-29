package com.sa.contable.servicios;

import java.util.List;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sa.contable.entidades.Rol;
import com.sa.contable.entidades.Usuario;
import com.sa.contable.repositorios.RolRepositorio;
import com.sa.contable.repositorios.UsuarioRepositorio;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;


@Service
public class UsuarioServicio {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private RolRepositorio rolRepositorio;

    @Value("${jwt.secret}")
    private String secretKey;
    

    public Usuario registrar(Usuario usuario) {
        Rol usuarioRol = rolRepositorio.findByNombre("ROLE_USUARIO")
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
        Optional<Usuario> usuarioOpt = usuarioRepositorio.findByNombreUsuario(nombreUsuario);
        if (usuarioOpt.isPresent() && usuarioOpt.get().getContraseña().equals(contraseña)) {
            return usuarioOpt;
        }
        return Optional.empty();
    }

    public String generarToken(Usuario usuario) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes()); // Convertir a SecretKey
        return Jwts.builder()
                .setSubject(usuario.getNombreUsuario())
                .claim("rol", usuario.getRol().getNombre()) // Añadir rol al token
                .claim("id", usuario.getId()) // Añadir ID del usuario al token
                .signWith(key, SignatureAlgorithm.HS512) // Usar la clave secreta convertida
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

    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepositorio.findAll();
    }

    public Optional<Usuario> obtenerUsuarioPorId(Long id) {
        return usuarioRepositorio.findById(id);
    }

    public String obtenerNombreUsuarioPorId(Long id) {
        return usuarioRepositorio.findById(id).map(Usuario::getNombreUsuario).orElse(null);
    }
}
