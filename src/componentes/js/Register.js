import React, { useState } from 'react';



const Register = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [message, setMessage] = useState('');
    const [loading, setLoading] = useState(false);

    const handleRegister = async () => {
        if (!username || !password) {
            setMessage('Por favor, complete todos los campos.');
            return;
        }

        setLoading(true);

        try {
            const response = await fetch('http://localhost:8080/api/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ nombreUsuario: username, contraseña: password }),
            });

            // Verificar si la respuesta tiene contenido JSON válido
            const isJson = response.headers.get('content-type')?.includes('application/json');
            const data = isJson ? await response.json() : await response.text();

            if (!response.ok) {
                console.error('Error:', response.status, data);
                throw new Error(data.message || `Error en el registro: ${response.status}`);
            }

            setMessage(data.message || 'Registro exitoso.');
            setUsername('');
            setPassword('');
        } catch (error) {
            setMessage(error.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ padding: '20px', maxWidth: '400px', margin: 'auto' }}>
            <h2>Registro</h2>
            <input
                type="text"
                placeholder="Nombre de usuario"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                style={{ width: '100%', padding: '10px', margin: '5px 0' }}
            />
            <input
                type="password"
                placeholder="Contraseña"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                style={{ width: '100%', padding: '10px', margin: '5px 0' }}
            />
            <button onClick={handleRegister} disabled={loading} style={{ width: '100%', padding: '10px', margin: '5px 0' }}>
                {loading ? 'Cargando...' : 'Registrarse'}
            </button>
            {message && <p style={{ color: 'red' }}>{message}</p>}
        </div>
    );
};

export default Register;
