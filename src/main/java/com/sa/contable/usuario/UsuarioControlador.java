package com.sa.contable.usuario;

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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.sa.contable.rol.RolServicio;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class UsuarioControlador {

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Autowired
    private RolServicio rolServicio;

    @RequestMapping(value = "/register", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptions() {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Usuario usuario) {
        try {
            Optional<Usuario> loggedInUser = usuarioServicio.iniciarSesion(usuario.getNombreUsuario(), usuario.getContraseña());
            if (loggedInUser.isPresent()) {
                // Respuesta JSON en caso de éxito
                return ResponseEntity.ok().body("Login exitoso");
            } else {
                // Respuesta JSON en caso de usuario o contraseña incorrectos
                return ResponseEntity.status(401).body("Usuario o contraseña incorrectos");
            }
        } catch (Exception e) {
            // Manejo de errores del servidor
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

/*     @PreAuthorize("hasRole('Administrador')") */
    @PostMapping("/usuarios/{usuarioId}/roles/{rolId}")
public ResponseEntity<?> agregarRol(@PathVariable Long usuarioId, @PathVariable Long rolId) {
    try {
        // Verificar si el usuario existe
        if (!usuarioServicio.existeUsuario(usuarioId)) {
            return ResponseEntity.status(404).body("Usuario no encontrado");
        }

        // Verificar si el rol existe
        if (!rolServicio.existeRol(rolId)) {
            return ResponseEntity.status(404).body("Rol no encontrado");
        }

        // Asignar el rol al usuario
        usuarioServicio.agregarRolAUsuario(usuarioId, rolId);
        return ResponseEntity.ok("Rol agregado exitosamente");

    } catch (Exception e) {
        return ResponseEntity.status(500).body("Error en el servidor: " + e.getMessage());
    }
}


    @PreAuthorize("hasRole('Administrador')")
    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        usuarioServicio.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}
