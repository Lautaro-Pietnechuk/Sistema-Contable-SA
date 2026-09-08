import React, { useEffect, useState } from 'react';
import axios from 'axios';
import '../css/LibroMayor.css';
import { jsPDF } from 'jspdf';
import autoTable from 'jspdf-autotable';

const LibroMayor = () => {
    const [codigoCuenta, setCodigoCuenta] = useState('');
    const [fechaInicio, setFechaInicio] = useState('');
    const [fechaFin, setFechaFin] = useState('');
    const [libroMayor, setLibroMayor] = useState([]);
    const [cargando, setCargando] = useState(false);
    const [error, setError] = useState('');
    const [saldoFinal, setSaldoFinal] = useState(0);
    const [cuentas, setCuentas] = useState([]);
    const [mensajeError, setMensajeError] = useState('');

    useEffect(() => {
        const hoy = new Date();
        const hace30Dias = new Date();
        const mañana = new Date(hoy);
        hace30Dias.setDate(hoy.getDate() - 30);
        mañana.setDate(hoy.getDate() + 1);

        setFechaInicio(hace30Dias.toISOString().split('T')[0]);
        setFechaFin(mañana.toISOString().split('T')[0]);

        const storedToken = localStorage.getItem('token');
        if (storedToken) {
            cargarCuentas(storedToken);
        }
    }, []);

    const cargarCuentas = async (token) => {
        try {
            const respuesta = await axios.get("http://localhost:8080/api/cuentas/recibeSaldo", {
                headers: { Authorization: `Bearer ${token}` },
            });

            const cuentasFiltradas = respuesta.data.filter((cuenta) => cuenta.recibeSaldo);
            setCuentas(cuentasFiltradas);
        } catch (error) {
            console.error("Error al obtener cuentas:", error);
            setMensajeError("Error al obtener cuentas.");
        }
    };

    const obtenerLibroMayor = async (e) => {
        e.preventDefault();
        const storedToken = localStorage.getItem('token');
    
        if (!storedToken) {
            setError('No se encontró el token. Por favor, inicie sesión.');
            return;
        }
    
        setCargando(true);
        setError('');

        try {
            // 1. Obtener los movimientos del libro mayor
            const responseMovimientos = await axios.get('http://localhost:8080/api/libroMayor', {
                params: {
                    codigoCuenta,
                    fechaInicio,
                    fechaFin,
                },
                headers: { Authorization: `Bearer ${storedToken}` }
            });
            setLibroMayor(responseMovimientos.data);

            // 2. Obtener el saldo de la cuenta desde el nuevo endpoint
            const responseSaldo = await axios.get(`http://localhost:8080/api/cuentas/${codigoCuenta}/saldo`, {
                headers: { Authorization: `Bearer ${storedToken}` }
            });

            const saldo = typeof responseSaldo.data === 'number' 
                ? responseSaldo.data 
                : (responseSaldo.data.saldo ?? 0);

            setSaldoFinal(saldo);
            console.log('Saldo obtenido exitosamente:', saldo);
        } catch (error) {
            console.error('Error al obtener datos del libro mayor o saldo:', error.response?.data || error.message);
            setError(error.response?.data?.mensaje || 'Error al obtener los datos.');
        } finally {
            setCargando(false);
        }
    };

    const generarPDF = () => {
        try {
            const doc = new jsPDF();
            
            const cuenta = cuentas.find(c => c.codigo.toString() === codigoCuenta);
            const nombreCuenta = cuenta ? cuenta.nombre : 'Cuenta Desconocida';
            
            doc.setFont('helvetica', 'bold');
            doc.setFontSize(14);
            doc.text('LIBRO MAYOR', 14, 15);
            
            doc.setFont('helvetica', 'normal');
            doc.setFontSize(10);
            doc.text(`Cuenta: ${nombreCuenta} (Código: ${codigoCuenta})`, 14, 25);
            doc.text(`Período: ${fechaInicio} al ${fechaFin}`, 14, 32);
            doc.text(`Saldo Final: ${saldoFinal.toFixed(2)}`, 14, 39);
            
            const rows = libroMayor.map((movimiento) => [
                new Date(movimiento.fecha).toLocaleDateString(),
                movimiento.descripcion || '',
                movimiento.debe ? movimiento.debe.toFixed(2) : '0.00',
                movimiento.haber ? movimiento.haber.toFixed(2) : '0.00',
                movimiento.saldo ? movimiento.saldo.toFixed(2) : '0.00',
            ]);
            
            autoTable(doc, {
                head: [['Fecha', 'Descripción', 'Debe', 'Haber', 'Saldo']],
                body: rows,
                startY: 48,
                margin: { top: 48 },
                styles: {
                    fontSize: 9,
                    cellPadding: 3,
                },
                headStyles: {
                    fillColor: [41, 128, 185],
                    textColor: 255,
                    fontStyle: 'bold',
                },
                columnStyles: {
                    0: { halign: 'center', cellWidth: 20 },
                    1: { cellWidth: 'auto' },
                    2: { halign: 'right', cellWidth: 25 },
                    3: { halign: 'right', cellWidth: 25 },
                    4: { halign: 'right', cellWidth: 25 },
                },
            });
            
            doc.setFont('helvetica', 'normal');
            doc.setFontSize(8);
            doc.text(`Fecha de generación: ${new Date().toLocaleString()}`, 14, doc.internal.pageSize.getHeight() - 10);
            
            const pdfBuffer = doc.output('arraybuffer');
            const pdfBlob = new Blob([pdfBuffer], { type: 'application/pdf' });
            const url = window.URL.createObjectURL(pdfBlob);
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', `libro_mayor_${codigoCuenta}_${fechaInicio}_${fechaFin}.pdf`);
            document.body.appendChild(link);
            link.click();
            link.remove();
            window.URL.revokeObjectURL(url);
        } catch (error) {
            console.error('Error al generar el PDF:', error);
            alert('Error al generar el PDF: ' + error.message);
        }
    };

    return (
        <div className="libro-mayor">
            <h1>Libro Mayor</h1>

            {cargando && <p>Cargando...</p>}
            {error && <p style={{ color: 'red' }}>{error}</p>}
            {mensajeError && <p style={{ color: 'red' }}>{mensajeError}</p>}

            <form onSubmit={obtenerLibroMayor}>
                <div>
                    <label htmlFor="codigoCuenta">Seleccionar Cuenta:</label>
                    <select
                        id="codigoCuenta"
                        name="codigoCuenta"
                        value={codigoCuenta}
                        onChange={(e) => setCodigoCuenta(e.target.value)}
                        required
                    >
                        <option value="">Seleccione una cuenta</option>
                        {cuentas.map((cuenta) => (
                            <option key={cuenta.codigo} value={cuenta.codigo}>
                                {cuenta.nombre}
                            </option>
                        ))}
                    </select>
                </div>

                <div>
                    <label htmlFor="fechaInicio">Fecha Inicio:</label>
                    <input
                        type="date"
                        id="fechaInicio"
                        name="fechaInicio"
                        value={fechaInicio}
                        onChange={(e) => setFechaInicio(e.target.value)}
                        required
                    />
                </div>
                <div>
                    <label htmlFor="fechaFin">Fecha Fin:</label>
                    <input
                        type="date"
                        id="fechaFin"
                        name="fechaFin"
                        value={fechaFin}
                        onChange={(e) => setFechaFin(e.target.value)}
                        required
                    />
                </div>
                <button type="submit">Obtener Libro Mayor</button>
            </form>

            {libroMayor.length > 0 && (
                <div>
                    <table>
                        <thead>
                            <tr>
                                <th>Fecha</th>
                                <th>Descripción</th>
                                <th>Debe</th>
                                <th>Haber</th>
                                <th>Saldo</th>
                            </tr>
                        </thead>
                        <tbody>
                            {libroMayor.map((movimiento, index) => (
                                <tr key={index}>
                                    <td>{new Date(movimiento.fecha).toLocaleDateString()}</td>
                                    <td>{movimiento.descripcion}</td>
                                    <td>{movimiento.debe?.toFixed(2) || '0.00'}</td>
                                    <td>{movimiento.haber?.toFixed(2) || '0.00'}</td>
                                    <td>{movimiento.saldo?.toFixed(2) || '0.00'}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                    <p><strong>Saldo Final:</strong> {saldoFinal.toFixed(2)}</p>
                    <button onClick={generarPDF}>Generar PDF</button>
                </div>
            )}
        </div>
    );
};

export default LibroMayor;