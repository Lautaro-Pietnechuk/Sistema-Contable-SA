package com.sa.contable.dto;

import java.time.LocalDateTime;

public record MovimientoCuentaDTO(
    Long id,
    LocalDateTime fecha,
    String numeroComprobante,
    Double monto
) {}