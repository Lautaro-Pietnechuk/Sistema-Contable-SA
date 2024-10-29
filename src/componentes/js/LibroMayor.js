import React, { useEffect, useState, useCallback } from 'react';
import axios from 'axios';

const LibroMayor = ({ cuentaCodigo }) => {
    const [asientos, setAsientos] = useState([]);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(true);
    const [fechaInicio, setFechaInicio] = useState(''); // Inicializa la fecha de inicio
    const [fechaFin, setFechaFin] = useState(''); // Inicializa la fecha de fin

    const obtenerLibroMayor = useCallback(async () => {
        setLoading(true);
        try {
            const token = localStorage.getItem('token');
            const respuesta = await axios.get(`http://localhost:8080/api/libro-mayor`, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
                params: {
                    cuentaCodigo, // Código de cuenta a filtrar
                    fechaInicio: fechaInicio || undefined, // Manejo de fechas
                    fechaFin: fechaFin || undefined,
                },
            });
            console.log('Parámetros enviados:', { cuentaCodigo, fechaInicio, fechaFin });

            console.log('Respuesta de la API completa:', respuesta.data); // Muestra toda la respuesta
            console.log('Respuesta de la API:', respuesta.data);
            setAsientos(respuesta.data.asientos || []); // Asumimos que la respuesta tiene esta estructura
            setError('');
        } catch (error) {
            console.error('Error obteniendo libro mayor:', error);
            setError(error.response?.data?.mensaje || 'Error al obtener el libro mayor.');
        } finally {
            setLoading(false);
        }
    }, [cuentaCodigo, fechaInicio, fechaFin]);

    useEffect(() => {
        obtenerLibroMayor();
    }, [obtenerLibroMayor]);

    return (
        <div>
            <h2>Libro Mayor</h2>

            {/* Inputs para seleccionar fechas */}
            <div>
                <label>
                    Fecha de Inicio:
                    <input
                        type="date"
                        value={fechaInicio}
                        onChange={(e) => setFechaInicio(e.target.value)} // Actualiza el estado al cambiar
                    />
                </label>
                <label>
                    Fecha de Fin:
                    <input
                        type="date"
                        value={fechaFin}
                        onChange={(e) => setFechaFin(e.target.value)} // Actualiza el estado al cambiar
                    />
                </label>
            </div>

            {loading ? (
                <p>Cargando...</p>
            ) : (
                <>
                    {error && <p style={{ color: 'red' }} aria-live="assertive">{error}</p>}
                    <table>
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Fecha</th>
                                <th>Descripción</th>
                                <th>Movimientos</th>
                            </tr>
                        </thead>
                        <tbody>
                            {asientos.length > 0 ? (
                                asientos.map((asiento) => (
                                    <tr key={asiento.id}>
                                        <td>{asiento.id}</td>
                                        <td>{asiento.fecha}</td>
                                        <td>{asiento.descripcion}</td>
                                        <td>
                                            <ul>
                                                {asiento.movimientos.map((movimiento) => (
                                                    <li key={movimiento.cuentaCodigo}>
                                                        {movimiento.cuentaNombre} - Debe: {movimiento.debe} - Haber: {movimiento.haber}
                                                    </li>
                                                ))}
                                            </ul>
                                        </td>
                                    </tr>
                                ))
                            ) : (
                                <tr>
                                    <td colSpan="4">No hay asientos disponibles</td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </>
            )}
        </div>
    );
};

export default LibroMayor;
