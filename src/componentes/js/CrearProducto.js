import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

const CrearProducto = () => {
    const [token, setToken] = useState('');
    const navigate = useNavigate();
    
    // 1. Agregamos metodoPago al estado inicial (Por defecto EFECTIVO)
    const [producto, setProducto] = useState({
        nombre: '',
        descripcion: '',
        precio: '',
        costoPromedio: '', 
        stock: '',
        metodoPago: 'EFECTIVO' 
    });

    const [mensaje, setMensaje] = useState('');
    const [error, setError] = useState(false);

    useEffect(() => {
        const storedToken = localStorage.getItem('token');
        if (!storedToken) {
            alert('Sesión expirada o no iniciada. Por favor, inicie sesión.');
            navigate('/login');
        } else {
            setToken(storedToken);
        }
    }, [navigate]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setProducto({
            ...producto,
            [name]: value
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setMensaje('');
        setError(false);

        try {
            const response = await fetch('http://localhost:8080/api/productos', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({
                    ...producto, // Esto ya incluye el metodoPago
                    precio: parseFloat(producto.precio),
                    costoPromedio: parseFloat(producto.costoPromedio),
                    stock: parseInt(producto.stock)
                }),
            });

            if (response.ok) {
                const data = await response.json();
                setMensaje(`Producto "${data.nombre}" creado con éxito.`);
                // 3. Limpiamos todos los campos y volvemos metodoPago a su valor por defecto
                setProducto({ 
                    nombre: '', 
                    descripcion: '', 
                    precio: '', 
                    costoPromedio: '', 
                    stock: '',
                    metodoPago: 'EFECTIVO'
                });
            } else {
                const errorMsg = await response.text();
                setError(true);
                setMensaje(`Error: ${errorMsg}`);
            }
        } catch (err) {
            setError(true);
            setMensaje('No se pudo conectar con el servidor.');
        }
    };

    return (
        <div style={{ maxWidth: '500px', margin: '20px auto', padding: '20px', border: '1px solid #ccc', borderRadius: '8px', fontFamily: 'Arial' }}>
            <h2 style={{ textAlign: 'center' }}>Nuevo Producto</h2>
            
            <form onSubmit={handleSubmit}>
                <div style={{ marginBottom: '15px' }}>
                    <label style={{ display: 'block', marginBottom: '5px' }}>Nombre del Producto:</label>
                    <input type="text" name="nombre" value={producto.nombre} onChange={handleChange} required style={{ width: '100%', padding: '8px', boxSizing: 'border-box' }} />
                </div>

                <div style={{ marginBottom: '15px' }}>
                    <label style={{ display: 'block', marginBottom: '5px' }}>Descripción:</label>
                    <textarea name="descripcion" value={producto.descripcion} onChange={handleChange} style={{ width: '100%', padding: '8px', boxSizing: 'border-box', minHeight: '60px' }} />
                </div>

                <div style={{ display: 'flex', gap: '10px', marginBottom: '15px' }}>
                    <div style={{ flex: 1 }}>
                        <label style={{ display: 'block', marginBottom: '5px' }}>Precio Venta:</label>
                        <input type="number" step="0.01" name="precio" value={producto.precio} onChange={handleChange} required style={{ width: '100%', padding: '8px', boxSizing: 'border-box' }} />
                    </div>
                    
                    <div style={{ flex: 1 }}>
                        <label style={{ display: 'block', marginBottom: '5px' }}>Costo (por unidad):</label>
                        <input type="number" step="0.01" name="costoPromedio" value={producto.costoPromedio} onChange={handleChange} required style={{ width: '100%', padding: '8px', boxSizing: 'border-box' }} />
                    </div>

                    <div style={{ flex: 1 }}>
                        <label style={{ display: 'block', marginBottom: '5px' }}>Stock Inicial:</label>
                        <input type="number" name="stock" value={producto.stock} onChange={handleChange} required style={{ width: '100%', padding: '8px', boxSizing: 'border-box' }} />
                    </div>
                </div>

                {/* 4. Nuevo bloque para el Método de Pago de la compra inicial */}
                <div style={{ marginBottom: '20px' }}>
                    <label style={{ display: 'block', marginBottom: '5px' }}>Método de Pago (Compra Inicial):</label>
                    <select 
                        name="metodoPago" 
                        value={producto.metodoPago} 
                        onChange={handleChange} 
                        required 
                        style={{ width: '100%', padding: '8px', boxSizing: 'border-box', backgroundColor: '#fff', border: '1px solid #ccc', borderRadius: '4px' }}
                    >
                        <option value="EFECTIVO">Efectivo</option>
                        <option value="TRANSFERENCIA">Transferencia Bancaria</option>
                        <option value="CREDITO">Credito</option>
                    </select>
                </div>

                <button type="submit" style={{ width: '100%', padding: '12px', backgroundColor: '#007bff', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}>
                    Registrar Producto
                </button>
            </form>

            {mensaje && (
                <div style={{ marginTop: '20px', padding: '10px', textAlign: 'center', color: 'white', backgroundColor: error ? '#dc3545' : '#28a745', borderRadius: '4px' }}>
                    {mensaje}
                </div>
            )}
        </div>
    );
};

export default CrearProducto;