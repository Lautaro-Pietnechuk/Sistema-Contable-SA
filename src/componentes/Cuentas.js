import React, { useEffect, useState } from 'react';
import axios from 'axios';

const Cuentas = () => {
    const [cuentas, setCuentas] = useState([]);
    const [error, setError] = useState(''); // Inicializa el estado para los errores

    // Función para obtener las cuentas desde la API
    const obtenerCuentas = async () => {
        try {
            const token = localStorage.getItem('token'); // Obtener el token del localStorage
            console.log('Token:', token); // Agrega esta línea para depuración
    
            const respuesta = await axios.get('http://localhost:8080/api/cuentas', {
                headers: {
                    Authorization: `Bearer ${token}`, // Incluir el token en la cabecera
                },
            });
            
            console.log('Respuesta de la API:', respuesta.data); // Log para ver la respuesta de la API
            
            setCuentas(respuesta.data); // Guardar las cuentas en el estado
            setError(''); // Limpiar el mensaje de error en caso de éxito
        } catch (error) {
            console.error('Error obteniendo cuentas:', error);
            setCuentas([]); // Asegurarse de que cuentas sea un array en caso de error
            setError('Error al obtener cuentas.'); // Establecer el mensaje de error
        }
    };
    

    // Ejecutar obtenerCuentas cuando el componente se monte
    useEffect(() => {
        obtenerCuentas(); // Llamar a la función aquí
    }, []);

    return (
        <div>
            <h2>Lista de Cuentas</h2>
            {error && <p style={{ color: 'red' }}>{error}</p>} {/* Mostrar el mensaje de error si existe */}
            <ul>
                {Array.isArray(cuentas) && cuentas.length > 0 ? (
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

export default Cuentas;
