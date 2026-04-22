package com.sa.contable.controladores;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sa.contable.configuracion.JwtUtil;
import com.sa.contable.dto.VentaDTO;
import com.sa.contable.servicios.VentaServicio;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/ventas")
@CrossOrigin(origins = "http://localhost:3000")
public class VentaControlador {

    @Autowired
    private VentaServicio ventaServicio;
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private HttpServletRequest request;

    private Long obtenerUsuarioIdDesdeToken() {
    String authHeader = request.getHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        throw new RuntimeException("Token no enviado o formato inválido");
    }

    String token = authHeader.substring(7);
    String id = jwtUtil.obtenerIdDelToken(token);
    return Long.parseLong(id);
    }

    @GetMapping
    public ResponseEntity<List<VentaDTO>> obtenerTodas() {
        List<VentaDTO> ventas = ventaServicio.obtenerTodas();
        
        return ResponseEntity.ok(ventas);
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try {
            VentaDTO venta = ventaServicio.obtenerPorId(id);
            return ResponseEntity.ok(venta);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/comprobante/{numeroComprobante}")
    public ResponseEntity<?> obtenerPorComprobante(@PathVariable String numeroComprobante) {
        try {
            VentaDTO venta = ventaServicio.obtenerPorComprobante(numeroComprobante);
            return ResponseEntity.ok(venta);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<VentaDTO>> obtenerPorCliente(@PathVariable Long clienteId) {
        List<VentaDTO> ventas = ventaServicio.obtenerPorCliente(clienteId);
        return ResponseEntity.ok(ventas);
    }

    @GetMapping("/rango-fechas")
    public ResponseEntity<List<VentaDTO>> obtenerPorRangoFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        List<VentaDTO> ventas = ventaServicio.obtenerPorRangoFechas(desde, hasta);
        return ResponseEntity.ok(ventas);
    }

    @PostMapping
    public ResponseEntity<?> crearVenta(@RequestBody VentaDTO ventaDTO) {
        try {
            Long usuarioId = obtenerUsuarioIdDesdeToken();
            VentaDTO ventaCreada = ventaServicio.crearVenta(ventaDTO, usuarioId);
            return ResponseEntity.status(HttpStatus.CREATED).body(ventaCreada);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    

}