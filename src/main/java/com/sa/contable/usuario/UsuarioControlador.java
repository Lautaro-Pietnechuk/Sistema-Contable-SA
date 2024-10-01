package com.sa.contable.usuario;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

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

    @PostMapping("/usuarios/{usuarioId}/roles/{rolId}")
    public ResponseEntity<?> agregarRol(@PathVariable Long usuarioId, @PathVariable Long rolId) {
        try {
            usuarioServicio.agregarRolAUsuario(usuarioId, rolId);
            return ResponseEntity.ok("Rol agregado exitosamente");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error en el servidor: " + e.getMessage());
        }
    }
    
}