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
    // Validar que el ID del usuario no sea nulo
    if (idUsuario == null) {
        return ResponseEntity.badRequest().body("El ID del usuario no puede ser nulo.");
    }

    // Verificar que haya al menos dos movimientos
    if (asientoDTO.getMovimientos() == null || asientoDTO.getMovimientos().size() < 2) {
        return ResponseEntity.badRequest().body("El asiento debe contener al menos dos movimientos.");
    }

    // Verificar que las cuentas de los movimientos sean diferentes
    Set<Long> cuentasUnicas = asientoDTO.getMovimientos().stream()
                                        .map(CuentaAsientoDTO::getCuentaCodigo)
                                        .collect(Collectors.toSet());
    if (cuentasUnicas.size() < 2) {
        return ResponseEntity.badRequest().body("Los movimientos deben involucrar al menos dos cuentas diferentes.");
    }

    // Calcular el total del debe y el haber
    BigDecimal totalDebe = asientoDTO.getMovimientos().stream()
        .map(CuentaAsientoDTO::getDebe)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalHaber = asientoDTO.getMovimientos().stream()
        .map(CuentaAsientoDTO::getHaber)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    // Verificar que el total de debe sea igual al total de haber
    if (totalDebe.compareTo(totalHaber) != 0) {
        String mensaje = String.format(
            "El saldo no cierra: El total del debe (%s) no coincide con el total del haber (%s).",
            totalDebe, totalHaber
        );
        return ResponseEntity.badRequest().body(Map.of("mensaje", mensaje));
    }

    // Si todas las validaciones pasan, proceder a crear el asiento
    try {
        Asiento nuevoAsiento = asientoServicio.crearAsiento(asientoDTO, idUsuario);

        // Crear los movimientos y asociarlos con el nuevo asiento
        for (CuentaAsientoDTO movimientoDTO : asientoDTO.getMovimientos()) {
            Cuenta cuenta = cuentaServicio.buscarPorCodigo(movimientoDTO.getCuentaCodigo());
            if (cuenta == null) {
                return ResponseEntity.badRequest().body(
                    Map.of("mensaje", "La cuenta con código " + movimientoDTO.getCuentaCodigo() + " no existe.")
                );
            }

            // Crear el nuevo movimiento
            CuentaAsiento nuevoMovimiento = new CuentaAsiento();
            nuevoMovimiento.setCuenta(cuenta);
            nuevoMovimiento.setAsiento(nuevoAsiento);
            nuevoMovimiento.setDebe(movimientoDTO.getDebe());
            nuevoMovimiento.setHaber(movimientoDTO.getHaber());

            // Calcular y establecer el nuevo saldo de la cuenta
            BigDecimal saldoActual = Optional.ofNullable(cuenta.getSaldoActual()).orElse(BigDecimal.ZERO);
            nuevoMovimiento.setSaldo(
                saldoActual.add(Optional.ofNullable(movimientoDTO.getDebe()).orElse(BigDecimal.ZERO))
                           .subtract(Optional.ofNullable(movimientoDTO.getHaber()).orElse(BigDecimal.ZERO))
            );

            // Guardar el movimiento
            cuentaAsientoServicio.crearMovimiento(nuevoMovimiento);

            // Actualizar el saldo de la cuenta
            cuenta.setSaldoActual(nuevoMovimiento.getSaldo());
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

        // Lógica para listar los asientos entre fechaInicio y fechaFin
        List<Asiento> asientos = asientoServicio.listarAsientos(sqlFechaInicio, sqlFechaFin);
        
        // Convertir a DTO
        List<AsientoDTO> asientosDTO = asientos.stream().map(asiento -> {
            AsientoDTO dto = new AsientoDTO();
            dto.setFecha(new java.sql.Date(asiento.getFecha().getTime()));
            dto.setDescripcion(asiento.getDescripcion());
            dto.setIdUsuario(asiento.getId_usuario());
            dto.setId(asiento.getId());
            
            // Convertir cuentasAsientos a CuentaAsientoDTO
            List<CuentaAsientoDTO> movimientosDTO = asiento.getCuentasAsientos().stream()
                .map(cuentaAsiento -> {
                    CuentaAsientoDTO movimientoDTO = new CuentaAsientoDTO();
                    movimientoDTO.setId(cuentaAsiento.getId());
                    
                    // Asegúrate de asignar todos los campos necesarios
                    if (cuentaAsiento.getCuenta() != null) {
                        movimientoDTO.setCuentaNombre(cuentaAsiento.getCuenta().getNombre()); // Nombre de la cuenta
                        movimientoDTO.setCuentaCodigo(cuentaAsiento.getCuenta().getCodigo()); // Código de la cuenta
                    }
                    movimientoDTO.setDebe(cuentaAsiento.getDebe());
                    movimientoDTO.setHaber(cuentaAsiento.getHaber());
                    movimientoDTO.setAsientoId(cuentaAsiento.getAsiento().getId()); // ID del asiento
                    movimientoDTO.setSaldo(cuentaAsiento.getSaldo()); // Saldo

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
