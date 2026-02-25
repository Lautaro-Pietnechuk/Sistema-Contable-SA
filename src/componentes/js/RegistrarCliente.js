import React, { useState, useEffect } from 'react';
import '../css/RegistrarCliente.css';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

const RegistrarCliente = ({ show, handleClose, handleSave }) => {
  const [nombre, setNombre] = useState('');
  const [email, setEmail] = useState('');
  const [telefono, setTelefono] = useState('');
  const [token, setToken] = useState('');
  const navigate = useNavigate();
  const [mensajeExito, setMensajeExito] = useState(''); 
  const [mensajeError, setMensajeError] = useState('');  

    useEffect(() => {
        const storedToken = localStorage.getItem('token');
        if (!storedToken) {
            alert('Sesión expirada o no iniciada. Por favor, inicie sesión.');
            navigate('/login');
        } else {
            setToken(storedToken);
        }
    }, [navigate]);
  
  const handleSubmit = async (e) => {
    e.preventDefault();
    setMensajeError(''); 

    const cliente = { 
        nombre, 
        mail: email || null, 
        telefono: telefono || null 
    };

    try {
      const response = await axios.post('http://localhost:8080/clientes', cliente, {
        headers: {
          'Content-Type': 'application/json', 
          'Authorization': `Bearer ${token}` 
        }
      });

      setMensajeExito('Cuenta creada con éxito.');
      setTimeout(() => setMensajeExito(''), 3000); 

      const nuevoCliente = response.data; 
      handleSave(nuevoCliente);

      setNombre('');
      setEmail('');
      setTelefono('');
      handleClose();

    } catch (error) {
        if (error.response) {
            console.error('Error del servidor:', error.response.data);
            
            if (error.response.status === 400) {
                setMensajeError(error.response.data.mensaje);
            } else if (error.response.status === 401) {
                alert('Sesión expirada. Por favor, inicie sesión nuevamente.');
                localStorage.removeItem('token');
                navigate('/login');
            } else if (error.response.status === 403) {
                setMensajeError('Necesitas permisos de administrador para realizar esta acción.');
            } else if (error.response.status === 500) {
                setMensajeError(error.response.data.mensaje || 'Ocurrió un error interno en el servidor.');
            } else {
                setMensajeError(`Error ${error.response.status}: ${error.response.data.mensaje || 'Error inesperado'}`);
            }
            
        } else if (error.request) {
            console.error('No se recibió respuesta del servidor:', error.request);
            setMensajeError('No se pudo conectar con el servidor. Verifica tu conexión.');
            
        } else {
            console.error('Error al configurar la solicitud:', error.message);
            setMensajeError('Error desconocido al intentar registrar el cliente.');
        }
    }
  };
  
  const handleTelefonoChange = (e) => {
    const value = e.target.value;
    if (/^\d*$/.test(value)) {
      setTelefono(value);
    }
  };

  // Estilos "blindados" para evitar que el CSS externo los modifique de forma distinta
  const formGroupStyle = { 
    width: '100%', 
    maxWidth: '300px', 
    margin: '0 auto 15px auto',
    display: 'flex',
    flexDirection: 'column'
  };
  
  const labelStyle = { 
    display: 'block', 
    textAlign: 'left', 
    marginBottom: '5px',
    fontWeight: 'bold',
    color: '#555'
  };

  const inputStyle = { 
    width: '100%', 
    boxSizing: 'border-box', 
    padding: '8px 12px',      // Padding uniforme
    margin: '0',              // Sin márgenes raros por defecto
    border: '1px solid #ccc', // Borde uniforme
    borderRadius: '4px',      // Bordes redondeados iguales
    fontSize: '14px',         // Mismo tamaño de letra
    fontFamily: 'inherit'     // Misma tipografía
  };

  return (
    <div className={`registrar-cliente-modal ${show ? 'show' : ''}`}>
      <div className="registrar-cliente-header">
        <button className="close-button" onClick={handleClose}>X</button>
        <h2>Registrar Cliente</h2>
      </div>

      {mensajeExito && <p style={{ color: 'green', fontWeight: 'bold' }}>{mensajeExito}</p>}
      {mensajeError && <p style={{ color: 'red', fontWeight: 'bold' }}>{mensajeError}</p>}

      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
        
        <div className="form-group" style={formGroupStyle}>
          <label style={labelStyle}>Nombre*</label>
          <input
            type="text"
            value={nombre}
            onChange={(e) => setNombre(e.target.value)}
            placeholder="Nombre COMPLETO"
            required
            className="input-tamano"
            style={inputStyle} 
          />
        </div>

        <div className="form-group" style={formGroupStyle}>
          <label style={labelStyle}>Email</label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="Email del cliente"
            className="input-tamano"
            style={inputStyle} 
          />
        </div>

        <div className="form-group" style={formGroupStyle}>
          <label style={labelStyle}>Teléfono</label>
          <input
            type="text" // Aunque sea teléfono, usamos text con validación para evitar las flechitas de type="number"
            value={telefono}
            onChange={handleTelefonoChange}
            placeholder="Teléfono del cliente"
            className="input-tamano"
            style={inputStyle} 
          />
        </div>

        <div className="boton-container" style={{ marginTop: '10px' }}>
          <button type="submit" className="registrar-cliente-boton">Registrar Cliente</button>
        </div>
      </form>
    </div>
  );
};

export default RegistrarCliente;