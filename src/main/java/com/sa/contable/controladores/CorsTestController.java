package com.sa.contable.controladores;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CorsTestController {

    @GetMapping("/api/cors-test")
    public ResponseEntity<String> corsTest() {
        return ResponseEntity.ok("CORS está funcionando");
    }
}
