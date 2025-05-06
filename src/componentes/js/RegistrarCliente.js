import React, { useState } from 'react';
import '../css/RegistrarCliente.css';

const RegistrarCliente = ({ show, handleClose, handleSave }) => {
  const [nombre, setNombre] = useState('');
  const [email, setEmail] = useState('');
  const [telefono, setTelefono] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    handleSave({ nombre, email, telefono });
    setNombre('');
    setEmail('');
    setTelefono('');
    handleClose();
  };

  const handleTelefonoChange = (e) => {
    const value = e.target.value;
    if (/^\d*$/.test(value)) {
      setTelefono(value);
    }
  };

  return (
    <div className={`registrar-cliente-modal ${show ? 'show' : ''}`}>
      <div className="registrar-cliente-header">
        <button className="close-button" onClick={handleClose}>X</button>
        <h2>Registrar Cliente</h2>
      </div>
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>Nombre</label>
          <input
            type="text"
            value={nombre}
            onChange={(e) => setNombre(e.target.value)}
            placeholder="Nombre del cliente"
            required
            className="input-tamano"
          />
        </div>
        <div className="form-group">
          <label>Email</label>
          <input
            type="text"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="Email del cliente"
            required
            className="input-tamano"
          />
        </div>
        <div className="form-group">
          <label>Teléfono</label>
          <input
            type="text"
            value={telefono}
            onChange={handleTelefonoChange}
            placeholder="Teléfono del cliente"
            required
            className="input-tamano"
          />
        </div>
        <div className="boton-container">
          <button type="submit" className="registrar-cliente-boton">Registrar Cliente</button>
        </div>
      </form>
    </div>
  );
};

export default RegistrarCliente;
