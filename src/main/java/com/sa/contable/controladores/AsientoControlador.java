package com.sa.contable.controladores;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Crear un nuevo asiento contable
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
            String mensaje = String.format("El saldo no cierra: El total del debe (%s) no coincide con el total del haber (%s).", totalDebe, totalHaber);
            return ResponseEntity.badRequest().body(Map.of("mensaje", mensaje));
        }

        try {
            Asiento nuevoAsiento = asientoServicio.crearAsiento(asientoDTO, idUsuario);
            procesarMovimientos(asientoDTO, nuevoAsiento);
            return ResponseEntity.ok(nuevoAsiento);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Error interno del servidor: " + e.getMessage()));
        }
    }

    // Procesar los movimientos y actualizar saldos
    private void procesarMovimientos(AsientoDTO asientoDTO, Asiento nuevoAsiento) {
        for (CuentaAsientoDTO movimientoDTO : asientoDTO.getMovimientos()) {
            Cuenta cuenta = cuentaServicio.buscarPorCodigo(movimientoDTO.getCuentaCodigo());
            if (cuenta == null) {
                throw new IllegalArgumentException("La cuenta con código " + movimientoDTO.getCuentaCodigo() + " no existe.");
            }

            CuentaAsiento nuevoMovimiento = new CuentaAsiento();
            nuevoMovimiento.setCuenta(cuenta);
            nuevoMovimiento.setAsiento(nuevoAsiento);
            nuevoMovimiento.setDebe(movimientoDTO.getDebe());
            nuevoMovimiento.setHaber(movimientoDTO.getHaber());

            BigDecimal saldoActual = Optional.ofNullable(cuenta.getSaldoActual()).orElse(BigDecimal.ZERO);
            BigDecimal nuevoSaldo = calcularNuevoSaldo(cuenta, saldoActual, movimientoDTO);

            nuevoMovimiento.setSaldo(nuevoSaldo);

            cuentaAsientoServicio.crearMovimiento(nuevoMovimiento);

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

    // Listar asientos entre dos fechas (Libro Diario)
    @GetMapping("/libro-diario")
    public ResponseEntity<Map<String, Object>> libroDiario(
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
        System.out.println("Número de asientos obtenidos: " + pageAsientos.getTotalElements());

        List<AsientoDTO> asientosDTO = pageAsientos.getContent().stream()
                .map(asiento -> {
                    AsientoDTO dto = new AsientoDTO();
                    dto.setId(asiento.getId());
                    dto.setFecha(asiento.getFecha());
                    dto.setDescripcion(asiento.getDescripcion());
                    dto.setIdUsuario(asiento.getId_usuario());
                    List<CuentaAsientoDTO> movimientos = asiento.getCuentasAsientos().stream()
                            .map(cuentaAsiento -> {
                                CuentaAsientoDTO cuentaDTO = new CuentaAsientoDTO();
                                cuentaDTO.setCuentaNombre(cuentaAsiento.getCuenta().getNombre());
                                cuentaDTO.setCuentaCodigo(cuentaAsiento.getCuenta().getCodigo());
                                cuentaDTO.setDebe(cuentaAsiento.getDebe());
                                cuentaDTO.setHaber(cuentaAsiento.getHaber());
                                cuentaDTO.setAsientoId(asiento.getId());
                                cuentaDTO.setSaldo(cuentaAsiento.getSaldo());
                                return cuentaDTO;
                            })
                            .collect(Collectors.toList());
                    dto.setMovimientos(movimientos);
                    return dto;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("asientos", asientosDTO);
        response.put("totalElementos", pageAsientos.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/libro-mayor")
    public ResponseEntity<Map<String, Object>> libroMayor(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(required = true) Long cuentaCodigo, // Sigue siendo obligatorio
            Pageable pageable) {
    
        // Validación de la cuentaCodigo, asegurando que no sea nulo.
        if (cuentaCodigo == null) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "El código de cuenta es obligatorio."));
        }
    
        LocalDate fin = (fechaFin == null || fechaFin.isBlank())
                ? LocalDate.now()
                : LocalDate.parse(fechaFin.trim(), FORMATTER);
    
        LocalDate inicio = (fechaInicio == null || fechaInicio.isBlank())
                ? fin.minusDays(30)
                : LocalDate.parse(fechaInicio.trim(), FORMATTER);
    
        // Llamar al servicio para listar los asientos por cuenta
        Page<Asiento> pageAsientos = asientoServicio.listarAsientosPorCuenta(inicio, fin, cuentaCodigo, pageable);
    
        List<AsientoDTO> asientosDTO = pageAsientos.getContent().stream()
                .map(asiento -> {
                    AsientoDTO dto = new AsientoDTO();
                    dto.setId(asiento.getId());
                    dto.setFecha(asiento.getFecha());
                    dto.setDescripcion(asiento.getDescripcion());
                    dto.setIdUsuario(asiento.getId_usuario());
                    
                    // Filtrar los movimientos para mostrar solo los de la cuenta seleccionada
                    List<CuentaAsientoDTO> movimientos = asiento.getCuentasAsientos().stream()
                            .filter(cuentaAsiento -> cuentaAsiento.getCuenta().getCodigo().equals(cuentaCodigo)) // Filtrar por cuentaCodigo
                            .map(cuentaAsiento -> {
                                CuentaAsientoDTO cuentaDTO = new CuentaAsientoDTO();
                                cuentaDTO.setCuentaNombre(cuentaAsiento.getCuenta().getNombre());
                                cuentaDTO.setCuentaCodigo(cuentaAsiento.getCuenta().getCodigo());
                                cuentaDTO.setDebe(cuentaAsiento.getDebe());
                                cuentaDTO.setHaber(cuentaAsiento.getHaber());
                                cuentaDTO.setAsientoId(asiento.getId());
                                cuentaDTO.setSaldo(cuentaAsiento.getSaldo());
                                return cuentaDTO;
                            })
                            .collect(Collectors.toList());
                    
                    dto.setMovimientos(movimientos);
                    return dto;
                })
                .collect(Collectors.toList());
    
        Map<String, Object> response = new HashMap<>();
        response.put("asientos", asientosDTO);
        response.put("totalElementos", pageAsientos.getTotalElements());
    
        return ResponseEntity.ok(response);
    }
   // Listar todos los asientos
@GetMapping("/todos")
public ResponseEntity<List<AsientoDTO>> listarAsientos(Pageable pageable) {
    LocalDate inicio = LocalDate.of(2000, 1, 1); // Cambia esta fecha según lo necesites
    LocalDate fin = LocalDate.now();
    Page<Asiento> pageAsientos = asientoServicio.listarAsientos(inicio, fin, pageable); // Llama al método correctamente

    List<AsientoDTO> asientosDTO = pageAsientos.getContent().stream().map(asiento -> {
        AsientoDTO dto = new AsientoDTO();
        dto.setId(asiento.getId());
        dto.setFecha(asiento.getFecha());
        dto.setDescripcion(asiento.getDescripcion());
        return dto;
    }).collect(Collectors.toList());
    return ResponseEntity.ok(asientosDTO);
}
}
