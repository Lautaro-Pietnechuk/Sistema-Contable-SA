import React, { useState } from 'react';

const CrearPermiso = () => {
    const [nombre, setNombre] = useState('');
    const [message, setMessage] = useState('');

    const handleCrearPermiso = async () => {
        if (!nombre) {
            setMessage('Por favor, complete el campo de nombre.');
            return;
        }

        try {
            const response = await fetch('http://localhost:8080/api/permisos', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ nombre }),
            });

            if (!response.ok) {
                const errorText = await response.text();
                console.error('Error:', response.status, errorText);
                throw new Error(`Error al crear permiso: ${response.status} ${errorText}`);
            }

            const data = await response.json();
            setMessage(`Permiso creado exitosamente: ${data.nombre}`);
        } catch (error) {
            setMessage(error.message);
        }
    };

    return (
        <div>
            <h2>Crear Permiso</h2>
            <input type="text" placeholder="Nombre del Permiso" value={nombre} onChange={(e) => setNombre(e.target.value)} />
            <button onClick={handleCrearPermiso}>Crear Permiso</button>
            <p>{message}</p>
        </div>
    );
};

export default CrearPermiso;