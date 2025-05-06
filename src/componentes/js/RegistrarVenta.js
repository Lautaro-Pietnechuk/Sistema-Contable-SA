import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { jwtDecode } from "jwt-decode";

const RegistrarVenta = () => {
    const [venta, setVenta] = useState({
        fecha: new Date().toLocaleDateString(),
        numeroVenta: '',
        cliente: '',
        cantidad: 0,
        total: 0,
    });
    const [usuario, setUsuario] = useState(null);
    const [clientes, setClientes] = useState([]);
    const [productos, setProductos] = useState([]);
    const [subVentas, setSubVentas] = useState([
        { producto: '', cantidad: 0, subtotal: 0, total: 0 }
    ]);
    const [mensajeExito, setMensajeExito] = useState('');
    const [mensajeError, setMensajeError] = useState('');

    const obtenerFechaActual = () => {
        const fecha = new Date();
        return new Date(fecha.getTime() - fecha.getTimezoneOffset() * 60000).toISOString().split("T")[0];
    };

    useEffect(() => {
        const storedToken = localStorage.getItem("token");
        if (!storedToken) {
            setMensajeError("Sesión expirada o no iniciada. Por favor, inicie sesión.");
            return;
        }

        try {
            const decoded = jwtDecode(storedToken);
            if (!decoded.sub || !decoded.id || !decoded.rol) {
                setMensajeError("Información del usuario incompleta en el token.");
                return;
            }

            setUsuario({ id: decoded.id, nombre: decoded.sub, rol: decoded.roles });
            setVenta((prevVenta) => ({
                ...prevVenta,
                fecha: obtenerFechaActual()
            }));
        } catch (error) {
            console.error("Error al decodificar el token:", error);
            setMensajeError("Error al decodificar el token.");
        }
    }, []);

    useEffect(() => {
        // Asignar número de venta automáticamente
        const fetchNumeroVenta = async () => {
            try {
                const response = await axios.get('http://localhost:8080/api/ventas/ultimoNumero');
                const ultimoNumero = response.data.ultimoNumero || 0;
                setVenta((prevVenta) => ({
                    ...prevVenta,
                    numeroVenta: `V-${ultimoNumero + 1}`
                }));
            } catch (error) {
                console.error('Error al obtener el último número de venta:', error);
                setMensajeError('Error al obtener el último número de venta.');
            }
        };

        fetchNumeroVenta();

        // Cargar clientes y productos desde el servidor
        const fetchData = async () => {
            try {
                const clientesResponse = await axios.get('http://localhost:8080/api/clientes');
                const productosResponse = await axios.get('http://localhost:8080/api/productos');
                setClientes(clientesResponse.data);
                setProductos(productosResponse.data);
            } catch (error) {
                console.error('Error al cargar clientes o productos:', error);
                setMensajeError('Error al cargar clientes o productos.');
            }
        };

        fetchData();
    }, []);

    useEffect(() => {
        const calcularTotalVenta = () => {
            const total = subVentas.reduce((acc, subVenta) => acc + subVenta.total, 0);
            setVenta((prevVenta) => ({
                ...prevVenta,
                total
            }));
        };

        calcularTotalVenta();
    }, [subVentas]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setVenta((prevVenta) => ({
            ...prevVenta,
            [name]: value
        }));
    };

    const handleSubVentaChange = (index, e) => {
        const { name, value } = e.target;
        const updatedSubVentas = [...subVentas];
        updatedSubVentas[index] = {
            ...updatedSubVentas[index],
            [name]: value
        };

        if (name === 'producto') {
            const selectedProducto = productos.find((producto) => producto.id === value);
            if (selectedProducto) {
                updatedSubVentas[index].subtotal = selectedProducto.precio;
            }
        }

        if (name === 'cantidad') {
            updatedSubVentas[index].total = updatedSubVentas[index].cantidad * updatedSubVentas[index].subtotal;
        }

        setSubVentas(updatedSubVentas);
    };

    const handleAddSubVenta = () => {
        setSubVentas([...subVentas, { producto: '', cantidad: 0, subtotal: 0, total: 0 }]);
    };

    const handleRemoveSubVenta = (index) => {
        const nuevasSubVentas = subVentas.filter((_, i) => i !== index);
        setSubVentas(nuevasSubVentas);
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        // Aquí se puede agregar la lógica para registrar la venta
        console.log('Venta registrada:', venta);
        console.log('SubVentas registradas:', subVentas);
        setMensajeExito('Venta registrada con éxito.');
        setTimeout(() => setMensajeExito(''), 3000); // Mensaje desaparecerá después de 3 segundos
    };

    return (
        <div className="registrar-venta-container">
            <div className="usuario">Usuario: {usuario && usuario.nombre}</div>
            <div className="form-row">
                <div className="form-group">
                    <div className="fecha">Fecha: {venta.fecha}</div>
                </div>
            </div>
            <h2>Registrar Venta</h2>
            <form onSubmit={handleSubmit}>
                <div className="form-row">
                    <div className="form-group">
                        <label>Cliente:</label>
                        <select name="cliente" value={venta.cliente} onChange={handleChange} required>
                            <option value="">Seleccione un cliente</option>
                            {clientes.map((cliente) => (
                                <option key={cliente.id} value={cliente.id}>
                                    {cliente.nombre}
                                </option>
                            ))}
                        </select>
                    </div>
                </div>
                <h3 style={{ marginTop: '5px', marginBottom: '5px' }}>SubVentas:</h3>
                <table className="registrar-venta-tabla">
                    <thead>
                        <tr>
                            <th>Producto</th>
                            <th>Cantidad</th>
                            <th>SubTotal</th>
                            <th>Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                        {subVentas.map((subVenta, index) => (
                            <tr key={index}>
                                <td>
                                    <select
                                        name="producto"
                                        value={subVenta.producto}
                                        onChange={(e) => handleSubVentaChange(index, e)}
                                        required
                                    >
                                        <option value="">Seleccione un producto</option>
                                        {productos.map((producto) => (
                                            <option key={producto.id} value={producto.id}>
                                                {producto.nombre}
                                            </option>
                                        ))}
                                    </select>
                                </td>
                                <td>
                                    <input
                                        type="number"
                                        name="cantidad"
                                        value={subVenta.cantidad}
                                        onChange={(e) => handleSubVentaChange(index, e)}
                                        min="1"
                                        required
                                    />
                                </td>
                                <td>
                                    <span>{subVenta.subtotal}</span>
                                </td>
                                <td>
                                    <button
                                        type="button"
                                        className="eliminar-subventa-button"
                                        onClick={() => handleRemoveSubVenta(index)}
                                    >
                                        Eliminar
                                    </button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
                <div className="boton-container">
                    <button type="button" className="add-subventa-button" onClick={handleAddSubVenta}>
                        Agregar SubVenta
                    </button>
                    <div className="form-group">
                        <div className="Total">Total Venta: {venta.total}</div>
                    </div>
                    <button type="submit">Registrar Venta</button>
                </div>
                {mensajeExito && <p className="mensaje-exito">{mensajeExito}</p>}
                {mensajeError && <p className="mensaje-error">{mensajeError}</p>}
            </form>
        </div>
    );
};

export default RegistrarVenta;