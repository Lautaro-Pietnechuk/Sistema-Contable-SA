package com.sa.contable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000") // Ajusta esto según la URL de tu frontend
public class UsuarioControlador {

    @Autowired
    private UsuarioServicio usuarioServicio;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Usuario usuario) {
        Optional<Usuario> loggedInUser = usuarioServicio.iniciarSesion(usuario.getNombreUsuario(), usuario.getContraseña());
        if (loggedInUser.isPresent()) {
            return ResponseEntity.ok("Login exitoso");
        } else {
            return ResponseEntity.status(401).body("Usuario o contraseña incorrectos");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Usuario usuario) {
        if (usuarioServicio.esNombreUsuarioTomado(usuario.getNombreUsuario())) {
            return ResponseEntity.status(400).body("El usuario ya existe");
        }

        usuarioServicio.registrar(usuario);
        return ResponseEntity.ok("Usuario registrado exitosamente");
    }

    @PostMapping("/usuarios/{usuarioId}/roles/{rolId}")
    public ResponseEntity<?> agregarRol(@PathVariable Long usuarioId, @PathVariable Long rolId) {
        usuarioServicio.agregarRolAUsuario(usuarioId, rolId);
        return ResponseEntity.ok("Rol asignado exitosamente");
    }
}
