package com.sa.contable.controladores;



import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sa.contable.dto.AsientoDTO;
import com.sa.contable.dto.CuentaAsientoDTO;
import com.sa.contable.entidades.Asiento;
import com.sa.contable.servicios.AsientoServicio;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/asientos")
public class AsientoControlador {

    @Autowired
    private AsientoServicio asientoServicio;

    // Endpoint para crear un nuevo asiento contable
    @PostMapping("/crear/{idUsuario}")
public ResponseEntity<?> crearAsiento(@PathVariable Long idUsuario, @RequestBody AsientoDTO asientoDTO) {
    if (idUsuario == null) {
        return ResponseEntity.badRequest().body("El ID del usuario no puede ser nulo");
    } // Establece el ID del usuario en el DTO
    
    try {
        Asiento nuevoAsiento = asientoServicio.crearAsiento(asientoDTO, idUsuario);
        return ResponseEntity.ok(nuevoAsiento);
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    } catch (Exception e) {
        e.printStackTrace(); // Mostrar detalles del error en los logs
        return ResponseEntity.status(500).body("Error interno del servidor.");
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
        
        // Aquí convertimos cuentasAsientos a CuentaAsientoDTO
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
        movimientoDTO.setAsientoId(cuentaAsiento.getAsiento()); // ID del asiento
        movimientoDTO.setSaldo(cuentaAsiento.getSaldo()); // Saldo

        return movimientoDTO;
    }).collect(Collectors.toList());


        dto.setMovimientos(movimientosDTO);
        return dto;
    }).collect(Collectors.toList());

    return asientosDTO;
}


    

    

    
}


