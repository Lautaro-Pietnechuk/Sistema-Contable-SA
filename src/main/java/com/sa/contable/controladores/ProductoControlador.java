    package com.sa.contable.controladores;

    import java.util.List;

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

    import com.sa.contable.dto.ProductoDTO;
    import com.sa.contable.servicios.ProductoServicio;

    @RestController
    @RequestMapping("/api/productos")
    @CrossOrigin(origins = "http://localhost:3000")
    public class ProductoControlador {

        @Autowired
        private ProductoServicio productoServicio;

        @GetMapping
        public ResponseEntity<List<ProductoDTO>> obtenerTodos() {
            List<ProductoDTO> productos = productoServicio.obtenerTodos();
            return ResponseEntity.ok(productos);
        }

        @GetMapping("/activos")
        public ResponseEntity<List<ProductoDTO>> obtenerActivos() {
            List<ProductoDTO> productos = productoServicio.obtenerActivos();
            return ResponseEntity.ok(productos);
        }

        @GetMapping("/{id}")
        public ResponseEntity<ProductoDTO> obtenerPorId(@PathVariable Long id) {
            try {
                ProductoDTO producto = productoServicio.obtenerPorId(id);
                return ResponseEntity.ok(producto);
            } catch (RuntimeException e) {
                return ResponseEntity.notFound().build();
            }
        }


        @GetMapping("/buscar")
        public ResponseEntity<List<ProductoDTO>> buscarPorNombre(@RequestParam String nombre) {
            List<ProductoDTO> productos = productoServicio.buscarPorNombre(nombre);
            return ResponseEntity.ok(productos);
        }

        @PostMapping
        public ResponseEntity<?> crearProducto(@RequestBody ProductoDTO productoDTO) {
            try {
                ProductoDTO productoCreado = productoServicio.crearProducto(productoDTO);
                return ResponseEntity.status(HttpStatus.CREATED).body(productoCreado);
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }

        @PutMapping("/{id}")
        public ResponseEntity<?> actualizarProducto(@PathVariable Long id, @RequestBody ProductoDTO productoDTO) {
            try {
                ProductoDTO productoActualizado = productoServicio.actualizarProducto(id, productoDTO);
                return ResponseEntity.ok(productoActualizado);
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<?> eliminarProducto(@PathVariable Long id) {
            try {
                productoServicio.eliminarProducto(id);
                return ResponseEntity.ok("Producto desactivado correctamente");
            } catch (RuntimeException e) {
                return ResponseEntity.notFound().build();
            }
        }

        @DeleteMapping("/definitivo/{id}")
        public ResponseEntity<?> eliminarProductoDefinitivo(@PathVariable Long id) {
            try {
                productoServicio.eliminarProductoDefinitivo(id);
                return ResponseEntity.ok("Producto eliminado definitivamente");
            } catch (RuntimeException e) {
                return ResponseEntity.notFound().build();
            }
        }
    }