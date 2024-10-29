package com.sa.contable.controladores;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sa.contable.configuracion.JwtUtil;
import com.sa.contable.dto.AsientoDTO;
import com.sa.contable.dto.CuentaAsientoDTO;
import com.sa.contable.entidades.Asiento;
import com.sa.contable.entidades.Cuenta;
import com.sa.contable.entidades.CuentaAsiento;
import com.sa.contable.entidades.Usuario;
import com.sa.contable.servicios.AsientoServicio;
import com.sa.contable.servicios.CuentaAsientoServicio;
import com.sa.contable.servicios.CuentaServicio;
import com.sa.contable.servicios.UsuarioServicio;

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

    @Autowired
    private UsuarioServicio usuarioServicio;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Crear un nuevo asiento contable
    @PostMapping("/crear/{idUsuario}")
public ResponseEntity<?> crearAsiento(@PathVariable Long idUsuario, @RequestBody AsientoDTO asientoDTO) {
    if (idUsuario == null) {
        return ResponseEntity.badRequest().body(Map.of("mensaje", "El ID del usuario no puede ser nulo."));
    }

    if (asientoDTO.getMovimientos() == null || asientoDTO.getMovimientos().isEmpty()) {
        return ResponseEntity.badRequest().body(Map.of("mensaje", "El asiento debe contener al menos un movimiento."));
    }

    if (asientoDTO.getFecha() == null) {
        return ResponseEntity.badRequest().body(Map.of("mensaje", "La fecha no puede ser nula."));
    }

    try {
        // Validar que el usuario exista
        Usuario usuario = usuarioServicio.obtenerUsuarioPorId(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        Asiento nuevoAsiento = asientoServicio.crearAsiento(asientoDTO, idUsuario);

        return ResponseEntity.ok(nuevoAsiento);
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("mensaje", "Error interno: " + e.getMessage()));
    }
}






    // Calcular total de debe o haber
    private BigDecimal calcularTotal(AsientoDTO asientoDTO, boolean esDebe) {
        return asientoDTO.getMovimientos().stream()
                .map(mov -> esDebe ? mov.getDebe() : mov.getHaber())
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Procesar los movimientos y actualizar saldos
    private void procesarMovimientos(AsientoDTO asientoDTO, Asiento nuevoAsiento) {
        for (CuentaAsientoDTO movimientoDTO : asientoDTO.getMovimientos()) {
            Cuenta cuenta = cuentaServicio.buscarPorCodigo(movimientoDTO.getCuentaCodigo());
            if (cuenta == null) {
                throw new IllegalArgumentException("La cuenta con código " + movimientoDTO.getCuentaCodigo() + " no existe.");
            }
    
            // Crear un nuevo movimiento y asignarle el asiento
            CuentaAsiento nuevoMovimiento = new CuentaAsiento();
            nuevoMovimiento.setCuenta(cuenta);
            nuevoMovimiento.setAsiento(nuevoAsiento);  // Asignar el asiento
            nuevoMovimiento.setDebe(movimientoDTO.getDebe());
            nuevoMovimiento.setHaber(movimientoDTO.getHaber());
    
            // Calcular el saldo
            BigDecimal saldoActual = Optional.ofNullable(cuenta.getSaldoActual()).orElse(BigDecimal.ZERO);
            BigDecimal nuevoSaldo = calcularNuevoSaldo(cuenta, saldoActual, movimientoDTO);
            nuevoMovimiento.setSaldo(nuevoSaldo);
    
            // Guardar el movimiento
            cuentaAsientoServicio.crearMovimiento(nuevoMovimiento);
    
            // Actualizar el saldo de la cuenta
            cuenta.setSaldoActual(nuevoSaldo);
            cuentaServicio.actualizarCuenta(cuenta.getCodigo(), cuenta);
        }
    }
    

    // Calcular el nuevo saldo según el tipo de cuenta
    private BigDecimal calcularNuevoSaldo(Cuenta cuenta, BigDecimal saldoActual, CuentaAsientoDTO movimientoDTO) {
        BigDecimal debe = Optional.ofNullable(movimientoDTO.getDebe()).orElse(BigDecimal.ZERO);
        BigDecimal haber = Optional.ofNullable(movimientoDTO.getHaber()).orElse(BigDecimal.ZERO);

        switch (cuenta.getTipo().toLowerCase()) {
            case "activo":
            case "egreso":
                return saldoActual.add(debe).subtract(haber);
            case "pasivo":
            case "patrimonio":
            case "ingreso":
                return saldoActual.subtract(debe).add(haber);
            default:
                throw new IllegalArgumentException("Tipo de cuenta desconocido: " + cuenta.getTipo());
        }
    }

    // Listar asientos entre dos fechas con paginación
    @GetMapping("/listar")
    public ResponseEntity<Map<String, Object>> listarAsientos(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            Pageable pageable) {

        LocalDate fin = (fechaFin == null || fechaFin.isBlank())
                ? LocalDate.now()
                : LocalDate.parse(fechaFin.trim(), FORMATTER);

        LocalDate inicio = (fechaInicio == null || fechaInicio.isBlank())
                ? fin.minusDays(30)
                : LocalDate.parse(fechaInicio.trim(), FORMATTER);

        Page<Asiento> pageAsientos = asientoServicio.listarAsientos(inicio, fin, pageable);
        List<AsientoDTO> asientosDTO = pageAsientos.getContent().stream()
                .map(this::convertirAsientoADTO)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("asientos", asientosDTO);
        response.put("totalElementos", pageAsientos.getTotalElements());

        return ResponseEntity.ok(response);
    }

    // Convertir Asiento a AsientoDTO
    private AsientoDTO convertirAsientoADTO(Asiento asiento) {
        AsientoDTO dto = new AsientoDTO();
        dto.setId(asiento.getId());
        dto.setFecha(asiento.getFecha());
        dto.setDescripcion(asiento.getDescripcion());
        dto.setIdUsuario(asiento.getId_usuario());

        List<CuentaAsientoDTO> movimientos = asiento.getCuentasAsientos().stream()
                .map(this::convertirCuentaAsientoADTO)
                .collect(Collectors.toList());
        dto.setMovimientos(movimientos);

        return dto;
    }

    // Convertir CuentaAsiento a CuentaAsientoDTO
    private CuentaAsientoDTO convertirCuentaAsientoADTO(CuentaAsiento cuentaAsiento) {
        CuentaAsientoDTO dto = new CuentaAsientoDTO();
        dto.setCuentaNombre(cuentaAsiento.getCuenta().getNombre());
        dto.setCuentaCodigo(cuentaAsiento.getCuenta().getCodigo());
        dto.setDebe(cuentaAsiento.getDebe());
        dto.setHaber(cuentaAsiento.getHaber());
        dto.setAsientoId(cuentaAsiento.getAsiento().getId());
        dto.setSaldo(cuentaAsiento.getSaldo());
        return dto;
    }
}
