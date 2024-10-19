import React, { useEffect, useState } from 'react';
import axios from 'axios';
import './Asientos.css'; // Asegúrate de tener un archivo de estilos

const Asientos = () => {
    const [asientos, setAsientos] = useState([]);
    const [cargando, setCargando] = useState(true);
    const [error, setError] = useState('');
    const [fechaInicio, setFechaInicio] = useState('');
    const [fechaFin, setFechaFin] = useState('');

    // Calcular fechas por defecto (30 días atrás y hoy)
    useEffect(() => {
        const hoy = new Date();
        const hace30Dias = new Date();
        hace30Dias.setDate(hoy.getDate() - 30);

        setFechaInicio(hace30Dias.toISOString().split('T')[0]); // YYYY-MM-DD
        setFechaFin(hoy.toISOString().split('T')[0]);
    }, []);

    useEffect(() => {
        const fetchAsientos = async () => {
            const storedToken = localStorage.getItem('token');
            console.log('Token almacenado:', storedToken);
            if (!storedToken) {
                setError('No se encontró el token. Por favor, inicie sesión.');
                setCargando(false);
                return;
            }

            try {
                const response = await axios.get('http://localhost:8080/api/asientos/listar', {
                    params: { fechaInicio, fechaFin },
                    headers: { Authorization: `Bearer ${storedToken}` }
                });

                if (Array.isArray(response.data)) {
                    setAsientos(response.data);
                } else {
                    console.error('La respuesta no es un array:', response.data);
                    setAsientos([]);
                    setError('No se encontraron asientos.');
                }
            } catch (error) {
                console.error('Error al obtener asientos:', error);
                setError(error.response ? error.response.data.message : 'Error al cargar los asientos.');
            } finally {
                setCargando(false);
            }
        };

        fetchAsientos();
    }, [fechaInicio, fechaFin]);

    const handleFechaInicioChange = (e) => {
        const nuevaFechaInicio = e.target.value;
        setFechaInicio(nuevaFechaInicio);

        if (nuevaFechaInicio > fechaFin) {
            setFechaFin(nuevaFechaInicio);
        }
    };

    const handleFechaFinChange = (e) => {
        const nuevaFechaFin = e.target.value;
        if (nuevaFechaFin < fechaInicio) {
            setError('La fecha de fin no puede ser anterior a la fecha de inicio.');
        } else {
            setError('');
            setFechaFin(nuevaFechaFin);
        }
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
                                    {asiento.movimientos && asiento.movimientos.length > 0 ? (
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
