package com.sa.contable.controladores;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.sa.contable.dto.AsientoDTO;
import com.sa.contable.dto.CuentaAsientoDTO;
import com.sa.contable.entidades.Asiento;
import com.sa.contable.entidades.Cuenta;
import com.sa.contable.entidades.CuentaAsiento;
import com.sa.contable.servicios.AsientoServicio;
import com.sa.contable.servicios.CuentaAsientoServicio;
import com.sa.contable.servicios.CuentaServicio;

import jakarta.servlet.http.HttpServletRequest;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/asientos")
public class AsientoControlador {

    @Autowired
    private AsientoServicio asientoServicio;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private CuentaServicio cuentaServicio;

    @Autowired
    private CuentaAsientoServicio cuentaAsientoServicio;

    // Endpoint para crear un nuevo asiento contable
    @PostMapping("/crear/{idUsuario}")
public ResponseEntity<?> crearAsiento(@PathVariable Long idUsuario, @RequestBody AsientoDTO asientoDTO) {
    if (idUsuario == null) {
        return ResponseEntity.badRequest().body("El ID del usuario no puede ser nulo.");
    }

    if (asientoDTO.getMovimientos() == null || asientoDTO.getMovimientos().size() < 2) {
        return ResponseEntity.badRequest().body("El asiento debe contener al menos dos movimientos.");
    }

    Set<Long> cuentasUnicas = asientoDTO.getMovimientos().stream()
                                        .map(CuentaAsientoDTO::getCuentaCodigo)
                                        .collect(Collectors.toSet());
    if (cuentasUnicas.size() < 2) {
        return ResponseEntity.badRequest().body("Los movimientos deben involucrar al menos dos cuentas diferentes.");
    }

    BigDecimal totalDebe = asientoDTO.getMovimientos().stream()
        .map(CuentaAsientoDTO::getDebe)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalHaber = asientoDTO.getMovimientos().stream()
        .map(CuentaAsientoDTO::getHaber)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    if (totalDebe.compareTo(totalHaber) != 0) {
        String mensaje = String.format(
            "El saldo no cierra: El total del debe (%s) no coincide con el total del haber (%s).",
            totalDebe, totalHaber
        );
        return ResponseEntity.badRequest().body(Map.of("mensaje", mensaje));
    }

    try {
        Asiento nuevoAsiento = asientoServicio.crearAsiento(asientoDTO, idUsuario);

        for (CuentaAsientoDTO movimientoDTO : asientoDTO.getMovimientos()) {
            Cuenta cuenta = cuentaServicio.buscarPorCodigo(movimientoDTO.getCuentaCodigo());
            if (cuenta == null) {
                return ResponseEntity.badRequest().body(
                    Map.of("mensaje", "La cuenta con código " + movimientoDTO.getCuentaCodigo() + " no existe.")
                );
            }

            CuentaAsiento nuevoMovimiento = new CuentaAsiento();
            nuevoMovimiento.setCuenta(cuenta);
            nuevoMovimiento.setAsiento(nuevoAsiento);
            nuevoMovimiento.setDebe(movimientoDTO.getDebe());
            nuevoMovimiento.setHaber(movimientoDTO.getHaber());

            BigDecimal saldoActual = Optional.ofNullable(cuenta.getSaldoActual()).orElse(BigDecimal.ZERO);

            // Calcular el nuevo saldo considerando el tipo de cuenta
            BigDecimal nuevoSaldo = calcularNuevoSaldo(cuenta, saldoActual, movimientoDTO);

            nuevoMovimiento.setSaldo(nuevoSaldo);

            cuentaAsientoServicio.crearMovimiento(nuevoMovimiento);

            cuenta.setSaldoActual(nuevoSaldo);
            cuentaServicio.actualizarCuenta(cuenta.getCodigo(), cuenta);
        }

        return ResponseEntity.ok(nuevoAsiento);
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(500).body(
            Map.of("mensaje", "Error interno del servidor: " + e.getMessage())
        );
    }
}

// Método para calcular el nuevo saldo según el tipo de cuenta
private BigDecimal calcularNuevoSaldo(Cuenta cuenta, BigDecimal saldoActual, CuentaAsientoDTO movimientoDTO) {
    BigDecimal debe = Optional.ofNullable(movimientoDTO.getDebe()).orElse(BigDecimal.ZERO);
    BigDecimal haber = Optional.ofNullable(movimientoDTO.getHaber()).orElse(BigDecimal.ZERO);

    switch (cuenta.getTipo().toLowerCase()) {
        case "activo":
        case "egreso":
            // El debe suma y el haber resta
            return saldoActual.add(debe).subtract(haber);

        case "pasivo":
        case "patrimonio":
        case "ingreso":
            // El debe resta y el haber suma
            return saldoActual.subtract(debe).add(haber);

        default:
            throw new IllegalArgumentException(
                "Tipo de cuenta desconocido: " + cuenta.getTipo()
            );
    }
}



    



@GetMapping("/listar")
public List<AsientoDTO> listarAsientos(
        @RequestParam(required = false) String fechaInicio,
        @RequestParam(required = false) String fechaFin) {

    // Formato para las fechas
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Si fechaFin es null o está en blanco, usa la fecha actual
    LocalDate fin = (fechaFin == null || fechaFin.isBlank()) 
            ? LocalDate.now() 
            : LocalDate.parse(fechaFin.trim(), formatter);

    // Si fechaInicio es null o está en blanco, usa 30 días atrás como valor por defecto
    LocalDate inicio = (fechaInicio == null || fechaInicio.isBlank()) 
            ? fin.minusDays(30) 
            : LocalDate.parse(fechaInicio.trim(), formatter);

    // Convertimos LocalDate a Date para usarlo en la consulta
    Date sqlFechaInicio = Date.valueOf(inicio);
    Date sqlFechaFin = Date.valueOf(fin);

    // Llamada al servicio y log de la cantidad de asientos obtenidos
    List<Asiento> asientos = asientoServicio.listarAsientos(sqlFechaInicio, sqlFechaFin);
    System.out.println("Número de asientos obtenidos: " + asientos.size());

    // Convertir a DTO
    List<AsientoDTO> asientosDTO = asientos.stream().map(asiento -> {
        AsientoDTO dto = new AsientoDTO();
        
        // Sumar un día a la fecha del asiento
        LocalDate fechaAsiento = new java.sql.Date(asiento.getFecha().getTime()).toLocalDate().plusDays(1);
        
        dto.setFecha(java.sql.Date.valueOf(fechaAsiento));
        dto.setDescripcion(asiento.getDescripcion());
        dto.setIdUsuario(asiento.getId_usuario());
        dto.setId(asiento.getId());

        // Convertir cuentasAsientos a CuentaAsientoDTO
        List<CuentaAsientoDTO> movimientosDTO = asiento.getCuentasAsientos().stream()
            .map(cuentaAsiento -> {
                CuentaAsientoDTO movimientoDTO = new CuentaAsientoDTO();
                movimientoDTO.setId(cuentaAsiento.getId());

                // Log en caso de cuenta nula
                if (cuentaAsiento.getCuenta() != null) {
                    movimientoDTO.setCuentaNombre(cuentaAsiento.getCuenta().getNombre());
                    movimientoDTO.setCuentaCodigo(cuentaAsiento.getCuenta().getCodigo());
                } else {
                    System.out.println("Cuenta nula en asiento ID: " + cuentaAsiento.getAsiento().getId());
                }

                movimientoDTO.setDebe(cuentaAsiento.getDebe());
                movimientoDTO.setHaber(cuentaAsiento.getHaber());
                movimientoDTO.setAsientoId(cuentaAsiento.getAsiento().getId());
                movimientoDTO.setSaldo(cuentaAsiento.getSaldo());

                return movimientoDTO;
            }).collect(Collectors.toList());

        dto.setMovimientos(movimientosDTO);
        return dto;
    }).collect(Collectors.toList());

    return asientosDTO;
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
        String encabezado = request.getHeader("Authorization");
        return encabezado != null && encabezado.startsWith("Bearer ") ? encabezado.substring(7) : null;
    }
}
