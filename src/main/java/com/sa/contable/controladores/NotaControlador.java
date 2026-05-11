package com.sa.contable.controladores;



import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sa.contable.configuracion.JwtUtil;
import com.sa.contable.entidades.Nota;
import com.sa.contable.servicios.NotaServicio;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/notas")
@CrossOrigin(origins = "*") // Ajustá esto según tu config de seguridad
public class NotaControlador {

    @Autowired
    private NotaServicio notaServicio;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private JwtUtil jwtUtil;

    

    @GetMapping
    public ResponseEntity<List<Nota>> obtenerTodas() {
        return ResponseEntity.ok(notaServicio.obtenerTodas());
    }

    private Long obtenerUsuarioIdDesdeToken() {
    String authHeader = request.getHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        throw new RuntimeException("Token no enviado o formato inválido");
    }

    String token = authHeader.substring(7);
    String id = jwtUtil.obtenerIdDelToken(token);
    return Long.parseLong(id);
    }

    @GetMapping("/venta/{idVenta}")
    public ResponseEntity<List<Nota>> obtenerPorVenta(@PathVariable Long idVenta) {
        List<Nota> notas = notaServicio.obtenerPorVenta(idVenta);
        return ResponseEntity.ok(notas);
    }

    @PostMapping
    public ResponseEntity<Nota> crearNota(@RequestBody Nota nota) {
        
        Long usuarioId = obtenerUsuarioIdDesdeToken();
        Nota nuevaNota = notaServicio.crearNota(nota, usuarioId);
        return new ResponseEntity<>(nuevaNota, HttpStatus.CREATED);
    }
}