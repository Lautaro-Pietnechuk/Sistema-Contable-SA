import React, { useState } from 'react';

const AsignarRol = () => {
    const [usuarioId, setUsuarioId] = useState('');
    const [rolId, setRolId] = useState('');
    const [message, setMessage] = useState('');

    const handleAsignarRol = async () => {
        // Validar campos
        if (!usuarioId || !rolId) {
            setMessage('Por favor, complete todos los campos.');
            return;
        }

        try {
            const response = await fetch(`http://localhost:8080/api/usuarios/${usuarioId}/roles/${rolId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
            });

            // Manejar la respuesta del servidor
            if (!response.ok) {
                const errorText = await response.text();
                console.error('Error:', response.status, errorText);
                throw new Error(`Error al asignar rol: ${response.status} ${errorText}`);
            }

            const data = await response.text();
            setMessage(data); // Mostrar mensaje de éxito
        } catch (error) {
            console.error('Error:', error);
            setMessage(`Error: ${error.message}`); // Mostrar mensaje de error
        }
    };

    return (
        <div>
            <h2>Asignar Rol</h2>
            <input
                type="text"
                placeholder="ID del Usuario"
                value={usuarioId}
                onChange={(e) => setUsuarioId(e.target.value)}
            />
            <input
                type="text"
                placeholder="ID del Rol"
                value={rolId}
                onChange={(e) => setRolId(e.target.value)}
            />
            <button onClick={handleAsignarRol}>Asignar Rol</button>
            <p>{message}</p>
        </div>
    );
};

export default AsignarRol;
