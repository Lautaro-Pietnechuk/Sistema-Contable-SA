package com.sa.contable.controladores;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sa.contable.configuracion.JwtUtil;
import com.sa.contable.dto.CuentaDTO;
import com.sa.contable.dto.SaldoDTO;
import com.sa.contable.entidades.Cuenta;
import com.sa.contable.servicios.CuentaServicio;

import jakarta.servlet.http.HttpServletRequest;
@RestController
@RequestMapping("/api/cuentas")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true") // Permitir cookies
public class CuentaControlador {

    private static final Logger logger = LoggerFactory.getLogger(CuentaControlador.class);

    @Autowired
    private CuentaServicio cuentaServicio;

    @Autowired
    private JwtUtil jwtUtil; // Inyectar JwtUtil

    @Autowired
    private HttpServletRequest request;

    @GetMapping
    public List<CuentaDTO> listarCuentas() { 
        List<Cuenta> cuentas = cuentaServicio.obtenerTodasLasCuentas();
        return cuentas.stream().map(this::convertirACuentaDTO).collect(Collectors.toList());
    }

    @PostMapping("/crear")
public ResponseEntity<String> crearCuenta(@RequestBody Cuenta cuenta) {
    // Verificar permisos de administrador
    ResponseEntity<String> permisoResponse = verificarPermisoAdministrador();
    if (permisoResponse != null) {
        return permisoResponse; // Retornar si no tiene permisos
    }

    try {
        // Asignar la cuenta padre automáticamente basado en el código
        asignarCuentaPadre(cuenta);

        // Crear la cuenta
        cuentaServicio.crearCuenta(cuenta);
        return ResponseEntity.ok("Cuenta creada con éxito");
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Error al crear la cuenta: " + e.getMessage());
    }
}

// Método para asignar automáticamente la cuenta padre basado en el código
private void asignarCuentaPadre(Cuenta cuenta) {
    long codigo = cuenta.getCodigo();
    Long codigoPadre = null;

    String codigoStr = String.valueOf(codigo); // Convertir el código a String

    // Determinar el código de la cuenta padre basado en los últimos dígitos
    if (codigoStr.length() == 3) {
        // Para códigos de longitud 3
        if (codigoStr.charAt(2) == '0') {
            // Si el último dígito es 0, el código padre es el primer dígito seguido de 00
            codigoPadre = Long.parseLong(codigoStr.charAt(0) + "00"); // Ej: 110 -> 100
        } else {
            // Si el último dígito no es 0, cambiar el último dígito a 0
            codigoPadre = Long.parseLong(codigoStr.substring(0, 2) + "0"); // Ej: 111 -> 110
        }
    } else if (codigoStr.length() == 2) {
        // Si tiene longitud 2, establecer el padre al primer dígito seguido de 00
        codigoPadre = Long.parseLong(codigoStr.charAt(0) + "00"); // Ej: 110 -> 100
    }

    // Imprimir el código padre para verificación
    System.out.println("Código de la cuenta: " + codigo + ", Código padre asignado: " + codigoPadre);

    // Si se determinó un código padre, buscarlo en la base de datos
    if (codigoPadre != null) {
        Optional<Cuenta> cuentaPadreOpt = cuentaServicio.obtenerCuentaPorCodigo(codigoPadre);
        if (cuentaPadreOpt.isPresent()) {
            cuenta.setCuentaPadre(cuentaPadreOpt.get()); // Asignar la cuenta padre
        } else {
            throw new RuntimeException("La cuenta padre con código " + codigoPadre + " no existe.");
        }
    }
}






    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarCuenta(@PathVariable Long id) {
        ResponseEntity<String> permisoResponse = verificarPermisoAdministrador();
        if (permisoResponse != null) {
            return permisoResponse; // Si hay un error de permisos, retornar la respuesta
        }
        
        cuentaServicio.eliminarCuenta(id);
        return ResponseEntity.ok("Cuenta eliminada con éxito.");
    }

    private ResponseEntity<String> verificarPermisoAdministrador() {
        // Obtener el token del encabezado de autorización
        String token = obtenerTokenDelEncabezado(request);
        if (token == null || !jwtUtil.esTokenValido(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No estás autenticado. Por favor inicia sesión.");
        }

        // Obtener el rol del token
        String rol = jwtUtil.obtenerRolDelToken(token);
        if (!"ROLE_ADMINISTRADOR".equals(rol)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permiso para realizar esta acción. Se requiere el rol 'ADMINISTRADOR'.");
        }

        return null; // Permiso verificado, retornar null
    }

    private String obtenerTokenDelEncabezado(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // Eliminar "Bearer " para obtener el token
        }
        return null; // Si no se encuentra el token
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
    @GetMapping("/recibeSaldo")
    public ResponseEntity<List<CuentaDTO>> listarCuentasRecibeSaldo() {
        logger.info("Solicitud recibida para listar cuentas que reciben saldo.");
        try {
            List<Cuenta> cuentas = cuentaServicio.obtenerCuentasRecibeSaldo(); // Llama al servicio para obtener cuentas
    
            if (cuentas == null || cuentas.isEmpty()) {
                logger.warn("No se encontraron cuentas que reciben saldo.");
                return ResponseEntity.noContent().build();  // 204 No Content
            }
    
            // Convertir cuentas a DTO antes de devolver
            List<CuentaDTO> cuentaDTOs = cuentas.stream()
                .map(this::convertirACuentaDTO) // Convierte a DTO
                .collect(Collectors.toList());
    
            logger.info("Devolviendo {} cuentas que reciben saldo.", cuentaDTOs.size());
            return ResponseEntity.ok(cuentaDTOs);  // 200 OK
        } catch (Exception e) {
            logger.error("Error al listar cuentas: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();  // 500 Internal Server Error
        }
    }


    @GetMapping("/{codigo}/saldo")
    public ResponseEntity<SaldoDTO> obtenerSaldo(@PathVariable Long codigo) {
        // Buscar la cuenta por su código
        Optional<Cuenta> cuenta = cuentaServicio.obtenerCuentaPorCodigo(codigo);
        
        // Verificar si la cuenta existe
        if (cuenta == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Retornar 404 si no se encuentra la cuenta
        }
        
        // Crear la respuesta con el saldo actual
        SaldoDTO saldoDTO = new SaldoDTO(cuenta.get().getCodigo(), cuenta.get().getSaldoActual());
        saldoDTO.setSaldo(cuenta.get().getSaldoActual());
        
        return ResponseEntity.ok(saldoDTO); // Retornar 200 OK con el saldo
    }

    
}
