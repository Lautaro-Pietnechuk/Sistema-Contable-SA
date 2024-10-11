import React, { useState, useEffect } from 'react';
import axios from 'axios';

const ListaCuentas = () => {
    const [cuentas, setCuentas] = useState([]);

    // Función para obtener las cuentas desde la API
    const obtenerCuentas = async () => {
        try {
            const respuesta = await axios.get('http://localhost:8080/api/cuentas');
            setCuentas(respuesta.data);
        } catch (error) {
            console.error('Error obteniendo cuentas:', error);
            setCuentas([]); // Asegurarse de que cuentas sea un array en caso de error
        }
    };

    // Ejecutar obtenerCuentas cuando el componente se monte
    useEffect(() => {
        obtenerCuentas();
    }, []);

    return (
        <div>
            <h2>Lista de Cuentas</h2>
            <ul>
                {Array.isArray(cuentas) ? (
                    cuentas.map((cuenta) => (
                        <li key={cuenta.id}>{cuenta.nombre}</li>
                    ))
                ) : (
                    <li>No hay cuentas disponibles</li>
                )}
            </ul>
        </div>
    );
};

export default ListaCuentas;