import React, { useEffect, useState } from 'react';
import axios from '../../axiosConfig';

const filaInfo = {
    margin: '8px 0',
    padding: '10px 12px',
    backgroundColor: '#f8f9fa',
    borderRadius: '6px',
    border: '1px solid #dee2e6'
};

function DetallesVenta({ show, handleClose, venta }) {
    const [detalleVenta, setDetalleVenta] = useState(null);
    const [error, setError] = useState('');

    useEffect(() => {
        const fetchDetalleVenta = async () => {
            if (!show || !venta?.id) {
                return;
            }

            setError('');
            try {
                const response = await axios.get(`http://localhost:8080/api/ventas/${venta.id}`);
                setDetalleVenta(response.data);
            } catch (fetchError) {
                console.error('Error al cargar los detalles de la venta:', fetchError);
                setError('No se pudieron cargar los detalles de la venta.');
            }
        };

        fetchDetalleVenta();
    }, [show, venta]);

    const formatMoneda = (valor) => {
        const numero = Number(valor || 0);
        return numero.toLocaleString('es-AR', { style: 'currency', currency: 'ARS' });
    };

    const formatFecha = (fechaIso) => {
        if (!fechaIso) {
            return '-';
        }

        const fecha = new Date(fechaIso);
        if (Number.isNaN(fecha.getTime())) {
            return '-';
        }

        return fecha.toLocaleString('es-AR');
    };

    if (!show || !venta) {
        return null;
    }

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
                zIndex: 1300
            }}
            onClick={handleClose}
        >
            <div
                style={{
                    width: '90%',
                    maxWidth: '760px',
                    backgroundColor: '#fff',
                    borderRadius: '10px',
                    padding: '20px',
                    boxShadow: '0 12px 30px rgba(0, 0, 0, 0.2)',
                    maxHeight: '90vh',
                    overflowY: 'auto'
                }}
                onClick={(event) => event.stopPropagation()}
            >
                <h2 style={{ marginTop: 0 }}>Detalles de la Venta</h2>

                {error && <p style={{ color: '#842029' }}>{error}</p>}

                {detalleVenta && (
                    <>
                        <div style={filaInfo}><strong>Comprobante:</strong> {detalleVenta.numeroComprobante || '-'}</div>
                        <div style={filaInfo}><strong>Fecha:</strong> {formatFecha(detalleVenta.fecha)}</div>
                        <div style={filaInfo}><strong>Cliente:</strong> {detalleVenta.clienteNombre || '-'}</div>
                        <div style={filaInfo}><strong>Total:</strong> {formatMoneda(detalleVenta.total)}</div>
                        <div style={filaInfo}><strong>Observaciones:</strong> {detalleVenta.observaciones || '-'}</div>

                        <h3 style={{ marginTop: '16px' }}>Items</h3>
                        <div style={{ overflowX: 'auto' }}>
                            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                                <thead>
                                    <tr>
                                        <th style={{ border: '1px solid #343a40', padding: '8px' }}>Producto</th>
                                        <th style={{ border: '1px solid #343a40', padding: '8px' }}>Cantidad</th>
                                        <th style={{ border: '1px solid #343a40', padding: '8px' }}>Precio Unitario</th>
                                        <th style={{ border: '1px solid #343a40', padding: '8px' }}>Subtotal</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {(detalleVenta.detalles || []).map((detalle) => (
                                        <tr key={detalle.id || `${detalle.productoId}-${detalle.productoNombre}`}>
                                            <td style={{ border: '1px solid #343a40', padding: '8px' }}>{detalle.productoNombre || '-'}</td>
                                            <td style={{ border: '1px solid #343a40', padding: '8px' }}>{detalle.cantidad ?? 0}</td>
                                            <td style={{ border: '1px solid #343a40', padding: '8px' }}>{formatMoneda(detalle.precioUnitario)}</td>
                                            <td style={{ border: '1px solid #343a40', padding: '8px' }}>{formatMoneda(detalle.subtotal)}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    </>
                )}

                <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '14px' }}>
                    <button type="button" onClick={handleClose}>Cerrar</button>
                </div>
            </div>
        </div>
    );
}

export default DetallesVenta;
