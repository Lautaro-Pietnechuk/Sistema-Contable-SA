import React, { useEffect, useState } from 'react';
import { jwtDecode } from 'jwt-decode';

const validarToken = (token) => {
    if (!token) {
        console.error('No token found');
        return false; // No hay token
    }

    try {
        const payload = jwtDecode(token); // Usa jwtDecode aquí
        const currentTime = Math.floor(Date.now() / 1000); // Tiempo actual en segundos

        if (payload.exp && payload.exp < currentTime) {
            console.error('Token has expired');
            return false; // El token ha expirado
        }
        console.error('Token valido');
        return true; // Token válido
    } catch (error) {
        console.error('Error decoding token:', error);
        return false; // Error al decodificar el token
    }
};

const Roles = () => {
    const [roles, setRoles] = useState([]);
    const [error, setError] = useState(null); // Estado para manejar errores

    useEffect(() => {
        const token = localStorage.getItem('token'); // Obtener el token del almacenamiento local

        if (!validarToken(token)) {
            console.error('Token no válido, abortando la solicitud');
            return; // Salir si el token no es válido
        }

        async function obtenerRoles() {
            try {
                const response = await fetch('/api/roles/listar', {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    }
                });
        
                if (!response.ok) {
                    const errorText = await response.text(); // Obtener el texto de la respuesta
                    const errorMessage = errorText || 'Error desconocido'; // Mensaje específico del error
                    throw new Error(`Error ${response.status}: ${errorMessage}`);
                }
        
                const dataText = await response.text(); // Obtener el texto de la respuesta antes de analizar
                const data = dataText ? JSON.parse(dataText) : []; // Solo analizar si hay texto
                console.log('Datos recibidos:', data);
                setRoles(data);
            } catch (error) {
                console.error('Error fetching roles:', error);
                setError(`Error al obtener roles: ${error.message}`); // Establece el mensaje de error específico
                setRoles([]); // Limpiar los roles en caso de error
            }
        }
        

        obtenerRoles();
    }, []);

    return (
        <div>
            <h1>Roles</h1>
            {error && <p style={{ color: 'red' }}>{error}</p>} {/* Muestra el mensaje de error si existe */}
            <ul>
                {roles.map((role, index) => (
                    <li key={index}>{role.name}</li>
                ))}
            </ul>
        </div>
    );
};

export default Roles;
