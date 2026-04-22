package com.sa.contable.servicios;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sa.contable.dto.AsientoDTO;
import com.sa.contable.dto.CuentaAsientoDTO;
import com.sa.contable.dto.DetalleVentaDTO;
import com.sa.contable.dto.VentaDTO;
import com.sa.contable.entidades.Cliente;
import com.sa.contable.entidades.DetalleVenta;
import com.sa.contable.entidades.Producto;
import com.sa.contable.entidades.Venta;
import com.sa.contable.repositorios.ClienteRepository;
import com.sa.contable.repositorios.ProductoRepositorio;
import com.sa.contable.repositorios.VentaRepositorio;

@Service
public class VentaServicio {

    private static Long cuentaDebe = 121L;
    private static Long cuentaHaber = 411L;

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

        // Crear venta
        Venta venta = new Venta();
        venta.setNumeroComprobante("TEMP");
        venta.setFecha(ventaDTO.getFecha() != null ? ventaDTO.getFecha() : LocalDateTime.now());
        venta.setCliente(cliente);
        venta.setObservaciones(ventaDTO.getObservaciones());
        venta.setTotal(0.0);
        Venta ventaGuardada = ventaRepositorio.save(venta);
        String comprobanteReal = "V-" + String.format("%05d", ventaGuardada.getId());
        venta.setNumeroComprobante("VENTA-" + venta.getId());
        ventaGuardada.setNumeroComprobante(comprobanteReal);

        // Procesar detalles
        Double subtotalVenta = 0.0;

        for (DetalleVentaDTO detalleDTO : ventaDTO.getDetalles()) {
            Producto producto = productoRepositorio.findById(detalleDTO.getProductoId())
                .orElseThrow(() -> new RuntimeException(
                    "Producto no encontrado con id: " + detalleDTO.getProductoId()));

            // Validar stock
            if (producto.getStock() < detalleDTO.getCantidad()) {
            throw new RuntimeException(
                "Stock insuficiente para el producto: " + producto.getNombre() +
                ". Disponible: " + producto.getStock() +
                ", Solicitado: " + detalleDTO.getCantidad());
            }

            // ✅ SOLUCIÓN: Definimos el precio seguro para evitar NullPointerException
            Double precioParaCalculo = detalleDTO.getPrecioUnitario() != null 
                ? detalleDTO.getPrecioUnitario() 
                : producto.getPrecio();

            // Crear Asiento Contable
            AsientoDTO asientoDTO = new AsientoDTO();
            asientoDTO.setFecha(venta.getFecha().toLocalDate());
            asientoDTO.setDescripcion("Venta - Comprobante: " + venta.getNumeroComprobante());
            asientoDTO.setNombreUsuario("UsuarioID: " + usuarioId); 
            
            CuentaAsientoDTO movimientoDebe = new CuentaAsientoDTO();
            movimientoDebe.setCuentaCodigo(cuentaDebe);
            movimientoDebe.setDebe(BigDecimal.valueOf(detalleDTO.getCantidad() * precioParaCalculo));
            movimientoDebe.setHaber(BigDecimal.valueOf(0.0));
            
            CuentaAsientoDTO movimientoHaber = new CuentaAsientoDTO();
            movimientoHaber.setCuentaCodigo(cuentaHaber);
            movimientoHaber.setDebe(BigDecimal.valueOf(0.0));
            movimientoHaber.setHaber(BigDecimal.valueOf(detalleDTO.getCantidad() * precioParaCalculo));
            
            asientoDTO.setMovimientos(List.of(movimientoDebe, movimientoHaber));
            asientoServicio.crearAsiento(asientoDTO, usuarioId);
            
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

        venta.setTotal(subtotalVenta);
        ventaGuardada = ventaRepositorio.save(ventaGuardada);
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