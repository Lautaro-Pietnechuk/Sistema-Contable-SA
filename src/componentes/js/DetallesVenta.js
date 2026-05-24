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
            if (!show || !venta?.id) return;

            setError('');
            try {
                const response = await axios.get(`http://localhost:8080/api/ventas/${venta.id}`);
                
                // 🚨 SUPER LOGS EN EL NAVEGADOR (Presioná F12)
                console.group("%c🔎 DEBUEANDO TIPO DE PAGO", "color: #007bff; font-weight: bold; font-size: 14px;");
                console.log("ID de Venta analizada:", venta.id);
                console.log("¿Viene como 'tipoDePago' (CamelCase)?:", response.data.tipoDePago);
                console.log("¿Viene como 'tipo_de_pago' (SnakeCase)?:", response.data.tipo_de_pago);
                console.log("Objeto JSON entero que mandó Java:", response.data);
                console.groupEnd();
                
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
        if (!fechaIso) return '-';
        const fecha = new Date(fechaIso);
        if (Number.isNaN(fecha.getTime())) return '-';
        return fecha.toLocaleString('es-AR');
    };

    const formatTipoPago = (tipo) => {
        if (!tipo) return '❌ VIENE VACÍO O NULO DESDE EL BACKEND';
        const t = tipo.toLowerCase().trim();
        if (t === 'efectivo') return '💸 Efectivo';
        if (t === 'debito') return '💳 Débito';
        if (t === 'cuenta_corriente') return '📓 Cuenta Corriente';
        return `❓ Tipo desconocido: ${tipo}`;
    };

    if (!show || !venta) return null;

    return (
        <div
            style={{
                position: 'fixed',
                top: 0, left: 0, width: '100%', height: '100%',
                backgroundColor: 'rgba(0, 0, 0, 0.5)',
                display: 'flex', justifyContent: 'center', alignItems: 'center',
                zIndex: 1300
            }}
            onClick={handleClose}
        >
            <div
                style={{
                    width: '90%', maxWidth: '760px', backgroundColor: '#fff',
                    borderRadius: '10px', padding: '20px',
                    boxShadow: '0 12px 30px rgba(0, 0, 0, 0.2)',
                    maxHeight: '90vh', overflowY: 'auto'
                }}
                onClick={(event) => event.stopPropagation()}
            >
                {/* MANTIENE AZUL EL TÍTULO */}
                <h2 style={{ marginTop: 0, color: '#4a90e2' }}>Detalles de la Venta</h2>

                {error && <p style={{ color: '#842029' }}>{error}</p>}

                {detalleVenta && (
                    <>
                        <div style={filaInfo}><strong>Comprobante:</strong> {detalleVenta.numeroComprobante || '-'}</div>
                        <div style={filaInfo}><strong>Fecha:</strong> {formatFecha(detalleVenta.fecha)}</div>
                        <div style={filaInfo}><strong>Cliente:</strong> {detalleVenta.clienteNombre || '-'}</div>
                        
                        {/* Muestra el resultado del formateo o el error explícito */}
                        <div style={filaInfo}>
                            <strong>Método de Pago:</strong> {formatTipoPago(detalleVenta.tipoDePago || detalleVenta.tipo_de_pago)}
                        </div>
                        
                        <div style={filaInfo}><strong>Total:</strong> {formatMoneda(detalleVenta.total)}</div>
                        <div style={filaInfo}><strong>Observaciones:</strong> {detalleVenta.observaciones || '-'}</div>

                        <h3 style={{ marginTop: '20px', color: '#333' }}>Items Facturados</h3>
                        <div style={{ overflowX: 'auto' }}>
                            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                                <thead>
                                    {/* MANTIENE AZUL LA CABECERA */}
                                    <tr style={{ backgroundColor: '#4a90e2' }}>
                                        <th style={{ border: '1px solid #ddd', padding: '10px', textAlign: 'left', color: 'white' }}>Producto</th>
                                        <th style={{ border: '1px solid #ddd', padding: '10px', textAlign: 'center', color: 'white' }}>Cantidad</th>
                                        <th style={{ border: '1px solid #ddd', padding: '10px', textAlign: 'right', color: 'white' }}>Precio Unitario</th>
                                        <th style={{ border: '1px solid #ddd', padding: '10px', textAlign: 'right', color: 'white' }}>Subtotal</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {(detalleVenta.detalles || []).map((detalle) => (
                                        <tr key={detalle.id || `${detalle.productoId}-${detalle.productoNombre}`} style={{ borderBottom: '1px solid #ddd' }}>
                                            <td style={{ padding: '10px', border: '1px solid #ddd' }}>{detalle.productoNombre || '-'}</td>
                                            <td style={{ padding: '10px', border: '1px solid #ddd', textAlign: 'center' }}>{detalle.cantidad ?? 0}</td>
                                            <td style={{ padding: '10px', border: '1px solid #ddd', textAlign: 'right' }}>{formatMoneda(detalle.precioUnitario)}</td>
                                            <td style={{ padding: '10px', border: '1px solid #ddd', textAlign: 'right' }}>{formatMoneda(detalle.subtotal)}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    </>
                )}

                <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '25px' }}>
                    {/* MANTIENE AZUL EL BOTÓN */}
                    <button 
                        type="button" 
                        onClick={handleClose}
                        style={{
                            backgroundColor: '#007bff', color: 'white', border: 'none',
                            padding: '10px 24px', borderRadius: '4px', cursor: 'pointer',
                            fontWeight: 'bold', fontSize: '15px'
                        }}
                    >
                        Cerrar Detalles
                    </button>
                </div>
            </div>
        </div>
    );
}

export default DetallesVenta;