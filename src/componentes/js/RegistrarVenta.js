import React, { useState, useEffect } from 'react';
import axios from '../../axiosConfig'; 
import { jwtDecode } from "jwt-decode";
import { useNavigate } from 'react-router-dom';
import { jsPDF } from 'jspdf';
import autoTable from 'jspdf-autotable';
import '../css/CrearAsiento.css'; 

const obtenerFechaActual = () => {
    const fecha = new Date();
    return new Date(fecha.getTime() - fecha.getTimezoneOffset() * 60000).toISOString().split("T")[0];
};

const RegistrarVenta = () => {
    const navigate = useNavigate();

    const [venta, setVenta] = useState({
        fecha: obtenerFechaActual(), 
        numeroVenta: '',
        cliente: '',
        tipoDePago: 'EFECTIVO', 
        observaciones: '' 
    });

    const [usuario, setUsuario] = useState(null);
    const [clientes, setClientes] = useState([]);
    const [productos, setProductos] = useState([]);
    const [subVentas, setSubVentas] = useState([
        { producto: '', cantidad: 0, subtotal: 0 } 
    ]);
    const [token, setToken] = useState('');
    const [mensajeExito, setMensajeExito] = useState(''); 
    const [mensajeError, setMensajeError] = useState('');  

    const totalVenta = subVentas.reduce((acc, subVenta) => acc + Number(subVenta.subtotal || 0), 0);

    useEffect(() => {
        const storedToken = localStorage.getItem('token');
        if (!storedToken) {
            alert('Sesión expirada o no iniciada. Por favor, inicie sesión.');
            navigate('/login');
            return;
        }

        setToken(storedToken);
        try {
            const decoded = jwtDecode(storedToken);
            setUsuario({ id: decoded.id, nombre: decoded.sub, rol: decoded.roles });
        } catch (error) {
            console.error("Error al decodificar el token:", error);
            setMensajeError("Error de autenticación.");
        }
    }, [navigate]);

    useEffect(() => {
        if (!token) return;

        const fetchData = async () => {
            const config = {
                headers: { Authorization: `Bearer ${token}` }
            };

            try {
                const [clientesResponse, productosResponse] = await Promise.all([
                    axios.get('http://localhost:8080/api/clientes', config),
                    axios.get('http://localhost:8080/api/productos/activos', config)
                ]);

                setClientes(clientesResponse.data);
                setProductos(productosResponse.data);
            } catch (error) {
                console.error('Error al cargar datos:', error);
                setMensajeError('No se pudieron cargar clientes/productos.');
            }
        };

        fetchData();
    }, [token]);

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
        const subVentaActual = { ...updatedSubVentas[index], [name]: value };

        const selectedProducto = productos.find(
            (p) => String(p.id) === (name === 'producto' ? String(value) : String(subVentaActual.producto))
        );
        const precioUnitario = selectedProducto ? Number(selectedProducto.precio) : 0;
        const cant = name === 'cantidad' ? Number(value) : Number(subVentaActual.cantidad);

        subVentaActual.subtotal = cant * precioUnitario;
        updatedSubVentas[index] = subVentaActual;
        setSubVentas(updatedSubVentas);
    };

    const handleAddSubVenta = () => {
        setSubVentas([...subVentas, { producto: '', cantidad: 0, subtotal: 0 }]);
    };

    const handleRemoveSubVenta = (index) => {
        setSubVentas((prev) => prev.filter((_, i) => i !== index));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!usuario || !usuario.id) {
            setMensajeError("Error: No se pudo identificar al usuario desde el token.");
            return;
        }

        if (subVentas.length === 0 || subVentas.some(sv => !sv.producto || Number(sv.cantidad) <= 0)) {
            setMensajeError("Debe agregar al menos un producto con cantidad válida.");
            return;
        }

        const payload = {
            fecha: venta.fecha + "T00:00:00",
            clienteId: Number(venta.cliente),
            usuarioId: Number(usuario.id),
            tipoDePago: venta.tipoDePago, 
            total: Number(totalVenta),
            observaciones: venta.observaciones,
            detalles: subVentas.map(sv => ({
                productoId: Number(sv.producto),
                cantidad: Number(sv.cantidad),
                subtotal: Number(sv.subtotal)
            }))
        };

        try {
            const config = {
                headers: { Authorization: `Bearer ${token}` }
            };

            const response = await axios.post('http://localhost:8080/api/ventas', payload, config);
            const ventaCreada = response.data;

            generarPDFVenta(ventaCreada);

            setMensajeExito('Venta registrada y factura descargada con éxito.');
            setMensajeError('');

            setVenta(prev => ({
                ...prev,
                cliente: '',
                tipoDePago: 'EFECTIVO',
                observaciones: ''
            }));
            setSubVentas([{ producto: '', cantidad: 0, subtotal: 0 }]);

            setTimeout(() => setMensajeExito(''), 3000);

        } catch (error) {
            console.error('Error al registrar la venta:', error);
            setMensajeError(error.response?.data || 'Hubo un error de conexión con el servidor.');
        }
    };

    const generarPDFVenta = (ventaData) => {
        try {
            const doc = new jsPDF();
            doc.setFont('helvetica', 'bold');
            doc.text('FACTURA DE VENTA', 14, 20);

            doc.setFont('helvetica', 'normal');
            doc.setFontSize(10);
            doc.text(`Comprobante: ${ventaData.numeroComprobante}`, 14, 30);
            doc.text(`Fecha: ${new Date(ventaData.fecha).toLocaleDateString()}`, 14, 37);
            doc.text(`Cliente: ${ventaData.clienteNombre}`, 14, 44);
            doc.text(`Método de Pago: ${ventaData.tipoDePago}`, 14, 51); 

            const detalles = ventaData.detalles.map(d => [
                d.productoNombre,
                d.cantidad,
                `$${d.precioUnitario.toFixed(2)}`,
                `$${d.subtotal.toFixed(2)}`
            ]);

            autoTable(doc, {
                head: [['Producto', 'Cantidad', 'Precio Unit.', 'Subtotal']],
                body: detalles,
                startY: 58,
                margin: { left: 14, right: 14 },
                styles: { fontSize: 10 },
                headStyles: { fillColor: [41, 128, 185] }
            });

            const finalY = doc.lastAutoTable.finalY || 100;
            doc.setFont('helvetica', 'bold');
            doc.text(`Total: $${ventaData.total.toFixed(2)}`, 14, finalY + 10);

            if (ventaData.observaciones) {
                doc.setFont('helvetica', 'normal');
                doc.setFontSize(9);
                doc.text(`Observaciones: ${ventaData.observaciones}`, 14, finalY + 20);
            }

            doc.save(`Factura_${ventaData.numeroComprobante}.pdf`);
        } catch (error) {
            console.error('Error al generar el PDF:', error);
        }
    };

    return (
        <div className="crear-asiento-modal"> 
            <div className="crear-asiento-header">
                {usuario && <div className="usuario">Usuario: {usuario.nombre}</div>}
                <div className="fecha">Fecha: {venta.fecha}</div>
            </div>
            
            <h2 className="crear-asiento-title">Registrar Venta</h2>
            
            <form onSubmit={handleSubmit}>
                {/* Contenedor Flex para Cliente y Pago bien alineados */}
                <div style={{ display: 'flex', gap: '20px', marginBottom: '20px' }}>
                    <div style={{ flex: 1 }}>
                        <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', color: '#333' }}>Cliente:</label>
                        <select
                            name="cliente"
                            value={venta.cliente}
                            onChange={handleChange}
                            required
                            style={{ width: '100%', padding: '10px', boxSizing: 'border-box', border: '1px solid #ccc', borderRadius: '4px' }}
                        >
                            <option value="">Seleccione un cliente</option>
                            {clientes.map((cliente) => (
                                <option key={cliente.id} value={cliente.id}>{cliente.nombre}</option>
                            ))}
                        </select>
                    </div>

                    <div style={{ flex: 1 }}>
                        <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', color: '#333' }}>Método de Pago:</label>
                        <select
                            name="tipoDePago"
                            value={venta.tipoDePago}
                            onChange={handleChange}
                            required
                            style={{ width: '100%', padding: '10px', boxSizing: 'border-box', border: '1px solid #ccc', borderRadius: '4px' }}
                        >
                            <option value="EFECTIVO">Efectivo</option>
                            <option value="TRANSFERENCIA">Transferencia</option>
                            <option value="CUENTA_CORRIENTE">Credito</option>
                        </select>
                    </div>
                </div>

                <h3 style={{ borderBottom: '1px solid #eee', paddingBottom: '10px' }}>Detalle de Productos</h3>
                
                <div style={{ overflowX: 'auto' }}>
                    <table className="crear-asiento-tabla" style={{ width: '100%', borderCollapse: 'collapse', marginBottom: '20px' }}>
                        <thead>
                            <tr>
                                <th style={{ padding: '10px', textAlign: 'left', borderBottom: '2px solid #ddd' }}>Producto</th>
                                <th style={{ padding: '10px', textAlign: 'center', borderBottom: '2px solid #ddd', width: '100px' }}>Cantidad</th>
                                <th style={{ padding: '10px', textAlign: 'right', borderBottom: '2px solid #ddd', width: '150px' }}>SubTotal</th>
                                <th style={{ padding: '10px', textAlign: 'center', borderBottom: '2px solid #ddd', width: '100px' }}>Acciones</th>
                            </tr>
                        </thead>
                        <tbody>
                            {subVentas.map((subVenta, index) => (
                                <tr key={index}>
                                    <td style={{ padding: '8px 0' }}>
                                        <select
                                            name="producto"
                                            value={subVenta.producto}
                                            onChange={(e) => handleSubVentaChange(index, e)}
                                            required
                                            style={{ width: '100%', padding: '8px', boxSizing: 'border-box', border: '1px solid #ccc', borderRadius: '4px' }}
                                        >
                                            <option value="">Seleccione un producto</option>
                                            {productos.filter((producto) => producto.activo !== false).map((producto) => (
                                                <option key={producto.id} value={producto.id}>{producto.nombre}</option>
                                            ))}
                                        </select>
                                    </td>
                                    <td style={{ padding: '8px 10px', textAlign: 'center' }}>
                                        <input
                                            type="number"
                                            name="cantidad"
                                            value={subVenta.cantidad}
                                            onChange={(e) => handleSubVentaChange(index, e)}
                                            min="1"
                                            required
                                            style={{ width: '100%', padding: '8px', boxSizing: 'border-box', border: '1px solid #ccc', borderRadius: '4px', textAlign: 'center' }}
                                        />
                                    </td>
                                    <td style={{ padding: '8px 10px', textAlign: 'right', fontWeight: 'bold', fontSize: '1.1rem' }}>
                                        ${subVenta.subtotal.toFixed(2)}
                                    </td>
                                    <td style={{ padding: '8px 0', textAlign: 'center' }}>
                                        <button 
                                            type="button" 
                                            className="crear-asiento-boton eliminar" 
                                            onClick={() => handleRemoveSubVenta(index)}
                                            style={{ backgroundColor: '#dc3545', color: 'white', border: 'none', padding: '8px 12px', borderRadius: '4px', cursor: 'pointer' }}
                                        >
                                            Eliminar
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>

                {/* Observaciones ocupando el ancho total */}
                <div style={{ marginBottom: '20px' }}>
                    <label htmlFor="observaciones" style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', color: '#333' }}>
                        Observaciones (opcional)
                    </label>
                    <textarea 
                        name="observaciones"
                        id="observaciones"
                        value={venta.observaciones}
                        onChange={handleChange}
                        placeholder="Detalles adicionales de la venta..."
                        rows="2"
                        style={{ width: '100%', padding: '10px', boxSizing: 'border-box', border: '1px solid #ccc', borderRadius: '4px', resize: 'vertical' }}
                    ></textarea>
                </div>

                {/* Total Venta alineado a la derecha como en facturas */}
                <div style={{ textAlign: 'right', fontWeight: 'bold', fontSize: '1.4rem', margin: '20px 0', color: '#2c3e50', paddingRight: '15px' }}>
                    Total Venta: ${totalVenta.toFixed(2)}
                </div>

                {/* Botones alineados y separados */}
                <div style={{ display: 'flex', justifyContent: 'space-between', gap: '15px', marginTop: '20px' }}>
                    <button 
                        type="button" 
                        onClick={handleAddSubVenta}
                        style={{ flex: 1, padding: '12px', backgroundColor: '#6c757d', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold', fontSize: '1rem' }}
                    >
                        + Agregar Producto
                    </button>
                    <button 
                        type="submit"
                        style={{ flex: 1, padding: '12px', backgroundColor: '#28a745', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold', fontSize: '1rem' }}
                    >
                        ✔ Confirmar y Registrar Venta
                    </button>
                </div>
            </form>

            {mensajeError && <p className="mensaje-error" style={{ color: '#dc3545', textAlign: 'center', marginTop: '15px', fontWeight: 'bold' }}>{mensajeError}</p>}
            {mensajeExito && <p className="mensaje-exito" style={{ color: '#28a745', textAlign: 'center', marginTop: '15px', fontWeight: 'bold' }}>{mensajeExito}</p>}
        </div>
    );
};

export default RegistrarVenta;