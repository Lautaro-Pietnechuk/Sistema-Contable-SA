import React, { useState, useEffect } from 'react';
import axios from '../../axiosConfig';
import { jsPDF } from 'jspdf'; // IMPORTANTE: Agregamos la importación
import autoTable from 'jspdf-autotable'; // IMPORTANTE: Agregamos la importación

// Reutilizamos el estilo de fila para mantener la coherencia visual
const filaInfo = {
    margin: '8px 0',
    padding: '10px 12px',
    backgroundColor: '#f8f9fa',
    borderRadius: '6px',
    border: '1px solid #dee2e6'
};

function NotaDebito({ show, handleClose, ventaSeleccionada }) {
    const [motivo, setMotivo] = useState('INTERESES');
    const [monto, setMonto] = useState('');
    const [tipoDePago, setTipoDePago] = useState('Cuenta Corriente');
    const [observaciones, setObservaciones] = useState('');
    
    const [mensajeExito, setMensajeExito] = useState('');
    const [mensajeError, setMensajeError] = useState('');

    useEffect(() => {
        if (show && ventaSeleccionada) {
            setMotivo('INTERESES');
            setMonto('');
            setTipoDePago('Cuenta Corriente');
            setObservaciones(`Ref: Ajuste sobre Venta Comprobante N° ${ventaSeleccionada.numeroComprobante || ventaSeleccionada.id}`);
            setMensajeExito('');
            setMensajeError('');
        }
    }, [show, ventaSeleccionada]);

    // Función para crear el PDF. Recibe los datos que devuelve tu backend.
    const generarPDFNotaDebito = (notaCreada) => {
        try {
            const doc = new jsPDF();
            
            // Título
            doc.setFont('helvetica', 'bold');
            doc.text('NOTA DE DÉBITO', 14, 20);

            // Cabecera
            doc.setFont('helvetica', 'normal');
            doc.setFontSize(10);
            
            // Si el backend te devuelve un número autogenerado lo usamos, si no, usamos el ID
            const numeroIdentificador = notaCreada?.numeroComprobante || notaCreada?.id || 'S/N';
            // Usamos la fecha que mandó el backend, o la fecha actual por defecto
            const fechaAlta = notaCreada?.fecha ? new Date(notaCreada.fecha).toLocaleDateString('es-AR') : new Date().toLocaleDateString('es-AR');

            doc.text(`Comprobante: ND-${numeroIdentificador}`, 14, 30);
            doc.text(`Fecha: ${fechaAlta}`, 14, 37);
            doc.text(`Cliente: ${ventaSeleccionada.clienteNombre || '-'}`, 14, 44);
            doc.text(`Comprobante Original: ${ventaSeleccionada.numeroComprobante || ventaSeleccionada.id}`, 14, 51);
            doc.text(`Método de Pago: ${tipoDePago}`, 14, 58); 

            // Como es una nota de débito, armamos una sola fila con el motivo y el monto total
            const detalles = [
                [
                    motivo, // Producto/Concepto
                    1,      // Cantidad
                    `$${Number(monto).toFixed(2)}`, // Precio Unit.
                    `$${Number(monto).toFixed(2)}`  // Subtotal
                ]
            ];

            // Tabla (con color rojizo para diferenciar de las facturas)
            autoTable(doc, {
                head: [['Concepto / Motivo', 'Cantidad', 'Importe', 'Subtotal']],
                body: detalles,
                startY: 65,
                margin: { left: 14, right: 14 },
                styles: { fontSize: 10 },
                headStyles: { fillColor: [220, 53, 69] } // Un color rojizo/carmesí
            });

            // Totales
            const finalY = doc.lastAutoTable.finalY || 100;
            doc.setFont('helvetica', 'bold');
            doc.text(`Total a Pagar: $${Number(monto).toFixed(2)}`, 14, finalY + 10);

            if (observaciones) {
                doc.setFont('helvetica', 'normal');
                doc.setFontSize(9);
                doc.text(`Observaciones: ${observaciones}`, 14, finalY + 20);
            }

            // Descarga automática
            doc.save(`Nota_Debito_${numeroIdentificador}.pdf`);
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

        const payload = {
            tipo: 'D',
            idVenta: ventaSeleccionada.id,
            clienteId: ventaSeleccionada.clienteId, 
            motivo: motivo,
            monto: Number(monto),
            tipoDePago: tipoDePago,
            observaciones: observaciones
        };

        try {
            // Guardamos la respuesta del backend en una variable
            const response = await axios.post('/api/notas', payload);

            // ¡Acá disparamos el PDF enviando lo que nos devolvió Java!
            generarPDFNotaDebito(response.data);

            setMensajeExito('Nota de Débito generada con éxito.');
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
                        <strong>Cliente:</strong> {ventaSeleccionada.clienteNombre}
                    </div>
                    <div style={filaInfo}>
                        <strong>Venta Original:</strong> {ventaSeleccionada.numeroComprobante || ventaSeleccionada.id}
                    </div>
                    <div style={filaInfo}>
                        <strong>Total Venta:</strong> ${Number(ventaSeleccionada.total).toLocaleString('es-AR', { minimumFractionDigits: 2 })}
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
                            <option value="Cuenta Corriente">Sumar a Cuenta Corriente</option>
                            <option value="EFECTIVO">Efectivo</option>
                            <option value="TRANSFERENCIA">Transferencia</option>
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
                        <button type="button" onClick={handleClose}>
                            Cancelar
                        </button>
                        <button type="submit">
                            Generar Débito
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}

export default NotaDebito;