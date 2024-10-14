package com.sa.contable.cuenta;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sa.contable.DTO.CuentaDTO;

@RestController
@RequestMapping("/api/cuentas")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true") // Permitir cookies
public class CuentaControlador {

    @Autowired
    private CuentaServicio cuentaServicio;

    @PreAuthorize("hasRole('ADMINISTRADOR')") 
    @GetMapping
    public List<CuentaDTO> listarCuentas() {
        List<Cuenta> cuentas = cuentaServicio.obtenerTodasLasCuentas();
        return cuentas.stream().map(this::convertirACuentaDTO).collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping("/crear")
    public ResponseEntity<String> crearCuenta(@RequestBody Cuenta cuenta) {
        try {
            // Verificar que la cuenta padre exista
            if (cuenta.getCuentaPadre() != null) {
                Optional<Cuenta> cuentaPadreOpt = cuentaServicio.obtenerCuentaPorCodigo(cuenta.getCuentaPadre().getCodigo());
                if (!cuentaPadreOpt.isPresent()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Error: La cuenta padre no existe.");
                }

                // Si existe, se asigna a la cuenta
                cuenta.setCuentaPadre(cuentaPadreOpt.get());
            }

            // Crear la cuenta
            cuentaServicio.crearCuenta(cuenta);
            return ResponseEntity.ok("Cuenta creada con éxito");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al crear la cuenta: " + e.getMessage());
        }
    }



    @PreAuthorize("hasRole('ADMINISTRADOR)")
    @DeleteMapping("/{id}")
    public void eliminarCuenta(@PathVariable Long id) {
        cuentaServicio.eliminarCuenta(id);
    }

    private CuentaDTO convertirACuentaDTO(Cuenta cuenta) {
        CuentaDTO dto = new CuentaDTO();
        dto.setId(cuenta.getCodigo());
        dto.setNombre(cuenta.getNombre());
        dto.setCodigo(cuenta.getCodigo());
        dto.setTipo(cuenta.getTipo());
        dto.setRecibeSaldo(cuenta.getRecibeSaldo());
        dto.setSaldoActual(cuenta.getSaldoActual());
        
        // Convertir las subcuentas a DTO
        if (cuenta.getSubCuentas() != null) {
            List<CuentaDTO> subCuentasDTO = cuenta.getSubCuentas().stream()
                .map(this::convertirACuentaDTO)
                .collect(Collectors.toList());
            dto.setSubCuentas(subCuentasDTO);
        }
        
        return dto;
    }
}
