import React, { useEffect, useState } from 'react';
import axios from 'axios';
import '../css/LibroMayor.css';

const LibroMayor = () => {
<<<<<<< Updated upstream
    const [codigoCuenta, setCodigoCuenta] = useState(''); // Cambiado a una cadena vacía
=======
    const [codigoCuenta, setCodigoCuenta] = useState('');
>>>>>>> Stashed changes
    const [fechaInicio, setFechaInicio] = useState('');
    const [fechaFin, setFechaFin] = useState('');
    const [libroMayor, setLibroMayor] = useState([]);
    const [cargando, setCargando] = useState(false);
    const [error, setError] = useState('');
    const [saldoFinal, setSaldoFinal] = useState(0);

    useEffect(() => {
        const hoy = new Date();
        const hace30Dias = new Date();
        hace30Dias.setDate(hoy.getDate() - 30);
        setFechaInicio(hace30Dias.toISOString().split('T')[0]);
        setFechaFin(hoy.toISOString().split('T')[0]);
    }, []);

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

            console.log('Respuesta del libro mayor:', response.data);
            setLibroMayor(response.data);

            // Calcular el saldo acumulado y final
            let saldoAcumulado = 0; // Inicializar el saldo acumulado
            response.data.forEach((movimiento) => {
                saldoAcumulado += (movimiento.debe || 0) - (movimiento.haber || 0);
                movimiento.saldo = saldoAcumulado; // Asignar el saldo acumulado a cada movimiento
            });

            // Guardar el saldo final
            setSaldoFinal(saldoAcumulado);
            setError('');
        } catch (error) {
            console.error('Error al obtener el libro mayor:', error.response ? error.response.data : error.message);
            setError(error.response ? error.response.data.mensaje : 'Error al obtener el libro mayor.');
        } finally {
            setCargando(false);
        }
    };

    if (cargando) {
        return <p>Cargando...</p>;
    }

    if (error) {
        return <p style={{ color: 'red' }}>{error}</p>;
    }

    return (
        <div className="libro-mayor">
            <h1>Libro Mayor</h1>
            <form onSubmit={obtenerLibroMayor}>
                <div>
                    <label htmlFor="codigoCuenta">Código de Cuenta:</label>
                    <input
                        type="text"
                        id="codigoCuenta"
                        name="codigoCuenta"
                        value={codigoCuenta}
                        onChange={(e) => setCodigoCuenta(e.target.value)}
<<<<<<< Updated upstream
                        placeholder="Código" // Agregado placeholder
=======
                        placeholder='Código'
>>>>>>> Stashed changes
                        required
                    />
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
                                <td>{movimiento.saldo?.toFixed(2) || '0.00'}</td> {/* Mostrar el saldo acumulado */}
                            </tr>
                        ))}
                        <tr>
                            <td colSpan={2}><strong>Saldo Final</strong></td>
                            <td colSpan={3}><strong>{saldoFinal.toFixed(2)}</strong></td> {/* Mostrar el saldo final */}
                        </tr>
                    </tbody>
                </table>
            )}
        </div>
    );
};

export default LibroMayor;
