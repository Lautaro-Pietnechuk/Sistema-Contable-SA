import React, { useEffect, useState } from 'react';
import axios from 'axios';
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

    useEffect(() => {
        const hoy = new Date();
        const hace30Dias = new Date();
        hace30Dias.setDate(hoy.getDate() - 30);
        setFechaInicio(hace30Dias.toISOString().split('T')[0]);
        setFechaFin(hoy.toISOString().split('T')[0]);
    }, []);

    useEffect(() => {
        const fetchAsientos = async () => {
            const storedToken = localStorage.getItem('token');
            if (!storedToken) {
                setError('No se encontró el token. Por favor, inicie sesión.');
                setCargando(false);
                return;
            }
            try {
                console.log('Fechas de búsqueda:', { fechaInicio, fechaFin, paginaActual });
                const response = await axios.get('http://localhost:8080/api/asientos/listar', {
                    params: {
                        fechaInicio,
                        fechaFin,
                        page: paginaActual - 1,
                        size: tamañoPorPagina,
                    },
                    headers: { Authorization: `Bearer ${storedToken}` }
                });

                console.log('Respuesta completa de la API:', response.data);

                const asientosData = response.data.asientos; // Accediendo a la propiedad 'asientos'
                const totalElementos = response.data.totalElementos; // Obteniendo el total de elementos

                if (asientosData && asientosData.length > 0) {
                    console.log('Datos de asientos recibidos:', asientosData);
                    setAsientos(asientosData);
                    setTotalPaginas(Math.ceil(totalElementos / tamañoPorPagina)); // Calculando el total de páginas
                } else {
                    console.log('No se encontraron asientos en la respuesta.');
                    setAsientos([]);
                    setError('No se encontraron asientos.');
                }
            } catch (error) {
                console.error('Error al cargar los asientos:', error);
                setError(error.response ? error.response.data.mensaje : 'Error al cargar los asientos.');
            } finally {
                setCargando(false);
            }
        };
        fetchAsientos();
    }, [fechaInicio, fechaFin, paginaActual]);

    const handleFechaInicioChange = (e) => {
        const nuevaFechaInicio = e.target.value;
        setFechaInicio(nuevaFechaInicio);
        if (nuevaFechaInicio > fechaFin) setFechaFin(nuevaFechaInicio);
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

    const cambiarPagina = (nuevaPagina) => {
        if (nuevaPagina >= 1 && nuevaPagina <= totalPaginas) {
            console.log('Cambiando a la página:', nuevaPagina);
            setPaginaActual(nuevaPagina);
        }
    };

    useEffect(() => {
        console.log('Asientos actuales:', asientos);
    }, [asientos]);

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
                <input type="date" id="fechaInicio" value={fechaInicio} onChange={handleFechaInicioChange} />
                <label htmlFor="fechaFin">Hasta:</label>
                <input type="date" id="fechaFin" value={fechaFin} onChange={handleFechaFinChange} />
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
