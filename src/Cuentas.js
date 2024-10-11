import React, { useState, useEffect } from 'react';
import axios from 'axios';

const Cuenta = () => {
  const [cuentas, setCuentas] = useState([]);
  const [nombre, setNombre] = useState('');
  const [saldo, setSaldo] = useState(0);
  const [isEditing, setIsEditing] = useState(false);
  const [currentCuentaId, setCurrentCuentaId] = useState(null);

  // Función para obtener cuentas
  const fetchCuentas = async () => {
    try {
      const response = await axios.get('http://localhost:3000/api/cuentas');
      setCuentas(response.data);
    } catch (error) {
      console.error('Error al obtener las cuentas:', error);
    }
  };

  // Efecto para cargar cuentas al montar el componente
  useEffect(() => {
    fetchCuentas();
  }, []);

  // Función para agregar o editar una cuenta
  const handleSubmit = async (e) => {
    e.preventDefault();
    const cuentaData = { nombre, saldo };

    try {
      if (isEditing) {
        await axios.put(`http://localhost:3000/api/cuentas/${currentCuentaId}`, cuentaData);
      } else {
        await axios.post('http://localhost:3000/api/cuentas', cuentaData);
      }
      fetchCuentas(); // Recargar cuentas
      resetForm();
    } catch (error) {
      console.error('Error al guardar la cuenta:', error);
    }
  };

  // Función para editar una cuenta
  const editCuenta = (cuenta) => {
    setNombre(cuenta.nombre);
    setSaldo(cuenta.saldo);
    setIsEditing(true);
    setCurrentCuentaId(cuenta.id);
  };

  // Función para resetear el formulario
  const resetForm = () => {
    setNombre('');
    setSaldo(0);
    setIsEditing(false);
    setCurrentCuentaId(null);
  };

  return (
    <div>
      <h2>Gestión de Cuentas</h2>
      <form onSubmit={handleSubmit}>
        <input
          type="text"
          placeholder="Nombre de la cuenta"
          value={nombre}
          onChange={(e) => setNombre(e.target.value)}
          required
        />
        <input
          type="number"
          placeholder="Saldo inicial"
          value={saldo}
          onChange={(e) => setSaldo(e.target.value)}
          required
        />
        <button type="submit">{isEditing ? 'Actualizar' : 'Agregar'}</button>
        <button type="button" onClick={resetForm}>Cancelar</button>
      </form>
      <ul>
        {cuentas.map((cuenta) => (
          <li key={cuenta.id}>
            {cuenta.nombre} - Saldo: {cuenta.saldo}
            <button onClick={() => editCuenta(cuenta)}>Editar</button>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default Cuenta;
