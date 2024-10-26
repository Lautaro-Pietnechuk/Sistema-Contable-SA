import { jwtDecode } from "jwt-decode";
import React, { useState, useEffect } from "react";
import axios from "axios";

const CrearAsiento = () => {
  const [usuario, setUsuario] = useState(null);
  const [mensajeError, setMensajeError] = useState("");
  const [mensajeExito, setMensajeExito] = useState("");
  const [descripcion, setDescripcion] = useState("");
  const [transacciones, setTransacciones] = useState([{ cuenta: "", tipo: "debe", monto: 0 }]);
  const [cuentas, setCuentas] = useState([]);
  const [fechaActual, setFechaActual] = useState(""); // Nuevo estado para la fecha

  useEffect(() => {
    const storedToken = localStorage.getItem("token");
    if (!storedToken) {
      setMensajeError("Sesión expirada o no iniciada. Por favor, inicie sesión.");
      return;
    }

    try {
      const decoded = jwtDecode(storedToken);
      console.log("Contenido decodificado del token:", decoded);

      if (!decoded.sub || !decoded.id || !decoded.rol) {
        setMensajeError("Información del usuario incompleta en el token.");
        return;
      }

      setUsuario({ id: decoded.id, nombre: decoded.sub, rol: decoded.roles });
      cargarCuentas(storedToken);
      setFechaActual(obtenerFechaActual()); // Establecer la fecha actual aquí
    } catch (error) {
      console.error("Error al decodificar el token:", error);
      setMensajeError("Error al decodificar el token.");
    }
  }, []);

  const cargarCuentas = async (token) => {
    try {
      const respuesta = await axios.get("http://localhost:8080/api/cuentas/recibeSaldo", {
        headers: { Authorization: `Bearer ${token}` },
      });

      const cuentasFiltradas = respuesta.data.filter((cuenta) => cuenta.recibeSaldo);
      setCuentas(cuentasFiltradas);
    } catch (error) {
      console.error("Error al obtener cuentas:", error);
      setMensajeError("Error al obtener cuentas.");
    }
  };

  const handleAddTransaccion = () => {
    setTransacciones([...transacciones, { cuenta: "", tipo: "debe", monto: 0 }]);
  };

  const handleRemoveTransaccion = (index) => {
    const nuevasTransacciones = transacciones.filter((_, i) => i !== index);
    setTransacciones(nuevasTransacciones);
  };

  const handleTransaccionChange = (index, field, value) => {
    const nuevasTransacciones = [...transacciones];
    nuevasTransacciones[index][field] = value;
    setTransacciones(nuevasTransacciones);
  };

  const obtenerFechaActual = () => {
    const fecha = new Date();
    return new Date(fecha.getTime() - fecha.getTimezoneOffset() * 60000).toISOString().split("T")[0]; // YYYY-MM-DD
  };

  const sumarUnDia = (fecha) => {
    const nuevaFecha = new Date(fecha);
    nuevaFecha.setDate(nuevaFecha.getDate() + 1); // Sumar un día
    return nuevaFecha.toISOString().split("T")[0]; // Retorna la fecha en formato YYYY-MM-DD
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const storedToken = localStorage.getItem("token");

    const movimientos = transacciones.map((transaccion) => ({
      cuentaCodigo: transaccion.cuenta,
      debe: transaccion.tipo === "debe" ? transaccion.monto : 0,
      haber: transaccion.tipo === "haber" ? transaccion.monto : 0,
    }));

    if (movimientos.length < 2) {
      setMensajeError("El asiento debe contener al menos dos movimientos.");
      return;
    }

    console.log("Datos a enviar al backend:", { descripcion, movimientos, fecha: fechaActual });

    try {
      const response = await axios.post(
        `http://localhost:8080/api/asientos/crear/${usuario.id}`,
        {
          descripcion,
          movimientos,
          fecha: sumarUnDia(fechaActual), // Suma un día a la fecha antes de enviarla
        },
        {
          headers: {
            Authorization: `Bearer ${storedToken}`,
          },
        }
      );

      console.log("Respuesta del servidor:", response.data);
      if (response.status === 200) {
        setMensajeExito("Asiento registrado con éxito.");
        setDescripcion("");
        setTransacciones([{ cuenta: "", tipo: "debe", monto: 0 }]);
        setMensajeError("");
      }
    } catch (error) {
      console.error("Error al registrar el asiento:", error);
      if (error.response) {
        console.error("Detalles del error:", error.response.data);
        const errorMsg = error.response.data.mensaje || "Error al registrar el asiento.";
        setMensajeError(errorMsg);
      } else {
        setMensajeError("Error al registrar el asiento.");
      }
    }
  };

  return (
    <div>
      <h2>Registrar Asiento Contable</h2>
      {usuario && (
        <div>
          <strong>Usuario:</strong> {usuario.nombre}
        </div>
      )}
      <div>
        <strong>Fecha:</strong> {fechaActual} {/* Muestra la fecha actual guardada en el estado */}
      </div>

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

        <h3>Transacciones</h3>
        {transacciones.map((transaccion, index) => (
          <div key={index} style={{ display: "flex", gap: "10px", marginBottom: "10px" }}>
            <select
              value={transaccion.cuenta}
              onChange={(e) => handleTransaccionChange(index, "cuenta", e.target.value)}
              required
            >
              <option value="">Seleccione una cuenta</option>
              {cuentas.map((cuenta) => (
                <option key={cuenta.id} value={cuenta.codigo}>
                  {cuenta.nombre}
                </option>
              ))}
            </select>

            <select
              value={transaccion.tipo}
              onChange={(e) => handleTransaccionChange(index, "tipo", e.target.value)}
            >
              <option value="debe">Debe</option>
              <option value="haber">Haber</option>
            </select>

            <input
              type="number"
              value={transaccion.monto}
              onChange={(e) => handleTransaccionChange(index, "monto", parseFloat(e.target.value))}
              placeholder="Monto"
              min="0"
              required
            />

            <button type="button" onClick={() => handleRemoveTransaccion(index)}>
              Eliminar
            </button>
          </div>
        ))}
        <button type="button" onClick={handleAddTransaccion}>
          Agregar Transacción
        </button>

        <button type="submit">Registrar Asiento</button>
      </form>

      {mensajeError && <p style={{ color: "red" }}>{mensajeError}</p>}
      {mensajeExito && <p className="exito">{mensajeExito}</p>}
    </div>
  );
};

export default CrearAsiento;
