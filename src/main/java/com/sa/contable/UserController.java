package com.sa.contable;

import com.sa.contable.Usuario;
import com.sa.contable.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000") // Ajusta esto según la URL de tu frontend
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Usuario user) {
        Optional<Usuario> loggedInUser = userService.login(user.getUsername(), user.getPassword());
        if (loggedInUser.isPresent()) {
            return ResponseEntity.ok("Login exitoso");
        } else {
            return ResponseEntity.status(401).body("Usuario o contraseña incorrectos");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Usuario user) {
        if (userService.isUsernameTaken(user.getUsername())) {
            return ResponseEntity.status(400).body("El usuario ya existe");
        }

        userService.register(user);
        return ResponseEntity.ok("Usuario registrado exitosamente");
    }
}
