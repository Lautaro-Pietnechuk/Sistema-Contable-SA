import React, { useEffect, useState } from 'react';
import axios from 'axios';

const Asientos = () => {
    const [asientos, setAsientos] = useState([]);

    useEffect(() => {
        const fetchAsientos = async () => {
            try {
                const response = await axios.get('http://localhost:8080/api/asientos/listar');
                // Asegúrate de que la respuesta sea un array
                if (Array.isArray(response.data)) {
                    setAsientos(response.data);
                } else {
                    console.error('La respuesta no es un array:', response.data);
                    setAsientos([]); // Establece un array vacío si no es un array
                }
            } catch (error) {
                console.error('Error fetching asientos:', error);
            }
        };

        fetchAsientos();
    }, []);

    return (
        <div>
            <h2>Libro de Asientos</h2>
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
        </div>
    );
};

export default Asientos;
