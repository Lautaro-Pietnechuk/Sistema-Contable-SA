import React, { useState } from 'react';

const CrearRol = () => {
    const [nombre, setNombre] = useState('');
    const [message, setMessage] = useState('');

    const handleCrearRol = async () => {
        if (!nombre) {
            setMessage('Por favor, complete el campo de nombre.');
            return;
        }

        try {
            const response = await fetch('http://localhost:8080/api/roles', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ nombre }),
            });

            if (!response.ok) {
                const errorText = await response.text();
                console.error('Error:', response.status, errorText);
                throw new Error(`Error al crear rol: ${response.status} ${errorText}`);
            }

            const data = await response.json();
            setMessage(`Rol creado exitosamente: ${data.nombre}`);
        } catch (error) {
            setMessage(error.message);
        }
    };

    return (
        <div>
            <h2>Crear Rol</h2>
            <input type="text" placeholder="Nombre del Rol" value={nombre} onChange={(e) => setNombre(e.target.value)} />
            <button onClick={handleCrearRol}>Crear Rol</button>
            <p>{message}</p>
        </div>
    );
};

export default CrearRol;