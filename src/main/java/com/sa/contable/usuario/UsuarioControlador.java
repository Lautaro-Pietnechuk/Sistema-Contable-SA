package com.sa.contable.Usuario;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class UsuarioControlador {

    @Autowired
    private UsuarioServicio usuarioServicio;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Usuario usuario) {
        try {
            Optional<Usuario> loggedInUser = usuarioServicio.iniciarSesion(usuario.getNombreUsuario(), usuario.getContraseña());
            if (loggedInUser.isPresent()) {
                return ResponseEntity.ok("Login exitoso");
            } else {
                return ResponseEntity.status(401).body("Usuario o contraseña incorrectos");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error en el servidor: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Usuario usuario) {
        try {
            if (usuarioServicio.esNombreUsuarioTomado(usuario.getNombreUsuario())) {
                return ResponseEntity.status(400).body("El usuario ya existe");
            }

            usuarioServicio.registrar(usuario);
            return ResponseEntity.ok("Usuario registrado exitosamente");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error en el servidor: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/usuarios/{usuarioId}/roles/{rolId}")
    public ResponseEntity<?> agregarRol(@PathVariable Long usuarioId, @PathVariable Long rolId) {
        try {
            usuarioServicio.agregarRolAUsuario(usuarioId, rolId);
            return ResponseEntity.ok("Rol agregado exitosamente");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error en el servidor: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        usuarioServicio.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}