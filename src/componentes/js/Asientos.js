import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { jsPDF } from 'jspdf';
import 'jspdf-autotable';

import '../css/Asientos.css';

const Asientos = () => {
    const [asientos, setAsientos] = useState([]);
    const [cargando, setCargando] = useState(true);
    const [error, setError] = useState('');
    const [fechaInicio, setFechaInicio] = useState('');
    const [fechaFin, setFechaFin] = useState('');
    const [paginaActual, setPaginaActual] = useState(1);
    const [totalPaginas, setTotalPaginas] = useState(0);
    const tamañoPorPagina = 5;

    // Set fechas por defecto cuando el componente se monta
    useEffect(() => {
        const hoy = new Date();
        const hace30Dias = new Date();
        hace30Dias.setDate(hoy.getDate() - 30);
        setFechaInicio(hace30Dias.toISOString().split('T')[0]); // Hace 30 días

        hoy.setDate(hoy.getDate() + 1); // Mañana
        setFechaFin(hoy.toISOString().split('T')[0]);
    }, []);

    // Llama a la API cada vez que cambian las fechas o la página
    useEffect(() => {
        const fetchAsientos = async () => {
            const storedToken = localStorage.getItem('token');
            if (!storedToken) {
                setError('No se encontró el token. Por favor, inicie sesión.');
                setCargando(false);
                return;
            }

            try {
                setCargando(true); // Inicia el loading mientras se hace la consulta
                setError(''); // Reseteamos el error cuando empezamos a cargar los datos

                const response = await axios.get('http://localhost:8080/api/asientos/listar', {
                    params: {
                        fechaInicio,
                        fechaFin,
                        page: paginaActual - 1,
                        size: tamañoPorPagina,
                    },
                    headers: { Authorization: `Bearer ${storedToken}` },
                });

                const { asientos: asientosData, totalElementos } = response.data;
                if (asientosData && asientosData.length > 0) {
                    setAsientos(asientosData);
                    setTotalPaginas(Math.ceil(totalElementos / tamañoPorPagina));
                } else {
                    setAsientos([]); // Asegura que el estado 'asientos' se vacíe si no hay datos
                    setError('No se encontraron asientos.');
                }
            } catch (error) {
                console.error('Error al cargar los asientos:', error);
                setError(error.response?.data?.mensaje || 'Error al cargar los asientos.');
            } finally {
                setCargando(false); // Detiene el loading
            }
        };

        if (fechaInicio && fechaFin) fetchAsientos();
    }, [fechaInicio, fechaFin, paginaActual]); // Dependencias para actualizar cada vez que cambien

    // Manejo de los cambios en las fechas
    const handleFechaInicioChange = (e) => {
        setFechaInicio(e.target.value);
        setPaginaActual(1); // Resetear la página a la primera cuando cambian las fechas
    };

    const handleFechaFinChange = (e) => {
        setFechaFin(e.target.value);
        setPaginaActual(1); // Resetear la página a la primera cuando cambian las fechas
    };

    const cambiarPagina = (nuevaPagina) => {
        if (nuevaPagina >= 1 && nuevaPagina <= totalPaginas) {
            setPaginaActual(nuevaPagina);
        }
    };

    const generarPDF = () => {
        const doc = new jsPDF();

        doc.setFont('helvetica', 'normal');
        doc.text('Libro Diario', 14, 20);
        doc.text(`Fecha: ${fechaInicio} - ${fechaFin}`, 14, 30);

        let y = 40; // Y-Position para las filas
        doc.autoTable({
            head: [['ID', 'Fecha', 'Descripción', 'Usuario', 'Movimientos']],
            body: asientos.map((asiento) => [
                asiento.id,
                new Date(asiento.fecha).toLocaleDateString(),
                asiento.descripcion,
                asiento.nombreUsuario || 'Usuario desconocido',
                asiento.movimientos.length > 0
                    ? asiento.movimientos.map((movimiento) =>
                          `${movimiento.cuentaNombre}: ${movimiento.debe} (Debe), ${movimiento.haber} (Haber)`
                      ).join(', ')
                    : 'No hay movimientos',
            ]),
            startY: y,
            margin: { top: 40 },
            styles: { fontSize: 8 },
        });

        doc.save('asientos.pdf');
    };

    return (
        <div className="contenedor-asientos">
            <h2>Libro Diario</h2>
            <div>
                <label htmlFor="fechaInicio">Desde:</label>
                <input type="date" id="fechaInicio" value={fechaInicio} onChange={handleFechaInicioChange} />
                <label htmlFor="fechaFin">Hasta:</label>
                <input type="date" id="fechaFin" value={fechaFin} onChange={handleFechaFinChange} />
            </div>

            {/* Muestra el error, pero el resto del contenido sigue visible */}
            {error && (
                <div style={{ color: 'red', marginBottom: '20px' }}>
                    <p>{error}</p>
                </div>
            )}

            {cargando ? (
                <p>Cargando...</p>
            ) : (
                <>
                    {asientos.length === 0 ? (
                        <p>No hay asientos disponibles para las fechas seleccionadas.</p>
                    ) : (
                        <div>
                            <table>
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Fecha</th>
                                        <th>Descripción</th>
                                        <th>Usuario</th>
                                        <th>Movimientos</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {asientos.map((asiento) => (
                                        <tr key={asiento.id}>
                                            <td>{asiento.id}</td>
                                            <td>{new Date(asiento.fecha).toLocaleDateString()}</td>
                                            <td>{asiento.descripcion}</td>
                                            <td>{asiento.nombreUsuario || 'Usuario desconocido'}</td>
                                            <td>
                                                {asiento.movimientos.length > 0 ? (
                                                    asiento.movimientos.map((movimiento, index) => (
                                                        <div key={index}>
                                                            {movimiento.cuentaNombre ? (
                                                                <span>
                                                                    {movimiento.cuentaNombre}: {movimiento.debe} (Debe), {movimiento.haber} (Haber)
                                                                </span>
                                                            ) : (
                                                                <span>Cuenta no definida</span>
                                                            )}
                                                        </div>
                                                    ))
                                                ) : (
                                                    <div>No hay movimientos disponibles.</div>
                                                )}
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                            <button onClick={generarPDF} style={{ marginTop: '20px' }}>
                                Descargar PDF
                            </button>
                        </div>
                    )}
                </>
            )}

            <div className="paginacion">
                <button onClick={() => cambiarPagina(paginaActual - 1)} disabled={paginaActual === 1}>
                    Anterior
                </button>
                <span>Página {paginaActual} de {totalPaginas}</span>
                <button onClick={() => cambiarPagina(paginaActual + 1)} disabled={paginaActual >= totalPaginas}>
                    Siguiente
                </button>
            </div>
        </div>
    );
};

export default Asientos;
