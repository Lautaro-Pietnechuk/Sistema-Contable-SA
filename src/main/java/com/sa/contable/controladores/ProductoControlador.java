    package com.sa.contable.controladores;

    import java.math.BigDecimal;
import java.util.List;

    import com.sa.contable.Configuracion.JwtUtil;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.CrossOrigin;
    import org.springframework.web.bind.annotation.DeleteMapping;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.PathVariable;
    import org.springframework.web.bind.annotation.PostMapping;
    import org.springframework.web.bind.annotation.PutMapping;
    import org.springframework.web.bind.annotation.RequestBody;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RequestParam;
    import org.springframework.web.bind.annotation.RestController;

    import com.sa.contable.DTO.ProductoDTO;
    import com.sa.contable.servicios.ProductoServicio;

import jakarta.servlet.http.HttpServletRequest;

    @RestController
    @RequestMapping("/api/productos")
    @CrossOrigin(origins = "http://localhost:3000")
    public class ProductoControlador {

        @Autowired
        private ProductoServicio productoServicio;

        @Autowired
        private HttpServletRequest request;

        @Autowired
        private JwtUtil jwtUtil;

        private static final Logger logger = LoggerFactory.getLogger(ProductoControlador.class);

        @GetMapping
        public ResponseEntity<List<ProductoDTO>> obtenerTodos() {
            logger.info("Solicitud para obtener todos los productos");
            List<ProductoDTO> productos = productoServicio.obtenerTodos();
            logger.info("Productos obtenidos correctamente: cantidad={}", productos.size());
            return ResponseEntity.ok(productos);
        }

        @GetMapping("/activos")
        public ResponseEntity<List<ProductoDTO>> obtenerActivos() {
            logger.info("Solicitud para obtener productos activos");
            List<ProductoDTO> productos = productoServicio.obtenerActivos();
            logger.info("Productos activos obtenidos correctamente: cantidad={}", productos.size());
            return ResponseEntity.ok(productos);
        }

        @GetMapping("/{id}")
        public ResponseEntity<ProductoDTO> obtenerPorId(@PathVariable Long id) {
            logger.info("Solicitud para obtener producto: productoId={}", id);
            try {
                ProductoDTO producto = productoServicio.obtenerPorId(id);
                logger.info("Producto obtenido correctamente: productoId={}", id);
                return ResponseEntity.ok(producto);
            } catch (RuntimeException e) {
                logger.warn("Producto no encontrado: productoId={}, motivo={}", id, e.getMessage());
                return ResponseEntity.notFound().build();
            }
        }


        @GetMapping("/buscar")
        public ResponseEntity<List<ProductoDTO>> buscarPorNombre(@RequestParam String nombre) {
            logger.info("Solicitud para buscar productos por nombre: nombre={}", nombre);
            List<ProductoDTO> productos = productoServicio.buscarPorNombre(nombre);
            logger.info("Búsqueda de productos finalizada: nombre={}, cantidad={}", nombre, productos.size());
            return ResponseEntity.ok(productos);
        }

        @PostMapping
        public ResponseEntity<?> crearProducto(@RequestBody ProductoDTO productoDTO, @RequestParam BigDecimal costoTotalCompra) {
            logger.info("Solicitud para crear producto: nombre={}, tipoDePago={}, costoTotalCompra={}",
                    productoDTO.getNombre(), productoDTO.getTipoDePago(), costoTotalCompra);
            try {
                Long usuarioId = obtenerUsuarioIdDesdeToken();
                logger.info("Usuario identificado para crear producto: usuarioId={}", usuarioId);
                ProductoDTO productoCreado = productoServicio.crearProducto(productoDTO, usuarioId, costoTotalCompra);
                logger.info("Producto creado correctamente desde el controlador: productoId={}", productoCreado.getId());
                return ResponseEntity.status(HttpStatus.CREATED).body(productoCreado);
            } catch (RuntimeException e) {
                logger.error("Error al crear producto: nombre={}, motivo={}", productoDTO.getNombre(), e.getMessage(), e);
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }

        @PutMapping("/{id}")
        public ResponseEntity<?> actualizarProducto(@PathVariable Long id, @RequestBody ProductoDTO productoDTO, @RequestParam BigDecimal costoTotalCompra) {
            logger.info("Solicitud para actualizar producto: productoId={}, nombre={}, costoTotalCompra={}",
                    id, productoDTO.getNombre(), costoTotalCompra);
            try {
                Long usuarioId = obtenerUsuarioIdDesdeToken();
                ProductoDTO productoActualizado = productoServicio.actualizarProducto(id, productoDTO, costoTotalCompra, usuarioId);
                logger.info("Producto actualizado correctamente: productoId={}", productoActualizado.getId());
                return ResponseEntity.ok(productoActualizado);
            } catch (RuntimeException e) {
                logger.error("Error al actualizar producto: productoId={}, motivo={}", id, e.getMessage(), e);
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }

        @PutMapping("/{id}/estado")
        public ResponseEntity<?> cambiarEstado(@PathVariable Long id) {
            logger.info("Solicitud para cambiar estado del producto: productoId={}", id);
            try {
                ProductoDTO productoActualizado = convertirADTO(productoServicio.cambiarEstado(id));
                logger.info("Estado del producto cambiado correctamente: productoId={}, activo={}",
                        id, productoActualizado.getActivo());
                return ResponseEntity.ok(productoActualizado);
            } catch (RuntimeException e) {
                logger.warn("No se pudo cambiar el estado del producto: productoId={}, motivo={}", id, e.getMessage());
                return ResponseEntity.notFound().build();
            }
        }

        @DeleteMapping("/definitivo/{id}")
        public ResponseEntity<?> eliminarProductoDefinitivo(@PathVariable Long id) {
            logger.info("Solicitud para eliminar definitivamente producto: productoId={}", id);
            try {
                productoServicio.eliminarProductoDefinitivo(id);
                logger.info("Producto eliminado definitivamente: productoId={}", id);
                return ResponseEntity.ok("Producto eliminado definitivamente");
            } catch (RuntimeException e) {
                logger.warn("No se pudo eliminar definitivamente el producto: productoId={}, motivo={}", id, e.getMessage());
                return ResponseEntity.notFound().build();
            }
        }


        private Long obtenerUsuarioIdDesdeToken() {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("Token no enviado o formato inválido");
            }
            String token = authHeader.substring(7);
            String id = jwtUtil.obtenerIdDelToken(token);
            return Long.parseLong(id);
        }

        private ProductoDTO convertirADTO(com.sa.contable.entidades.Producto producto) {
            ProductoDTO dto = new ProductoDTO();
            dto.setId(producto.getId());
            dto.setNombre(producto.getNombre());
            dto.setDescripcion(producto.getDescripcion());
            dto.setPrecio(producto.getPrecio());
            dto.setStock(producto.getStock());
            dto.setActivo(producto.getActivo());
            dto.setCostoPromedio(producto.getCostoPromedio());
            return dto;
        }
    }