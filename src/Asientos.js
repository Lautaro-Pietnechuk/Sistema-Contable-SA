import React, { useEffect, useState } from 'react';
import axios from 'axios';

const Asientos = () => {
    const [asientos, setAsientos] = useState([]);

    useEffect(() => {
        const fetchAsientos = async () => {
            try {
                const response = await axios.get('http://localhost:8080/api/asientos');
                setAsientos(response.data);
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
                                {asiento.movimientos.map((movimiento, index) => (
                                    <div key={index}>
                                        {movimiento.cuenta.nombre}: {movimiento.monto} {movimiento.tipo}
                                    </div>
                                ))}
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
};

export default Asientos;
