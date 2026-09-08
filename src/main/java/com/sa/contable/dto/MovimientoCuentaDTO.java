package com.sa.contable.DTO;

import java.time.LocalDateTime;

public record MovimientoCuentaDTO(
    Long id,
    LocalDateTime fecha,
    String numeroComprobante,
    Double monto
) {}