// src/App.js
import React from 'react';
import Login from './components/Login';
import Register from './components/Register';

function App() {
    return (
        <div>
            <h1>Aplicación de Usuarios</h1>
            <Login />
            <Register />
        </div>
    );
}

export default App;

