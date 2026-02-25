package com.sa.contable;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 1. Atrapa tus validaciones manuales (ej. throw new IllegalArgumentException("El mail ya está registrado"))
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(IllegalArgumentException e) {
        // Devuelve 400 (Bad Request) porque el usuario mandó un dato incorrecto/repetido
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("mensaje", e.getMessage()));
    }

    // 2. Atrapa cualquier otro error inesperado (ej. se cayó la base de datos)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        // Devuelve 500 (Internal Server Error) y no expone detalles técnicos sensibles al frontend
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("mensaje", "Ocurrió un error interno en el servidor."));
    }
}
