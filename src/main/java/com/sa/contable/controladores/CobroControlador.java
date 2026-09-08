
package com.sa.contable.controladores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sa.contable.DTO.AnularCobroRequest;
import com.sa.contable.DTO.CobroRequest;
import com.sa.contable.DTO.CobroResponse;
import com.sa.contable.servicios.CobroServicio;

@RestController
@RequestMapping("/api/cobros")
@CrossOrigin(origins = "*") // Ajustalo según tu config de CORS global
public class CobroControlador {

    @Autowired
    private CobroServicio cobroServicio;

    @PostMapping
    public ResponseEntity<?> registrarCobro(@RequestBody CobroRequest request) {
        try {
            // Validaciones básicas de entrada
            if (request.getClienteId() == null) {
                return ResponseEntity.badRequest().body("El ID del cliente es obligatorio.");
            }
            if (request.getMonto() == null || request.getMonto() <= 0) {
                return ResponseEntity.badRequest().body("El monto del cobro debe ser mayor a cero.");
            }
            
            System.out.println("\n==================================================");
            System.out.println("💰 [RECEPCIÓN COBRO] - Procesando pago para Cliente ID: " + request.getClienteId());
            System.out.println("   💵 Monto recibido: $" + request.getMonto());
            System.out.println("   💳 Método: " + request.getMetodoPago());
            System.out.println("==================================================\n");

            // Ejecutamos la lógica de imputación en el servicio
            cobroServicio.registrarCobro(request.getClienteId(), request.getMonto(), request.getMetodoPago(), 1L);
            
            return ResponseEntity.ok().body("{\"message\": \"Cobro procesado e imputado con éxito.\"}");
            
        } catch (RuntimeException e) {
            System.out.println("❌ [ERROR COBRO] - No se pudo procesar: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{idcliente}")
    public ResponseEntity<?> obtenerHistorialCobros(@PathVariable Long idcliente, @RequestParam(required = false) String desde, @RequestParam(required = false) String hasta)  {
        try {
            if (idcliente == null) {
                return ResponseEntity.badRequest().body("El ID del cliente es obligatorio.");
            }
            var cobros = cobroServicio.obtenerCobrosPorCliente(idcliente, desde, hasta);
            var cobrosResponse = cobros.stream()
                .map(c -> new CobroResponse(c.getId(), c.getFecha(), c.getMonto(), c.getMetodoPago(), c.getAnulado()))
                .toList();
            return ResponseEntity.ok(cobrosResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/anular/{cobroId}")
    public ResponseEntity<?> anularCobro(@PathVariable Long cobroId, @RequestBody AnularCobroRequest request) {
        try {
            if (cobroId == null) {
                return ResponseEntity.badRequest().body("El ID del cobro es obligatorio.");
            }
            if (request.getMotivo() == null || request.getMotivo().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El motivo de anulación es obligatorio.");
            }
            cobroServicio.anularCobro(cobroId, request.getMotivo(), 1L);
            return ResponseEntity.ok().body("{\"message\": \"Cobro anulado con éxito.\"}");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}