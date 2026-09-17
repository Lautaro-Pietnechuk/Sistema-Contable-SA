import React, { useState, useEffect } from 'react';
import axios from '../../axiosConfig';
import { jsPDF } from 'jspdf';
import autoTable from 'jspdf-autotable';

const filaInfo = {
    margin: '8px 0',
    padding: '10px 12px',
    backgroundColor: '#f8f9fa',
    borderRadius: '6px',
    border: '1px solid #dee2e6'
};

const MOTIVOS_LABELS = {
    INTERESES: 'Intereses por mora',
    FLETE: 'Gasto de envío / Flete',
    ERROR_FACTURACION: 'Faltante en facturación',
    OTROS: 'Otros cargos'
};

function NotaDebito({ show, handleClose, ventaSeleccionada }) {
    const [motivo, setMotivo] = useState('INTERESES');
    const [monto, setMonto] = useState('');
    const [tipoDePago, setTipoDePago] = useState('CUENTA_CORRIENTE');
    const [observaciones, setObservaciones] = useState('');
    
    const [mensajeExito, setMensajeExito] = useState('');
    const [mensajeError, setMensajeError] = useState('');

    useEffect(() => {
        if (show && ventaSeleccionada) {
            const nroOrigen = ventaSeleccionada.numeroComprobante || ventaSeleccionada.id;
            setMotivo('INTERESES');
            setMonto('');
            setTipoDePago('CUENTA_CORRIENTE');
            setObservaciones(`Ajuste sobre Venta Comprobante N° ${nroOrigen}`);
            setMensajeExito('');
            setMensajeError('');
        }
    }, [show, ventaSeleccionada]);

    const generarPDFNotaDebito = (notaCreada) => {
        try {
            const doc = new jsPDF();
            
            const nroComprobante = notaCreada?.numeroComprobante 
                || notaCreada?.numero 
                || (notaCreada?.id ? `ND-${notaCreada.id}` : 'S/N');

            const comprobanteOrigen = ventaSeleccionada?.numeroComprobante 
                || ventaSeleccionada?.id 
                || '-';

            const fechaEmision = notaCreada?.fecha 
                ? new Date(notaCreada.fecha).toLocaleDateString() 
                : new Date().toLocaleDateString();

            const clienteNombre = notaCreada?.clienteNombre 
                || ventaSeleccionada?.clienteNombre 
                || '-';

            const metodoPago = notaCreada?.tipoDePago || tipoDePago;

            // Encabezado
            doc.setFont('helvetica', 'bold');
            doc.text('NOTA DE DÉBITO', 14, 20);

            // Metadatos
            doc.setFont('helvetica', 'normal');
            doc.setFontSize(10);
            doc.text(`Comprobante: ${nroComprobante}`, 14, 30);
            doc.text(`Fecha: ${fechaEmision}`, 14, 37);
            doc.text(`Cliente: ${clienteNombre}`, 14, 44);
            doc.text(`Comprobante Asociado: ${comprobanteOrigen}`, 14, 51);

            const importeTotal = Number(notaCreada?.monto || monto || 0);
            const conceptoDetalle = MOTIVOS_LABELS[motivo] || motivo;

            const detalles = [
                [
                    conceptoDetalle,
                    metodoPago,
                    `$${importeTotal.toFixed(2)}`
                ]
            ];

            autoTable(doc, {
                head: [['Concepto / Motivo', 'Método de Pago', 'Total']],
                body: detalles,
                startY: 65,
                margin: { left: 14, right: 14 },
                styles: { fontSize: 10 },
                headStyles: { fillColor: [220, 53, 69] }
            });

            const finalY = doc.lastAutoTable.finalY || 100;
            doc.setFont('helvetica', 'bold');
            doc.text(`Total: $${importeTotal.toFixed(2)}`, 14, finalY + 10);

            const notasAdicionales = notaCreada?.observaciones || observaciones;
            if (notasAdicionales) {
                doc.setFont('helvetica', 'normal');
                doc.setFontSize(9);
                doc.text(`Observaciones: ${notasAdicionales}`, 14, finalY + 20);
            }

            doc.save(`Nota_Debito_${nroComprobante}.pdf`);
        } catch (error) {
            console.error('Error al generar el PDF:', error);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        
        if (Number(monto) <= 0) {
            setMensajeError("El monto debe ser mayor a 0.");
            return;
        }

        const storedToken = localStorage.getItem('token');
        const config = storedToken ? {
            headers: { Authorization: `Bearer ${storedToken}` }
        } : {};

        const payload = {
            tipo: 'D',
            idVenta: ventaSeleccionada.id,
            clienteId: ventaSeleccionada.clienteId || ventaSeleccionada.cliente?.id, 
            motivo: motivo,
            monto: Number(monto),
            tipoDePago: tipoDePago,
            observaciones: observaciones
        };

        try {
            const response = await axios.post('http://localhost:8080/api/notas', payload, config);
            const notaCreada = response.data;

            generarPDFNotaDebito(notaCreada);

            setMensajeExito('Nota de Débito generada y comprobante descargado con éxito.');
            setMensajeError('');
            
            setTimeout(() => {
                handleClose();
            }, 2000);

        } catch (error) {
            console.error('Error al generar la Nota de Débito:', error);
            let mensajeAmostrar = 'Error al conectar con el servidor.';
            if (error.response?.data) {
                if (typeof error.response.data === 'string') {
                    mensajeAmostrar = error.response.data;
                } else if (error.response.data.message) {
                    mensajeAmostrar = error.response.data.message;
                }
            }
            setMensajeError(mensajeAmostrar);
        }
    };

    if (!show || !ventaSeleccionada) {
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
                zIndex: 1400 
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
                <h2 style={{ marginTop: 0, borderBottom: '1px solid #dee2e6', paddingBottom: '10px' }}>
                    Emitir Nota de Débito
                </h2>

                <div style={{ marginBottom: '20px' }}>
                    <div style={filaInfo}>
                        <strong>Cliente:</strong> {ventaSeleccionada.clienteNombre || ventaSeleccionada.cliente?.nombre || '-'}
                    </div>
                    <div style={filaInfo}>
                        <strong>Comprobante Original:</strong> {ventaSeleccionada.numeroComprobante || ventaSeleccionada.id}
                    </div>
                    <div style={filaInfo}>
                        <strong>Total Venta Original:</strong> ${Number(ventaSeleccionada.total || 0).toLocaleString('es-AR', { minimumFractionDigits: 2 })}
                    </div>
                </div>

                <form onSubmit={handleSubmit}>
                    <div style={{ display: 'flex', gap: '15px', marginBottom: '15px', flexWrap: 'wrap' }}>
                        <div style={{ flex: '1 1 300px' }}>
                            <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Motivo:</label>
                            <select
                                value={motivo}
                                onChange={(e) => setMotivo(e.target.value)}
                                required
                                style={{ width: '100%', padding: '10px', boxSizing: 'border-box', borderRadius: '6px', border: '1px solid #ccc' }}
                            >
                                <option value="INTERESES">Intereses por mora</option>
                                <option value="FLETE">Gasto de envío / Flete</option>
                                <option value="ERROR_FACTURACION">Faltante en facturación</option>
                                <option value="OTROS">Otros cargos</option>
                            </select>
                        </div>

                        <div style={{ flex: '1 1 300px' }}>
                            <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Monto a Cobrar ($):</label>
                            <input
                                type="number"
                                value={monto}
                                onChange={(e) => setMonto(e.target.value)}
                                min="0.01"
                                step="0.01"
                                required
                                style={{ width: '100%', padding: '10px', boxSizing: 'border-box', borderRadius: '6px', border: '1px solid #ccc' }}
                            />
                        </div>
                    </div>

                    <div style={{ marginBottom: '15px' }}>
                        <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Método de Pago:</label>
                        <select
                            value={tipoDePago}
                            onChange={(e) => setTipoDePago(e.target.value)}
                            required
                            style={{ width: '100%', padding: '10px', boxSizing: 'border-box', borderRadius: '6px', border: '1px solid #ccc' }}
                        >
                            <option value="CUENTA_CORRIENTE">Sumar a Cuenta Corriente</option>
                            <option value="EFECTIVO">Efectivo</option>
                            <option value="DEBITO">Transferencia / Débito</option>
                        </select>
                    </div>

                    <div style={{ marginBottom: '20px' }}>
                        <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Observaciones:</label>
                        <textarea 
                            value={observaciones}
                            onChange={(e) => setObservaciones(e.target.value)}
                            placeholder="Detalle el motivo exacto..."
                            rows="3"
                            required={motivo === 'OTROS'}
                            style={{ width: '100%', padding: '10px', boxSizing: 'border-box', borderRadius: '6px', border: '1px solid #ccc', resize: 'vertical' }}
                        ></textarea>
                    </div>

                    {mensajeError && <div style={{ color: '#842029', backgroundColor: '#f8d7da', padding: '10px', borderRadius: '6px', marginBottom: '15px', border: '1px solid #f5c2c7' }}>{mensajeError}</div>}
                    {mensajeExito && <div style={{ color: '#0f5132', backgroundColor: '#d1e7dd', padding: '10px', borderRadius: '6px', marginBottom: '15px', border: '1px solid #badbcc' }}>{mensajeExito}</div>}

                    <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px', marginTop: '14px' }}>
                        <button 
                            type="button" 
                            onClick={handleClose}
                            style={{ padding: '10px 16px', backgroundColor: '#6c757d', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
                        >
                            Cancelar
                        </button>
                        <button 
                            type="submit"
                            style={{ padding: '10px 16px', backgroundColor: '#dc3545', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}
                        >
                            Generar Débito
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}

export default NotaDebito;