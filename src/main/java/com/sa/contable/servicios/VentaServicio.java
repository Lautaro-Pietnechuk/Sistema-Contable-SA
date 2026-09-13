package com.sa.contable.servicios;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sa.contable.DTO.AsientoDTO;
import com.sa.contable.DTO.CuentaAsientoDTO;
import com.sa.contable.DTO.DetalleVentaDTO;
import com.sa.contable.DTO.VentaDTO;
import com.sa.contable.entidades.Cliente;
import com.sa.contable.entidades.DetalleVenta;
import com.sa.contable.entidades.Producto;
import com.sa.contable.entidades.Venta;
import com.sa.contable.repositorios.ClienteRepository;
import com.sa.contable.repositorios.ProductoRepositorio;
import com.sa.contable.repositorios.VentaRepositorio;

@Service
public class VentaServicio {

    private static final Logger logger = LoggerFactory.getLogger(VentaServicio.class);

    private static final Long cuentaDebeCuentaCorriente = 121L; // deudores por venta - cuenta corriente
    private static final Long cuentaDebeEfectivo = 111L; // caja - efectivo
    private static final Long cuentaDebeDebito = 113L; // banco c/c - débito
    private static final Long cuentaHaber = 411L; // ventas

    private static final Long cuentaHaberCMV = 131L; // ventas
    private static final Long cuentaDebeCMV = 510L; // ventas

    @Autowired
    private VentaRepositorio ventaRepositorio;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProductoRepositorio productoRepositorio;

    @Autowired
    private AsientoServicio asientoServicio;

    @Transactional(readOnly = true)
    public List<VentaDTO> obtenerTodas() {
        return ventaRepositorio.findAll()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VentaDTO obtenerPorId(Long id) {
        Venta venta = ventaRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con id: " + id));
        return convertirADTO(venta);
    }

    @Transactional(readOnly = true)
    public VentaDTO obtenerPorComprobante(String numeroComprobante) {
        Venta venta = ventaRepositorio.findByNumeroComprobante(numeroComprobante)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con comprobante: " + numeroComprobante));
        return convertirADTO(venta);
    }

    @Transactional(readOnly = true)
    public List<VentaDTO> obtenerPorCliente(Long clienteId) {
        return ventaRepositorio.findByClienteId(clienteId)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VentaDTO> obtenerPorRangoFechas(LocalDateTime desde, LocalDateTime hasta) {
        return ventaRepositorio.findByFechaBetween(desde, hasta)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public VentaDTO crearVenta(VentaDTO ventaDTO, Long usuarioId) {
        // Buscar cliente
        Cliente cliente = clienteRepository.findById(ventaDTO.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con id: " + ventaDTO.getClienteId()));
        Double totalVenta = 0.0;
        BigDecimal totalCMV = new BigDecimal("0.0");
        // Crear venta
        Venta venta = new Venta();
        venta.setNumeroComprobante("TEMP");
        venta.setFecha(ventaDTO.getFecha() != null ? ventaDTO.getFecha() : LocalDateTime.now());
        venta.setCliente(cliente);
        venta.setTipoDePago(ventaDTO.getTipoDePago());
        venta.setObservaciones(ventaDTO.getObservaciones());
        venta.setEstado("PENDIENTE");
        venta.setTotal(0.0);
        Venta ventaGuardada = ventaRepositorio.save(venta);
        String comprobanteReal = "V-" + String.format("%05d", ventaGuardada.getId());
        venta.setNumeroComprobante(comprobanteReal);

        // Procesar detalles
        Double subtotalVenta = 0.0;

        for (DetalleVentaDTO detalleDTO : ventaDTO.getDetalles()) {
            Producto producto = productoRepositorio.findById(detalleDTO.getProductoId())
                    .orElseThrow(() -> new RuntimeException(
                    "Producto no encontrado con id: " + detalleDTO.getProductoId()));

            // Validar stock
            if (producto.getStock() < detalleDTO.getCantidad()) {
                throw new RuntimeException(
                        "Stock insuficiente para el producto: " + producto.getNombre()
                        + ". Disponible: " + producto.getStock()
                        + ", Solicitado: " + detalleDTO.getCantidad());
            }

            // ✅ SOLUCIÓN: Definimos el precio seguro para evitar NullPointerException
            Double precioParaCalculo = detalleDTO.getPrecioUnitario() != null
                    ? detalleDTO.getPrecioUnitario()
                    : producto.getPrecio();
            // SUMAR PARA EL ASIENTO
            totalVenta += detalleDTO.getCantidad() * precioParaCalculo;
            totalCMV = totalCMV.add(BigDecimal.valueOf(detalleDTO.getCantidad()).multiply(producto.getCostoPromedio()));

            // Crear detalle de venta
            DetalleVenta detalle = new DetalleVenta();
            detalle.setProducto(producto);
            detalle.setCantidad(detalleDTO.getCantidad());
            detalle.setPrecioUnitario(precioParaCalculo);
            detalle.setSubtotal(detalle.getCantidad() * precioParaCalculo);

            venta.agregarDetalle(detalle);

            // Descontar stock
            producto.setStock(producto.getStock() - detalleDTO.getCantidad());
            productoRepositorio.save(producto);

            subtotalVenta += detalle.getSubtotal();
        }

        // Crear Asiento Contable DE VENTA
        AsientoDTO asientoDTO_VENTA = new AsientoDTO();
        asientoDTO_VENTA.setFecha(venta.getFecha().toLocalDate());
        asientoDTO_VENTA.setDescripcion("Venta - Comprobante: " + venta.getNumeroComprobante());
        asientoDTO_VENTA.setNombreUsuario("UsuarioID: " + usuarioId);

        CuentaAsientoDTO movimientoDebeVENTA = new CuentaAsientoDTO();
        switch (ventaDTO.getTipoDePago()) {
            case "EFECTIVO":
                movimientoDebeVENTA.setCuentaCodigo(cuentaDebeEfectivo);
                break;
            case "DEBITO":
                movimientoDebeVENTA.setCuentaCodigo(cuentaDebeDebito);
                break;
            case "CUENTA_CORRIENTE":
                movimientoDebeVENTA.setCuentaCodigo(cuentaDebeCuentaCorriente);
                break;
            default:
                throw new RuntimeException("Tipo de pago no válido. Debe ser: EFECTIVO, DEBITO o CUENTA_CORRIENTE");
        }
        movimientoDebeVENTA.setDebe(BigDecimal.valueOf(totalVenta));
        movimientoDebeVENTA.setHaber(BigDecimal.valueOf(0.0));

        CuentaAsientoDTO movimientoHaberVENTA = new CuentaAsientoDTO();
        movimientoHaberVENTA.setCuentaCodigo(cuentaHaber);
        movimientoHaberVENTA.setDebe(BigDecimal.valueOf(0.0));
        movimientoHaberVENTA.setHaber(BigDecimal.valueOf(totalVenta));

        asientoDTO_VENTA.setMovimientos(List.of(movimientoDebeVENTA, movimientoHaberVENTA));
        asientoServicio.crearAsiento(asientoDTO_VENTA, usuarioId);

        // ASIENTO DE CMV

        AsientoDTO asientoDTO_CMV = new AsientoDTO();
        asientoDTO_CMV.setFecha(venta.getFecha().toLocalDate());
        asientoDTO_CMV.setDescripcion("Costo de mercaderías vendidas - Comprobante: " + venta.getNumeroComprobante());
        asientoDTO_CMV.setNombreUsuario("UsuarioID: " + usuarioId);

        CuentaAsientoDTO movimientoDebeCMV = new CuentaAsientoDTO();

        movimientoDebeCMV.setCuentaCodigo(cuentaDebeCMV);
        movimientoDebeCMV.setDebe(totalCMV);
        movimientoDebeCMV.setHaber(BigDecimal.valueOf(0.0));

        CuentaAsientoDTO movimientoHaberCMV = new CuentaAsientoDTO();
        movimientoHaberCMV.setCuentaCodigo(cuentaHaberCMV);
        movimientoHaberCMV.setDebe(BigDecimal.valueOf(0.0));
        movimientoHaberCMV.setHaber(totalCMV);

        asientoDTO_CMV.setMovimientos(List.of(movimientoDebeCMV, movimientoHaberCMV));
        asientoServicio.crearAsiento(asientoDTO_CMV, usuarioId);

        venta.setTotal(subtotalVenta);
        if ("CUENTA_CORRIENTE".equals(ventaDTO.getTipoDePago())) {
            Double saldoAFavor = cliente.getSaldoAFavor();
            Double saldoUsado = Math.min(saldoAFavor, venta.getTotal());
            Double saldoPendiente = venta.getTotal() - saldoUsado;

            cliente.setSaldoAFavor(saldoAFavor - saldoUsado);
            clienteRepository.save(cliente);
            venta.setSaldoPendiente(saldoPendiente);
            logger.info("Saldo a favor aplicado a la venta: clienteId={}, ventaId={}, usado={}, restanteCliente={}, saldoPendienteVenta={}",
                    cliente.getId(), venta.getId(), saldoUsado, cliente.getSaldoAFavor(), venta.getSaldoPendiente());
        } else {
            venta.setSaldoPendiente(0.0);
        }
        ventaGuardada = ventaRepositorio.saveAndFlush(venta);
        logger.info("Venta guardada: ventaId={}, tipoDePago={}, total={}, saldoPendiente={}, estado={}",
                ventaGuardada.getId(), ventaGuardada.getTipoDePago(), ventaGuardada.getTotal(),
                ventaGuardada.getSaldoPendiente(), ventaGuardada.getEstado());
        return convertirADTO(ventaGuardada);
    }



    
    // Métodos de conversión
    private VentaDTO convertirADTO(Venta venta) {
        VentaDTO dto = new VentaDTO();
        dto.setId(venta.getId());
        dto.setNumeroComprobante(venta.getNumeroComprobante());
        dto.setFecha(venta.getFecha());
        dto.setClienteId(venta.getCliente().getId());
        dto.setClienteNombre(venta.getCliente().getNombre());
        dto.setTotal(venta.getTotal());
        dto.setObservaciones(venta.getObservaciones());
        dto.setEstado(venta.getEstado());
        dto.setTipoDePago(venta.getTipoDePago());
        dto.setSaldoPendiente(venta.getSaldoPendiente());

        if (venta.getDetalles() != null) {
            List<DetalleVentaDTO> detallesDTO = venta.getDetalles()
                    .stream()
                    .map(this::convertirDetalleADTO)
                    .collect(Collectors.toList());
            dto.setDetalles(detallesDTO);
        }

        return dto;
    }

    private DetalleVentaDTO convertirDetalleADTO(DetalleVenta detalle) {
        DetalleVentaDTO dto = new DetalleVentaDTO();
        dto.setId(detalle.getId());
        dto.setProductoId(detalle.getProducto().getId());
        dto.setProductoNombre(detalle.getProducto().getNombre());
        dto.setCantidad(detalle.getCantidad());
        dto.setPrecioUnitario(detalle.getPrecioUnitario());
        dto.setSubtotal(detalle.getSubtotal());
        return dto;
    }
}
