import React, { useState } from 'react';

const Register = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [message, setMessage] = useState('');

    const handleRegister = async () => {
        if (!username || !password) {
            setMessage('Por favor, complete todos los campos.');
            return;
        }

        try {
            const response = await fetch('http://localhost:8080/api/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ nombreUsuario: username, contraseña: password }),
            });

            if (!response.ok) {
                const errorText = await response.text();
                console.error('Error:', response.status, errorText);
                throw new Error(`Error en el registro: ${response.status} ${errorText}`);
            }

            const data = await response.text(); 
            setMessage(data);
        } catch (error) {
            setMessage(error.message);
        }
    };

    return (
        <div>
            <h2>Registro</h2>
            <input type="text" placeholder="Username" value={username} onChange={(e) => setUsername(e.target.value)} />
            <input type="password" placeholder="Password" value={password} onChange={(e) => setPassword(e.target.value)} />
            <button onClick={handleRegister}>Registrarse</button>
            <p>{message}</p>
        </div>
    );
};

export default Register;