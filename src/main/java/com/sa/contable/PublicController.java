package com.sa.contable;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    @GetMapping("/test")
    public ResponseEntity<String> testPublicEndpoint() {
        return ResponseEntity.ok("Este es un endpoint público!");
    }
}