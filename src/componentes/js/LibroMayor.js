import React, { useEffect, useState } from 'react';
import axios from 'axios';
import '../css/LibroMayor.css';
import { jsPDF } from 'jspdf';
import 'jspdf-autotable';

const LibroMayor = () => {
    const [codigoCuenta, setCodigoCuenta] = useState('');
    const [fechaInicio, setFechaInicio] = useState('');
    const [fechaFin, setFechaFin] = useState('');
    const [libroMayor, setLibroMayor] = useState([]);
    const [cargando, setCargando] = useState(false);
    const [error, setError] = useState('');
    const [saldoFinal, setSaldoFinal] = useState(0);
    const [cuentas, setCuentas] = useState([]); // Estado para las cuentas
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
            cargarCuentas(storedToken); // Cargar cuentas cuando se cargue el componente
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
        try {
            const response = await axios.get('http://localhost:8080/api/libroMayor', {
                params: {
                    codigoCuenta,
                    fechaInicio,
                    fechaFin,
                },
                headers: { Authorization: `Bearer ${storedToken}` }
            });
            setLibroMayor(response.data);
    
            let saldoAcumulado = 0;
            response.data.forEach((movimiento) => {
                const { debe, haber, tipoCuenta } = movimiento;
                if (tipoCuenta) {
                    let nuevoSaldo = saldoAcumulado;
                    switch (tipoCuenta.toLowerCase()) {
                        case "activo":
                        case "egreso":
                            nuevoSaldo = saldoAcumulado + (debe || 0) - (haber || 0);
                            break;
                        case "pasivo":
                        case "patrimonio":
                        case "ingreso":
                            nuevoSaldo = saldoAcumulado - (debe || 0) + (haber || 0);
                            break;
                        default:
                            console.error('Tipo de cuenta desconocido:', tipoCuenta);
                    }
                    saldoAcumulado = nuevoSaldo;
                } else {
                    console.error('tipoCuenta es undefined para el movimiento:', movimiento);
                }
                movimiento.saldo = saldoAcumulado;
            });
    
            setSaldoFinal(saldoAcumulado);
            setError('');
        } catch (error) {
            console.error('Error al obtener el libro mayor:', error.response ? error.response.data : error.message);
            setError(error.response ? error.response.data.mensaje : 'Error al obtener el libro mayor.');
        } finally {
            setCargando(false);
        }
    };

    const generarPDF = () => {
        const doc = new jsPDF();
        
        const rows = libroMayor.map((movimiento) => [
            new Date(movimiento.fecha).toLocaleDateString(),
            movimiento.descripcion,
            movimiento.debe?.toFixed(2) || '0.00',
            movimiento.haber?.toFixed(2) || '0.00',
            movimiento.saldo?.toFixed(2) || '0.00',
        ]);

        doc.autoTable({
            head: [['Fecha', 'Descripción', 'Debe', 'Haber', 'Saldo']],
            body: rows,
        });

        doc.text(`Saldo Final: ${saldoFinal.toFixed(2)}`, 14, doc.lastAutoTable.finalY + 10);
        doc.save('libro_mayor.pdf');
    };

    return (
        <div className="libro-mayor">
            <h1>Libro Mayor</h1>

            {/* Mostrar mensaje de carga si está cargando */}
            {cargando && <p>Cargando...</p>}

            {/* Mostrar mensaje de error si hay error */}
            {error && <p style={{ color: 'red' }}>{error}</p>}

            {/* Mostrar mensaje de error si hay error al obtener las cuentas */}
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
