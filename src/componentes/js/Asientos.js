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
        setFechaInicio(hace30Dias.toISOString().split('T')[0]); // Fecha de inicio: hace 30 días

        // Incrementar la fecha de fin en 1 día
        hoy.setDate(hoy.getDate() + 1); // Aumentar la fecha actual en 1 día
        setFechaFin(hoy.toISOString().split('T')[0]); // Fecha de fin: mañana
    }, []);

    useEffect(() => {
        console.log('Fechas iniciales:', { fechaInicio, fechaFin }); // Verificar fechas iniciales
    }, [fechaInicio, fechaFin]);

    useEffect(() => {
        const fetchAsientos = async () => {
            const storedToken = localStorage.getItem('token');
            if (!storedToken) {
                setError('No se encontró el token. Por favor, inicie sesión.');
                setCargando(false);
                return;
            }

            // Verificar si las fechas son válidas
            if (!fechaInicio || !fechaFin) {
                console.log('Fechas de búsqueda son inválidas.');
                setError('Por favor, establezca las fechas antes de buscar.');
                setCargando(false);
                return;
            }

            // Convertir las fechas a objetos Date para comparar
            const fechaInicioDate = new Date(fechaInicio);
            const fechaFinDate = new Date(fechaFin);
            if (fechaInicioDate > fechaFinDate) {
                setError('La fecha de inicio no puede ser posterior a la fecha de fin.');
                setCargando(false);
                return;
            }

            console.log('Fechas de búsqueda:', { fechaInicio, fechaFin, paginaActual }); // Fechas antes de la llamada
            try {
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

        // Verificar si las fechas son válidas antes de llamar a fetchAsientos
        if (fechaInicio && fechaFin) {
            fetchAsientos();
        }
    }, [fechaInicio, fechaFin, paginaActual]);

    const handleFechaInicioChange = (e) => {
        const nuevaFechaInicio = e.target.value;
        setFechaInicio(nuevaFechaInicio);
        console.log('Nueva fecha de inicio:', nuevaFechaInicio); // Verificar el cambio
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
            console.log('Cambiando a la página:', nuevaPagina); // Log para cambio de página
            setPaginaActual(nuevaPagina);
        }
    };

    useEffect(() => {
        console.log('Asientos actuales:', asientos); // Verificar asientos actuales
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
