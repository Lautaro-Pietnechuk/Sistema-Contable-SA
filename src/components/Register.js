// src/components/Register.js
import React, { useState } from 'react';

const Register = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [message, setMessage] = useState('');

    const handleRegister = async () => {
        try {
            const response = await fetch('http://localhost:8080/api/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ username, password }),
            });

            if (!response.ok) {
                throw new Error('Error en el registro');
            }

            const data = await response.text(); // O puedes usar response.json() si la respuesta es JSON
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
