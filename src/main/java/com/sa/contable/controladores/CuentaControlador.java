package com.sa.contable.controladores;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sa.contable.configuracion.JwtUtil;
import com.sa.contable.dto.CuentaDTO;
import com.sa.contable.dto.SaldoDTO;
import com.sa.contable.entidades.Cuenta;
import com.sa.contable.repositorios.CuentaAsientoRepositorio;
import com.sa.contable.servicios.CuentaServicio;

import jakarta.servlet.http.HttpServletRequest;
@RestController
@RequestMapping("/api/cuentas")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true") // Permitir cookies
public class CuentaControlador {

    private static final Logger logger = LoggerFactory.getLogger(CuentaControlador.class);

    @Autowired
    private CuentaAsientoRepositorio cuentaAsientoRepositorio;

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

    @GetMapping("/arbol")
public ResponseEntity<List<Map<String, Object>>> listarCuentasEnArbol() {
    // Obtener todas las cuentas principales (sin cuenta padre)
    List<Cuenta> cuentasPrincipales = cuentaServicio.obtenerCuentasSinPadre();

    // Crear una lista para representar el árbol de cuentas
    List<Map<String, Object>> arbol = new ArrayList<>();

    // Llamar al método recursivo para construir el árbol
    for (Cuenta cuenta : cuentasPrincipales) {
        arbol.add(convertirCuentaEnMapa(cuenta)); // Convertir cada cuenta en un mapa
    }

    return ResponseEntity.ok(arbol);
}

// Método recursivo para convertir una cuenta en un mapa anidado
private Map<String, Object> convertirCuentaEnMapa(Cuenta cuenta) {
    Map<String, Object> cuentaMap = new HashMap<>();
    cuentaMap.put("nombre", cuenta.getNombre());
    cuentaMap.put("codigo", cuenta.getCodigo());

    // Convertir las cuentas hijas si existen
    if (cuenta.getHijas() != null && !cuenta.getHijas().isEmpty()) {
        List<Map<String, Object>> subCuentas = new ArrayList<>();
        for (Cuenta hija : cuenta.getHijas()) {
            subCuentas.add(convertirCuentaEnMapa(hija)); // Recursivamente convertir las hijas
        }
        cuentaMap.put("subCuentas", subCuentas);
    } else {
        cuentaMap.put("subCuentas", new ArrayList<>()); // subCuentas vacías si no hay hijas
    }

    return cuentaMap;
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

        // Crear la nueva cuenta
        cuentaServicio.crearCuenta(cuenta);
        return ResponseEntity.ok("Cuenta creada con éxito");
    } catch (RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body("Error al crear la cuenta: " + e.getMessage());
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Error inesperado: " + e.getMessage());
    }
}

// Método para asignar automáticamente la cuenta padre basado en el código
private void asignarCuentaPadre(Cuenta cuenta) {
    long codigo = cuenta.getCodigo();
    Long codigoPadre = null;

    String codigoStr = String.valueOf(codigo); // Convertir el código a String

    // Verificar si el código es de una cuenta principal
    if (codigoStr.length() == 3 && codigoStr.endsWith("00")) {
        System.out.println("Código de la cuenta: " + codigo + " es una cuenta principal sin cuenta padre.");
        return; // No tiene padre, salir del método
    }

    // Determinar el código de la cuenta padre para los rangos 100, 200, 300, 400, 500
    if (codigoStr.length() == 3) {
        if (codigoStr.endsWith("00")) {
            codigoPadre = null; // Las cuentas principales (100, 200, 300, 400, 500) no tienen padre
        } else if (codigoStr.charAt(1) == '0') {
            // Códigos como 110, 120, 130, etc.
            codigoPadre = Long.parseLong(codigoStr.charAt(0) + "00"); // Ej. 110 -> 100, 220 -> 200, etc.
        } else {
            // Códigos como 111, 125, 211, 225, etc.
            codigoPadre = Long.parseLong(codigoStr.substring(0, 2) + "0"); // Ej. 111 -> 110, 125 -> 120, etc.
        }
    } else if (codigoStr.length() == 2) {
        // Código como 11 -> Padre: 100
        codigoPadre = Long.parseLong(codigoStr.charAt(0) + "00");
    }

    // Imprimir para verificar el código padre
    System.out.println("Código de la cuenta: " + codigo + ", Código padre asignado: " + codigoPadre);

    // Buscar la cuenta padre en la base de datos si el código es válido
    if (codigoPadre != null) {
        System.out.println("Buscando cuenta padre con código: " + codigoPadre);
        Optional<Cuenta> cuentaPadreOpt = cuentaServicio.obtenerCuentaPorCodigo(codigoPadre);
        if (cuentaPadreOpt.isPresent()) {
            Cuenta cuentaPadre = cuentaPadreOpt.get();

            // Asignar la cuenta padre a la nueva cuenta
            cuenta.setCuentaPadre(cuentaPadre);

            // Agregar la nueva cuenta a la lista de hijas de la cuenta padre
            cuentaPadre.agregarHija(cuenta);

            // Establecer recibeSaldo de la cuenta padre como false
            cuentaPadre.setRecibeSaldo(false);

            // Aquí no es necesario guardar la cuenta padre de nuevo
            System.out.println("Se ha asignado correctamente la cuenta hija " + codigo 
                    + " a la cuenta padre " + codigoPadre + ". El estado recibeSaldo de la cuenta padre es ahora false.");
        } else {
            throw new RuntimeException("La cuenta padre con código " + codigoPadre + " no existe.");
        }
    }
}















    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarCuenta(@PathVariable Long id) {
        // Verificar si el usuario tiene permisos de administrador
        ResponseEntity<String> permisoResponse = verificarPermisoAdministrador();
        if (permisoResponse != null) {
            return permisoResponse; // Si hay un error de permisos, retornar la respuesta
        }

        // Verificar si la cuenta ha sido utilizada en algún asiento
        if (haSidoUtilizadaEnAsiento(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No se puede eliminar la cuenta porque ha sido utilizada en un asiento.");
        }

        // Si no ha sido utilizada, proceder a eliminarla
        cuentaServicio.eliminarCuenta(id);
        return ResponseEntity.ok("Cuenta eliminada con éxito.");
    }

    public boolean haSidoUtilizadaEnAsiento(Long cuentaId) {
        // Utiliza el repositorio inyectado para verificar la existencia
        return cuentaAsientoRepositorio.existsByCuentaCodigo(cuentaId);
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
        if (cuenta.getHijas() != null) {
            List<CuentaDTO> hijasDTO = cuenta.getHijas().stream()
                .map(this::convertirACuentaDTO)
                .collect(Collectors.toList());
            dto.sethijas(hijasDTO);
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
    if (!cuenta.isPresent()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Retornar 404 si no se encuentra la cuenta
    }
    
    // Crear la respuesta con el saldo actual
    SaldoDTO saldoDTO = new SaldoDTO(cuenta.get().getCodigo(), cuenta.get().getSaldoActual());
    
    return ResponseEntity.ok(saldoDTO); // Retornar 200 OK con el saldo
}


    @PutMapping("/editarNombre/{codigo}")
public ResponseEntity<String> editarNombreCuenta(@PathVariable Long codigo, @RequestBody String nuevoNombre) {
    ResponseEntity<String> permisoResponse = verificarPermisoAdministrador();
    if (permisoResponse != null) {
        return permisoResponse; // Si hay un error de permisos, retornar la respuesta
    }
    try {
        cuentaServicio.editarNombreCuenta(codigo, nuevoNombre);
        return ResponseEntity.ok("Nombre de la cuenta actualizado correctamente");
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cuenta no encontrada");
    }
}





    @GetMapping("/existsByCuentaCodigo/{cuentaCodigo}")
    public boolean existsByCuentaCodigo(@PathVariable Long cuentaCodigo) {
        return cuentaAsientoRepositorio.existsByCuentaCodigo(cuentaCodigo);
    }

    
}
