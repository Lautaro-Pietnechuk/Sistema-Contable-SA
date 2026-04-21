import React from 'react';

const campo = {
  margin: '10px 0',
  padding: '10px 12px',
  backgroundColor: '#f8f9fa',
  borderRadius: '6px',
  border: '1px solid #dee2e6'
};

function DetallesProducto({ show, handleClose, producto }) {
  if (!show || !producto) {
    return null;
  }

  const formatMoneda = (valor) => {
    const numero = Number(valor || 0);
    return numero.toLocaleString('es-AR', { style: 'currency', currency: 'ARS' });
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

        <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '15px' }}>
          <button type="button" onClick={handleClose}>Cerrar</button>
        </div>
      </div>
    </div>
  );
}

export default DetallesProducto;