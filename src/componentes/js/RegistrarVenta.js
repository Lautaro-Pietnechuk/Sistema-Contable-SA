import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { jwtDecode } from "jwt-decode";
import { useNavigate } from 'react-router-dom';
import { jsPDF } from 'jspdf';
import autoTable from 'jspdf-autotable';
import '../css/CrearAsiento.css'; 

// 1. Función movida fuera del componente para evitar recreaciones en cada render
const obtenerFechaActual = () => {
    const fecha = new Date();
    return new Date(fecha.getTime() - fecha.getTimezoneOffset() * 60000).toISOString().split("T")[0];
};

const RegistrarVenta = () => {
    const navigate = useNavigate();

    // 2. Estado inicial limpio y sin dependencias cruzadas
    const [venta, setVenta] = useState({
        fecha: obtenerFechaActual(), 
        numeroVenta: '',
        cliente: '',
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

    // 3. Cálculo dinámico del total en cada renderizado (Evita bucles infinitos)
    const totalVenta = subVentas.reduce((acc, subVenta) => acc + Number(subVenta.subtotal || 0), 0);

    // Efecto para verificar sesión e identificar al usuario
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

    // Efecto para cargar clientes y productos solo cuando el token existe
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

        // Buscamos el producto para calcular el subtotal dinámicamente
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

        // 4. Aseguramos que los datos que enviamos al backend sean números (Integer/Long) y no Strings
        const payload = {
            fecha: venta.fecha + "T00:00:00", // Agregamos la hora porque Java espera un LocalDateTime
            clienteId: Number(venta.cliente), // <-- Cambiado de idCliente a clienteId
            usuarioId: Number(usuario.id),
            total: Number(totalVenta),
            observaciones: venta.observaciones,
            detalles: subVentas.map(sv => ({
                productoId: Number(sv.producto), // <-- Cambiado de idProducto a productoId
                cantidad: Number(sv.cantidad),
                subtotal: Number(sv.subtotal)
            }))
        };

        // Imprimimos el payload para que puedas verificar en consola qué estás enviando
        console.log("Enviando payload al backend:", payload);

        try {
            const config = {
                headers: { Authorization: `Bearer ${token}` }
            };

            const response = await axios.post('http://localhost:8080/api/ventas', payload, config);
            const ventaCreada = response.data;

            console.log("Venta creada:", ventaCreada);

            // Generar PDF con los datos de la venta
            generarPDFVenta(ventaCreada);

            setMensajeExito('Venta registrada y factura descargada con éxito.');
            setMensajeError('');

            // Limpiamos el formulario tras el éxito
            setVenta(prev => ({
                ...prev,
                cliente: '',
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

            // Encabezado
            doc.setFont('helvetica', 'bold');
            doc.text('FACTURA DE VENTA', 14, 20);

            doc.setFont('helvetica', 'normal');
            doc.setFontSize(10);
            doc.text(`Comprobante: ${ventaData.numeroComprobante}`, 14, 30);
            doc.text(`Fecha: ${new Date(ventaData.fecha).toLocaleDateString()}`, 14, 37);
            doc.text(`Cliente: ${ventaData.clienteNombre}`, 14, 44);

            // Tabla de detalles
            const detalles = ventaData.detalles.map(d => [
                d.productoNombre,
                d.cantidad,
                `$${d.precioUnitario.toFixed(2)}`,
                `$${d.subtotal.toFixed(2)}`
            ]);

            autoTable(doc, {
                head: [['Producto', 'Cantidad', 'Precio Unit.', 'Subtotal']],
                body: detalles,
                startY: 52,
                margin: { left: 14, right: 14 },
                styles: { fontSize: 10 },
                headStyles: { fillColor: [41, 128, 185] }
            });

            // Total
            const finalY = doc.lastAutoTable.finalY || 100;
            doc.setFont('helvetica', 'bold');
            doc.text(`Total: $${ventaData.total.toFixed(2)}`, 14, finalY + 10);

            // Observaciones
            if (ventaData.observaciones) {
                doc.setFont('helvetica', 'normal');
                doc.setFontSize(9);
                doc.text(`Observaciones: ${ventaData.observaciones}`, 14, finalY + 20);
            }

            // Descargar
            const pdfBuffer = doc.output('arraybuffer');
            const pdfBlob = new Blob([pdfBuffer], { type: 'application/pdf' });
            const url = window.URL.createObjectURL(pdfBlob);
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', `Factura_${ventaData.numeroComprobante}.pdf`);
            document.body.appendChild(link);
            link.click();
            link.remove();
            window.URL.revokeObjectURL(url);
        } catch (error) {
            console.error('Error al generar el PDF:', error);
            alert('Error al generar el PDF: ' + error.message);
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
                <select
                    name="cliente"
                    value={venta.cliente}
                    onChange={handleChange}
                    required
                    className="crear-asiento-descripcion"
                    style={{ marginBottom: '15px' }}
                >
                    <option value="">Seleccione un cliente</option>
                    {clientes.map((cliente) => (
                        <option key={cliente.id} value={cliente.id}>{cliente.nombre}</option>
                    ))}
                </select>

                <h3>SubVentas</h3>
                
                <table className="crear-asiento-tabla">
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
                                        {productos.filter((producto) => producto.activo !== false).map((producto) => (
                                            <option key={producto.id} value={producto.id}>{producto.nombre}</option>
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
                                <td style={{ textAlign: 'center', fontWeight: 'bold' }}>
                                    ${subVenta.subtotal}
                                </td>
                                <td>
                                    <button type="button" className="crear-asiento-boton eliminar" onClick={() => handleRemoveSubVenta(index)}>
                                        Eliminar
                                    </button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>

                <div style={{ marginTop: '15px', display: 'flex', flexDirection: 'column', alignItems: 'flex-start' }}>
                    <label htmlFor="observaciones" style={{ fontSize: '0.9rem', marginBottom: '5px', color: '#555' }}>
                        Observaciones (opcional)
                    </label>
                    <textarea 
                        name="observaciones"
                        id="observaciones"
                        value={venta.observaciones}
                        onChange={handleChange}
                        placeholder="Escriba aquí..."
                        className="crear-asiento-descripcion"
                        rows="2"
                        style={{ width: '50%', minWidth: '250px', resize: 'vertical' }}
                    ></textarea>
                </div>

                <div style={{ textAlign: 'center', fontWeight: 'bold', fontSize: '1.2rem', margin: '15px 0' }}>
                    Total Venta: ${totalVenta}
                </div>

                <div className="boton-container">
                    <button className="crear-asiento-boton" type="button" onClick={handleAddSubVenta}>
                        Agregar SubVenta
                    </button>
                    <button className="crear-asiento-boton" type="submit">
                        Registrar Venta
                    </button>
                </div>
            </form>

            {mensajeError && <p className="mensaje-error">{mensajeError}</p>}
            {mensajeExito && <p className="mensaje-exito">{mensajeExito}</p>}
        </div>
    );
};

export default RegistrarVenta;