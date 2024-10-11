import React, { useState } from 'react';
import axios from 'axios';

const Asiento = () => {
  const [descripcion, setDescripcion] = useState('');
  const [monto, setMonto] = useState(0);
  const [fecha, setFecha] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();

    const nuevoAsiento = {
      descripcion,
      monto,
      fecha,
    };

    try {
      const response = await axios.post('http://localhost:8080/api/asientos', nuevoAsiento);
      console.log('Asiento creado:', response.data);
      // Aquí podrías actualizar el estado o mostrar un mensaje de éxito
    } catch (error) {
      console.error('Error al crear el asiento:', error);
      // Aquí podrías manejar el error (mostrar un mensaje al usuario, etc.)
    }
  };

  return (
    <div>
      <h2>Crear Asiento Contable</h2>
      <form onSubmit={handleSubmit}>
        <div>
          <label>Descripción:</label>
          <input 
            type="text" 
            value={descripcion} 
            onChange={(e) => setDescripcion(e.target.value)} 
            required 
          />
        </div>
        <div>
          <label>Monto:</label>
          <input 
            type="number" 
            value={monto} 
            onChange={(e) => setMonto(e.target.value)} 
            required 
          />
        </div>
        <div>
          <label>Fecha:</label>
          <input 
            type="date" 
            value={fecha} 
            onChange={(e) => setFecha(e.target.value)} 
            required 
          />
        </div>
        <button type="submit">Crear Asiento</button>
      </form>
    </div>
  );
};

export default Asiento;
