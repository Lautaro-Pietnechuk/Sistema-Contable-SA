import React, { useEffect, useState } from 'react';
import axios from 'axios';
import './Asientos.css'; // Asegúrate de tener un archivo de estilos para mejorar la presentación

const Asientos = () => {
    const [asientos, setAsientos] = useState([]);
    const [cargando, setCargando] = useState(true);
    const [error, setError] = useState('');
    const [fechaInicio, setFechaInicio] = useState('');
    const [fechaFin, setFechaFin] = useState('');

    // Calcular las fechas de inicio y fin por defecto (hace 30 días y la fecha actual)
    useEffect(() => {
        const hoy = new Date();
        const hace30Dias = new Date();
        hace30Dias.setDate(hoy.getDate() - 30);

        setFechaInicio(hace30Dias.toISOString().split('T')[0]); // Formato YYYY-MM-DD
        setFechaFin(hoy.toISOString().split('T')[0]);
    }, []);

    useEffect(() => {
        const fetchAsientos = async () => {
            try {
                const response = await axios.get('http://localhost:8080/api/asientos/listar', {
                    params: {
                        fechaInicio,
                        fechaFin,
                    }
                });
                if (Array.isArray(response.data)) {
                    setAsientos(response.data);
                } else {
                    console.error('La respuesta no es un array:', response.data);
                    setAsientos([]);
                }
            } catch (error) {
                console.error('Error fetching asientos:', error);
                setError('No se pudieron cargar los asientos. Intente más tarde.');
            } finally {
                setCargando(false);
            }
        };

        fetchAsientos();
    }, [fechaInicio, fechaFin]); // Ejecutar de nuevo cuando cambian las fechas

    const handleFechaInicioChange = (e) => {
        setFechaInicio(e.target.value);
    };

    const handleFechaFinChange = (e) => {
        setFechaFin(e.target.value);
    };

    if (cargando) {
        return <p>Cargando...</p>;
    }

    if (error) {
        return <p>{error}</p>;
    }

    return (
        <div>
            <h2>Libro de Asientos</h2>
            <div>
                <label htmlFor="fechaInicio">Desde:</label>
                <input
                    type="date"
                    id="fechaInicio"
                    value={fechaInicio}
                    onChange={handleFechaInicioChange}
                />
                <label htmlFor="fechaFin">Hasta:</label>
                <input
                    type="date"
                    id="fechaFin"
                    value={fechaFin}
                    onChange={handleFechaFinChange}
                />
            </div>

            {asientos.length === 0 ? (
                <p>No hay asientos disponibles para las fechas seleccionadas.</p>
            ) : (
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
                        {asientos.map(asiento => (
                            <tr key={asiento.id}>
                                <td>{asiento.id}</td>
                                <td>{new Date(asiento.fecha).toLocaleDateString()}</td>
                                <td>{asiento.descripcion}</td>
                                <td>
                                    {Array.isArray(asiento.movimientos) && asiento.movimientos.length > 0 ? (
                                        asiento.movimientos.map((movimiento, index) => (
                                            <div key={index}>
                                                {movimiento.cuenta.nombre}: {movimiento.monto} {movimiento.tipo}
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
            )}
        </div>
    );
};

export default Asientos;
