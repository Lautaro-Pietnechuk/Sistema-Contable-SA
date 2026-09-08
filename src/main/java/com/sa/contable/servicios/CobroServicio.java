package com.sa.contable.servicios;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sa.contable.DTO.AsientoDTO;
import com.sa.contable.DTO.CuentaAsientoDTO;
import com.sa.contable.entidades.Cliente;
import com.sa.contable.entidades.Cobro;
import com.sa.contable.entidades.Venta;
import com.sa.contable.repositorios.ClienteRepository;
import com.sa.contable.repositorios.CobroRepositorio;
import com.sa.contable.repositorios.VentaRepositorio;

@Service
public class CobroServicio {

    private static final Logger logger = LoggerFactory.getLogger(CobroServicio.class);

    private static final Long cuentaCaja = 111L; // caja - efectivo
    private static final Long cuentaBanco = 113L; // banco c/c - débito
    private static final Long cuentaDeudores = 121L; // deudores por ventas - cuenta corriente

    @Autowired
    private CobroRepositorio cobroRepositorio;

    @Autowired
    private VentaRepositorio ventaRepositorio;

    @Autowired
    private ClienteRepository clienteRepositorio;

    @Autowired
    private AsientoServicio asientoServicio;

    @Transactional
    public void registrarCobro(Long clienteId, Double montoCobrado, String metodoPago, Long usuarioId) {

        logger.info("Iniciando proceso de registro de cobro para el Cliente ID: {} por un monto de ${}", clienteId, montoCobrado);

        // 0. Buscar al cliente en la base de datos
        Cliente cliente = clienteRepositorio.findById(clienteId)
                .orElseThrow(() -> {
                    logger.error("Error al registrar cobro: No se encontró al Cliente ID: {}", clienteId);
                    return new RuntimeException("Cliente no encontrado con ID: " + clienteId);
                });

        logger.debug("Cliente identificado exitosamente: {}", cliente.getNombre());

        // 1. Guardás el recibo de cobro en su tabla para la caja
        Cobro cobro = new Cobro();
        cobro.setMonto(montoCobrado);
        cobro.setCliente(cliente);
        cobro.setMetodoPago(metodoPago != null ? metodoPago.toUpperCase() : "EFECTIVO");

        cobroRepositorio.save(cobro);
        logger.info("Recibo de Cobro guardado en base de datos. ID asignado: {}, Método: {}", cobro.getId(), cobro.getMetodoPago());

        // 1.5 Crear asiento contable para el cobro
        crearAsientoCobro(cobro, metodoPago, montoCobrado, usuarioId);

        // 2. Buscás las ventas que te debe ese cliente, de la más vieja a la más nueva
        logger.debug("Buscando cuentas corrientes con saldo pendiente para el cliente: {}", cliente.getNombre());
        List<Venta> deudas = ventaRepositorio.findByClienteIdAndEstadoOrderByFechaAsc(clienteId, "PENDIENTE");
        logger.info("Se encontraron {} facturas pendientes de pago para este cliente", deudas.size());

        Double plataDisponible = montoCobrado;

        for (Venta venta : deudas) {
            if (plataDisponible <= 0) {
                logger.debug("Se agotó la plata disponible del cobro. Cortando la imputación de facturas.");
                break;
            }

            Double saldoDeLaFactura = venta.getSaldoPendiente();
            logger.debug("Procesando Venta ID: {} | Saldo pendiente actual: ${} | Plata restante en mano: ${}",
                    venta.getId(), saldoDeLaFactura, plataDisponible);

            if (plataDisponible >= saldoDeLaFactura) {
                // La plata alcanza para apagar esta factura entera
                venta.setSaldoPendiente(0.0);
                venta.setEstado("PAGADA");
                plataDisponible -= saldoDeLaFactura;

                logger.info("Venta ID: {} cubierta totalmente. Nuevo saldo: $0.0 | Estado: PAGADA", venta.getId());
            } else {
                // La plata no alcanza para toda la factura, es un pago parcial
                venta.setSaldoPendiente(saldoDeLaFactura - plataDisponible);
                logger.info("Venta ID: {} cubierta parcialmente. Cobrado: ${} | Queda un remanente de deuda de: ${}",
                        venta.getId(), plataDisponible, venta.getSaldoPendiente());

                plataDisponible = 0.0; // Se acabó la plata del cobro
            }
            ventaRepositorio.save(venta);
        }

        logger.info("Finalizado el proceso de imputación. Vuelto/Excedente no imputado: ${}", plataDisponible);
    }

    private void crearAsientoCobro(Cobro cobro, String metodoPago, Double monto, Long usuarioId) {
        try {
            // Crear Asiento Contable DE COBRO
            AsientoDTO asientoDTO = new AsientoDTO();
            asientoDTO.setFecha(LocalDate.now());
            asientoDTO.setDescripcion("Cobro Cliente " + cobro.getCliente().getNombre() + " - Recibo Nro " + cobro.getId());
            asientoDTO.setNombreUsuario("UsuarioID: " + usuarioId);

            // Movimiento Debe - Caja o Banco según método de pago
            CuentaAsientoDTO movimientoDebe = new CuentaAsientoDTO();
            if ("EFECTIVO".equalsIgnoreCase(metodoPago)) {
                movimientoDebe.setCuentaCodigo(cuentaCaja);
            } else {
                movimientoDebe.setCuentaCodigo(cuentaBanco);
            }
            movimientoDebe.setDebe(BigDecimal.valueOf(monto));
            movimientoDebe.setHaber(BigDecimal.valueOf(0.0));

            // Movimiento Haber - Deudores por Ventas
            CuentaAsientoDTO movimientoHaber = new CuentaAsientoDTO();
            movimientoHaber.setCuentaCodigo(cuentaDeudores);
            movimientoHaber.setDebe(BigDecimal.valueOf(0.0));
            movimientoHaber.setHaber(BigDecimal.valueOf(monto));

            asientoDTO.setMovimientos(List.of(movimientoDebe, movimientoHaber));
            asientoServicio.crearAsiento(asientoDTO, usuarioId);

            logger.info("Asiento de cobro creado para cobro ID: {}", cobro.getId());
        } catch (Exception e) {
            logger.error("Error al crear asiento de cobro: {}", e.getMessage());
            throw new RuntimeException("Error al registrar asiento de cobro: " + e.getMessage());
        }
    }

    public List<Cobro> obtenerCobrosPorCliente(Long clienteId, String desdeStr, String hastaStr) {
        logger.debug("Solicitando historial de cobros en base de datos para el Cliente ID: {}", clienteId);
        
        // Parseo de la fecha inicial (desdeStr)
        LocalDateTime desde = (desdeStr != null && !desdeStr.trim().isEmpty())
                ? LocalDate.parse(desdeStr).atStartOfDay()
                : LocalDateTime.of(2000, 1, 1, 0, 0);

        // Parseo de la fecha final (hastaStr)
        LocalDateTime hasta = (hastaStr != null && !hastaStr.trim().isEmpty())
                ? LocalDate.parse(hastaStr).atTime(LocalTime.MAX)
                : LocalDate.now().atTime(LocalTime.MAX);
                
        return cobroRepositorio.findByClienteIdAndFechaBetweenOrderByFechaDesc(clienteId, desde, hasta);
    }

    @Transactional
    public void anularCobro(Long cobroId, String motivo, Long usuarioId) {
        logger.warn("Iniciando solicitud de ANULACIÓN para el Cobro ID: {} - Motivo: {}", cobroId, motivo);

        Cobro cobro = cobroRepositorio.findById(cobroId)
                .orElseThrow(() -> {
                    logger.error("Anulación fallida: No se encontró el Cobro ID: {}", cobroId);
                    return new RuntimeException("Cobro no encontrado con ID: " + cobroId);
                });

        if (cobro.getAnulado()) {
            logger.warn("El Cobro ID: {} ya figuraba como anulado en el sistema. Abortando operación.", cobroId);
            throw new RuntimeException("El cobro ya está anulado.");
        }

        cobro.setAnulado(true);
        cobroRepositorio.save(cobro);
        logger.warn("El estado del Cobro ID: {} ha sido cambiado a ANULADO con éxito. Motivo: {}", cobro.getId(), motivo);

        // Registrar asiento de reversa para anulación
        crearAsientoAnulacion(cobro, motivo, usuarioId);

        // Lógica para revertir la imputación del cobro en las ventas
        Long idDelCliente = cobro.getCliente().getId();
        logger.debug("Buscando facturas canceladas para el Cliente ID: {} para restablecer la deuda anterior", idDelCliente);
        List<Venta> ventasImputadas = ventaRepositorio.findByClienteIdAndEstadoOrderByFechaAsc(idDelCliente, "PAGADA");

        Double montoARevertir = cobro.getMonto();
        logger.info("Monto total a devolver a las cuentas corrientes: ${}", montoARevertir);

        for (Venta venta : ventasImputadas) {
            if (montoARevertir <= 0) {
                break;
            }

            Double saldoActual = venta.getSaldoPendiente();

            if (saldoActual == 0) {
                // Esta factura estaba completamente pagada, ahora se vuelve pendiente
                venta.setSaldoPendiente(montoARevertir);
                venta.setEstado("PENDIENTE");

                logger.info("Reversión aplicada a Venta ID: {}. Pasa de PAGADA a PENDIENTE con un saldo de ${}",
                        venta.getId(), venta.getSaldoPendiente());
                montoARevertir = 0.0;
            } else {
                // Esta factura ya tenía saldo pendiente, le sumamos el monto del cobro anulado
                venta.setSaldoPendiente(saldoActual + montoARevertir);

                logger.info("Reversión parcial aplicada a Venta ID: {}. Nuevo saldo acumulado de deuda: ${}",
                        venta.getId(), venta.getSaldoPendiente());
                montoARevertir = 0.0;
            }
            ventaRepositorio.save(venta);
        }

        logger.info("Proceso de desimputación por anulación completado.");
    }

    private void crearAsientoAnulacion(Cobro cobro, String motivo, Long usuarioId) {
        try {
            // Crear Asiento Contable DE ANULACIÓN (asiento reverso)
            AsientoDTO asientoDTO = new AsientoDTO();
            asientoDTO.setFecha(LocalDate.now());
            asientoDTO.setDescripcion("ANULACIÓN - Cobro Cliente " + cobro.getCliente().getNombre() + " - Recibo Nro " + cobro.getId() + " - Motivo: " + motivo);
            asientoDTO.setNombreUsuario("UsuarioID: " + usuarioId);

            // Movimiento Debe - Deudores por Ventas (reverso del Haber original)
            CuentaAsientoDTO movimientoDebe = new CuentaAsientoDTO();
            movimientoDebe.setCuentaCodigo(cuentaDeudores);
            movimientoDebe.setDebe(BigDecimal.valueOf(cobro.getMonto()));
            movimientoDebe.setHaber(BigDecimal.valueOf(0.0));

            // Movimiento Haber - Caja o Banco (reverso del Debe original)
            CuentaAsientoDTO movimientoHaber = new CuentaAsientoDTO();
            if ("EFECTIVO".equalsIgnoreCase(cobro.getMetodoPago())) {
                movimientoHaber.setCuentaCodigo(cuentaCaja);
            } else {
                movimientoHaber.setCuentaCodigo(cuentaBanco);
            }
            movimientoHaber.setDebe(BigDecimal.valueOf(0.0));
            movimientoHaber.setHaber(BigDecimal.valueOf(cobro.getMonto()));

            asientoDTO.setMovimientos(List.of(movimientoDebe, movimientoHaber));
            asientoServicio.crearAsiento(asientoDTO, usuarioId);

            logger.info("Asiento de anulación creado para cobro ID: {}", cobro.getId());
        } catch (Exception e) {
            logger.error("Error al crear asiento de anulación para cobro: {}", e.getMessage());
            throw new RuntimeException("Error al registrar asiento de anulación: " + e.getMessage());
        }
    }
}
