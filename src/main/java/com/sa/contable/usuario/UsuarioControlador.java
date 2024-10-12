package com.sa.contable.usuario;

import java.util.Map;
import java.util.Optional;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.sa.contable.Configuracion.JwtUtil;
import com.sa.contable.rol.RolServicio;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true") // Permitir cookies
public class UsuarioControlador {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioControlador.class);

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Autowired
    private RolServicio rolServicio;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
public ResponseEntity<?> login(@RequestBody Usuario usuario, HttpServletResponse response) {
    logger.info("Intento de inicio de sesión para el usuario: {}", usuario.getNombreUsuario());
    try {
        Optional<Usuario> loggedInUser = usuarioServicio.iniciarSesion(usuario.getNombreUsuario(), usuario.getContraseña());
        if (loggedInUser.isPresent()) {
            
            String token = jwtUtil.generarToken(loggedInUser.get().getNombreUsuario(), loggedInUser.get().getRol().getNombre());

            // Crear la cookie con el token
            Cookie cookie = new Cookie("token", token);
            cookie.setHttpOnly(true); // No accesible desde JavaScript
            cookie.setSecure(true); // Requiere HTTPS en producción
            cookie.setPath("/"); // Disponible para todas las rutas del backend
            cookie.setMaxAge(24 * 60 * 60); // 1 día de duración
            response.addCookie(cookie); // Agregar la cookie a la respuesta

            logger.info("Login exitoso para el usuario: {}", usuario.getNombreUsuario());
            return ResponseEntity.ok(Map.of("message", "Login exitoso", "token", token)); // Devolver el token en la respuesta si es necesario
        } else {
            logger.warn("Usuario o contraseña incorrectos para: {}", usuario.getNombreUsuario());
            return ResponseEntity.status(401).body(Map.of("error", "Usuario o contraseña incorrectos"));
        }
    } catch (Exception e) {
        logger.error("Error en el inicio de sesión para el usuario: {}", usuario.getNombreUsuario(), e);
        return ResponseEntity.status(500).body(Map.of("error", "Error en el servidor: " + e.getMessage()));
    }
}


    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Usuario usuario) {
        logger.info("Intento de registro para el usuario: {}", usuario.getNombreUsuario());
        try {
            if (usuarioServicio.esNombreUsuarioTomado(usuario.getNombreUsuario())) {
                logger.warn("El nombre de usuario ya existe: {}", usuario.getNombreUsuario());
                return ResponseEntity.status(400).body("El usuario ya existe");
            }

            usuarioServicio.registrar(usuario);
            logger.info("Usuario registrado exitosamente: {}", usuario.getNombreUsuario());
            return ResponseEntity.ok("Usuario registrado exitosamente");
        } catch (Exception e) {
            logger.error("Error en el registro del usuario: {}", usuario.getNombreUsuario(), e);
            return ResponseEntity.status(500).body("Error en el servidor: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('Administrador')") 
    @PostMapping("/{usuarioId}/roles/{rolId}")
    public ResponseEntity<?> agregarRol(@PathVariable Long usuarioId, @PathVariable Long rolId) {
        logger.info("Intento de agregar rol con ID {} al usuario con ID {}", rolId, usuarioId);
        try {
            if (!usuarioServicio.existeUsuario(usuarioId)) {
                logger.warn("Usuario no encontrado con ID: {}", usuarioId);
                return ResponseEntity.status(404).body("Usuario no encontrado");
            }

            if (!rolServicio.existeRol(rolId)) {
                logger.warn("Rol no encontrado con ID: {}", rolId);
                return ResponseEntity.status(404).body("Rol no encontrado");
            }

            usuarioServicio.agregarRolAUsuario(usuarioId, rolId);
            logger.info("Rol agregado exitosamente: Rol ID {} para Usuario ID {}", rolId, usuarioId);
            return ResponseEntity.ok("Rol agregado exitosamente");

        } catch (Exception e) {
            logger.error("Error al agregar rol al usuario", e);
            return ResponseEntity.status(500).body("Error en el servidor: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('Administrador')")
    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        logger.info("Intento de eliminar usuario con ID: {}", id);
        try {
            usuarioServicio.eliminarUsuario(id);
            logger.info("Usuario eliminado con ID: {}", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error al eliminar el usuario con ID: {}", id, e);
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // Eliminar la cookie
        response.addCookie(cookie);

        logger.info("Logout exitoso");
        return ResponseEntity.ok(Map.of("message", "Logout exitoso"));
    }
}
