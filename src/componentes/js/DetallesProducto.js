import React, { useEffect, useState } from 'react';
import axios from '../../axiosConfig';

const campo = {
  margin: '10px 0',
  padding: '10px 12px',
  backgroundColor: '#f8f9fa',
  borderRadius: '6px',
  border: '1px solid #dee2e6'
};

function DetallesProducto({ show, handleClose, producto }) {
  const [ventas, setVentas] = useState([]);
  const [cargandoVentas, setCargandoVentas] = useState(false);
  const [errorVentas, setErrorVentas] = useState('');

  useEffect(() => {
    const cargarVentas = async () => {
      if (!show || !producto?.id) {
        return;
      }

      setCargandoVentas(true);
      setErrorVentas('');

      try {
        const response = await axios.get('http://localhost:8080/api/ventas');
        const ventasData = Array.isArray(response.data) ? response.data : [];
        const ventasActivas = ventasData.filter((venta) => {
          const estado = String(venta.estado || '').trim().toUpperCase();
          const contieneProducto = (venta.detalles || []).some(
            (detalle) => Number(detalle.productoId) === Number(producto.id)
          );

          return estado !== 'ANULADA' && contieneProducto;
        });

        setVentas(ventasActivas);
      } catch (error) {
        console.error('Error al cargar las ventas del producto:', error);
        setErrorVentas('No se pudieron cargar las ventas del producto.');
        setVentas([]);
      } finally {
        setCargandoVentas(false);
      }
    };

    cargarVentas();
  }, [show, producto]);

  if (!show || !producto) {
    return null;
  }

  const formatMoneda = (valor) => {
    const numero = Number(valor || 0);
    return numero.toLocaleString('es-AR', { style: 'currency', currency: 'ARS' });
  };

  const formatFecha = (fechaIso) => {
    if (!fechaIso) return '-';
    const fecha = new Date(fechaIso);
    return Number.isNaN(fecha.getTime()) ? '-' : fecha.toLocaleDateString('es-AR');
  };

  return (
    <div
      style={{
        position: 'fixed',
        top: 0,
        left: 0,
        width: '100%',
        height: '100%',
        backgroundColor: 'rgba(0, 0, 0, 0.5)',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        zIndex: 1200
      }}
      onClick={handleClose}
    >
      <div
        style={{
          width: '90%',
          maxWidth: '560px',
          backgroundColor: '#fff',
          borderRadius: '10px',
          padding: '20px',
          boxShadow: '0 12px 30px rgba(0, 0, 0, 0.2)'
        }}
        onClick={(e) => e.stopPropagation()}
      >
        <h2 style={{ marginTop: 0 }}>Detalles del Producto</h2>

        <div style={campo}><strong>ID:</strong> {producto.id ?? '-'}</div>
        <div style={campo}><strong>Nombre:</strong> {producto.nombre || '-'}</div>
        <div style={campo}><strong>Descripcion:</strong> {producto.descripcion || '-'}</div>
        <div style={campo}><strong>Precio:</strong> {formatMoneda(producto.precio)}</div>
        <div style={campo}><strong>Stock:</strong> {producto.stock ?? 0}</div>
        <div style={campo}><strong>Estado:</strong> {producto.activo ? 'Activo' : 'Inactivo'}</div>

        <h3 style={{ marginTop: '22px' }}>Ventas activas</h3>
        {cargandoVentas && <p>Cargando ventas...</p>}
        {errorVentas && <p style={{ color: '#842029' }}>{errorVentas}</p>}
        {!cargandoVentas && !errorVentas && ventas.length === 0 && (
          <p>No hay ventas activas con este producto.</p>
        )}
        {!cargandoVentas && ventas.length > 0 && (
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', minWidth: '650px' }}>
              <thead>
                <tr style={{ backgroundColor: '#4a90e2', color: '#fff' }}>
                  <th style={{ border: '1px solid #ddd', padding: '8px' }}>Comprobante</th>
                  <th style={{ border: '1px solid #ddd', padding: '8px' }}>Fecha</th>
                  <th style={{ border: '1px solid #ddd', padding: '8px' }}>Cliente</th>
                  <th style={{ border: '1px solid #ddd', padding: '8px' }}>Cantidad</th>
                  <th style={{ border: '1px solid #ddd', padding: '8px' }}>Precio unitario</th>
                  <th style={{ border: '1px solid #ddd', padding: '8px' }}>Subtotal</th>
                  <th style={{ border: '1px solid #ddd', padding: '8px' }}>Estado</th>
                </tr>
              </thead>
              <tbody>
                {ventas.map((venta) => {
                  const detallesProducto = (venta.detalles || []).filter(
                    (detalle) => Number(detalle.productoId) === Number(producto.id)
                  );

                  return detallesProducto.map((detalle) => (
                    <tr key={`${venta.id}-${detalle.id || producto.id}`}>
                      <td style={{ border: '1px solid #ddd', padding: '8px' }}>{venta.numeroComprobante || '-'}</td>
                      <td style={{ border: '1px solid #ddd', padding: '8px' }}>{formatFecha(venta.fecha)}</td>
                      <td style={{ border: '1px solid #ddd', padding: '8px' }}>{venta.clienteNombre || '-'}</td>
                      <td style={{ border: '1px solid #ddd', padding: '8px', textAlign: 'center' }}>{detalle.cantidad ?? 0}</td>
                      <td style={{ border: '1px solid #ddd', padding: '8px', textAlign: 'right' }}>{formatMoneda(detalle.precioUnitario)}</td>
                      <td style={{ border: '1px solid #ddd', padding: '8px', textAlign: 'right' }}>{formatMoneda(detalle.subtotal)}</td>
                      <td style={{ border: '1px solid #ddd', padding: '8px' }}>{venta.estado || '-'}</td>
                    </tr>
                  ));
                })}
              </tbody>
            </table>
          </div>
        )}

        <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '15px' }}>
          <button type="button" onClick={handleClose}>Cerrar</button>
        </div>
      </div>
    </div>
  );
}

export default DetallesProducto;