import React, { useState } from 'react';
import './App.css';
import Login from './Login';
import Register from './Register';

function App() {
  const [showLogin, setShowLogin] = useState(true);

  return (
    <div className="App">
      <button onClick={() => setShowLogin(!showLogin)}>
        {showLogin ? 'Crear Cuenta' : 'Iniciar Sesión'}
      </button>
      {showLogin ? <Login /> : <Register />}
    </div>
  );
}

export default App;