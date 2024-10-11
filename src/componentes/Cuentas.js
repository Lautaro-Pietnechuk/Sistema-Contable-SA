import React, { useEffect, useState } from 'react';
import axios from 'axios';

const Cuentas = () => {
    const [cuentas, setCuentas] = useState([]);

    useEffect(() => {
        const fetchCuentas = async () => {
            try {
                const response = await axios.get('http://localhost:8080/api/cuentas');
                console.log('Respuesta de la API:', response.data); // Verifica aquí
                if (Array.isArray(response.data)) {
                    setCuentas(response.data);
                } else {
                    console.error('La respuesta no es un array:', response.data);
                    setCuentas([]); // Establecer un array vacío si no es un array
                }
            } catch (error) {
                console.error('Error fetching cuentas:', error);
            }
        };

        fetchCuentas();
    }, []);

    return (
        <div>
            <h2>Plan de Cuentas</h2>
            <table>
                <thead>
                    <tr>
                        <th>Número de Cuenta</th>
                        <th>Nombre</th>
                        <th>Recibe Saldo</th>
                        <th>Tipo</th>
                    </tr>
                </thead>
                <tbody>
                    {cuentas.map(cuenta => (
                        <tr key={cuenta.id}>
                            <td>{cuenta.nroCuenta}</td>
                            <td>{cuenta.nombre}</td>
                            <td>{cuenta.recibeSaldo ? 'Sí' : 'No'}</td>
                            <td>{cuenta.tipo}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
};

export default Cuentas;
