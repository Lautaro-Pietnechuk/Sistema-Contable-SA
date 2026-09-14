package com.sa.contable.DTO;

import java.time.LocalDateTime;

public record MovimientoCuentaDTO(
    Long id,
    LocalDateTime fecha,
    String numeroComprobante,
    Double monto,
    String tipo,
    String motivo
) {
    public MovimientoCuentaDTO(Long id, LocalDateTime fecha, String numeroComprobante, Double monto) {
        this(id, fecha, numeroComprobante, monto, "VENTA", null);
    }
}