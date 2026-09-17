package com.sa.contable.DTO;

import java.time.LocalDateTime;

public record MovimientoCuentaDTO(
    Long id,
    LocalDateTime fecha,
    String numeroComprobante,
    Double monto,
    Double totalDeudaCorriente,
    String tipo,
    String motivo
) {
    public MovimientoCuentaDTO(Long id, LocalDateTime fecha, String numeroComprobante, Double monto) {
        this(id, fecha, numeroComprobante, monto, monto, "VENTA", null);
    }

    public MovimientoCuentaDTO(Long id, LocalDateTime fecha, String numeroComprobante, Double monto,
                               Double totalDeudaCorriente) {
        this(id, fecha, numeroComprobante, monto, totalDeudaCorriente, "VENTA", null);
    }

    public MovimientoCuentaDTO(Long id, LocalDateTime fecha, String numeroComprobante, Double monto,
                               String tipo, String motivo) {
        this(id, fecha, numeroComprobante, monto, monto, tipo, motivo);
    }
}