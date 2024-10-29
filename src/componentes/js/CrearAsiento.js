import React, { useState, useEffect } from "react";
import axios from "axios";
import { jwtDecode } from "jwt-decode";

const CrearAsiento = () => {
    const [usuario, setUsuario] = useState(null);
    const [mensajeError, setMensajeError] = useState("");
    const [mensajeExito, setMensajeExito] = useState("");
    const [descripcion, setDescripcion] = useState("");
    const [transacciones, setTransacciones] = useState([{ cuenta: "", tipo: "debe", monto: null }]);
    const [cuentas, setCuentas] = useState([]);
    const [fechaActual, setFechaActual] = useState("");

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
            setFechaActual(obtenerFechaActual());
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
        setTransacciones([...transacciones, { cuenta: "", tipo: "debe", monto: null }]);
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

    const obtenerTipoCuenta = (cuentaCodigo) => {
        const cuenta = cuentas.find(c => c.codigo === cuentaCodigo);
        return cuenta ? cuenta.tipo : null;
    };

    const calcularSaldo = (transacciones) => {
        const saldosProyectados = {};

        if (!transacciones || !Array.isArray(transacciones)) {
            console.error("Transacciones no definidas o no son un arreglo:", transacciones);
            return saldosProyectados;
        }

        transacciones.forEach(({ cuenta, monto, tipo }) => {
            const tipoCuenta = obtenerTipoCuenta(cuenta);

            if (!saldosProyectados[cuenta]) {
                saldosProyectados[cuenta] = 0;
            }

            console.log(`Cuenta: ${cuenta}, Tipo: ${tipo}, TipoCuenta: ${tipoCuenta}, Monto: ${monto}`);

            if (tipo === "debe") {
                if (tipoCuenta === "Activo" || tipoCuenta === "Egreso") {
                    saldosProyectados[cuenta] += monto;
                } else if (tipoCuenta === "Pasivo" || tipoCuenta === "Patrimonio" || tipoCuenta === "Ingreso") {
                    saldosProyectados[cuenta] -= monto;
                }
            } else if (tipo === "haber") {
                if (tipoCuenta === "Activo" || tipoCuenta === "Egreso") {
                    saldosProyectados[cuenta] -= monto;
                } else if (tipoCuenta === "Pasivo" || tipoCuenta === "Patrimonio" || tipoCuenta === "Ingreso") {
                    saldosProyectados[cuenta] += monto;
                }
            }

            console.log(`Saldo proyectado para la cuenta ${cuenta}: ${saldosProyectados[cuenta]}`);
        });

        return saldosProyectados;
    };

    const obtenerFechaActual = () => {
        const fecha = new Date();
        return new Date(fecha.getTime() - fecha.getTimezoneOffset() * 60000).toISOString().split("T")[0];
    };

    const sumarUnDia = (fecha) => {
        const nuevaFecha = new Date(fecha);
        nuevaFecha.setDate(nuevaFecha.getDate() + 1);
        return nuevaFecha.toISOString().split("T")[0];
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        const storedToken = localStorage.getItem("token");
        const movimientos = transacciones.map((transaccion) => {
            const saldo = transaccion.monto;
            return {
                cuentaCodigo: transaccion.cuenta,
                debe: transaccion.tipo === "debe" ? transaccion.monto : 0,
                haber: transaccion.tipo === "haber" ? transaccion.monto : 0,
                saldo: saldo,
            };
        });

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
                    fecha: sumarUnDia(fechaActual),
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
                setTransacciones([{ cuenta: "", tipo: "debe", monto: null }]);
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

    const saldosProyectados = calcularSaldo(transacciones);

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
                <option key={cuenta.codigo} value={cuenta.codigo}>
                  {cuenta.codigo} - {cuenta.nombre}
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
              value={transaccion.monto === null ? "" : transaccion.monto} // Muestra un string vacío si es null
              onChange={(e) => handleTransaccionChange(index, "monto", parseFloat(e.target.value) || null)} // Cambié el valor a null si no se puede parsear
              placeholder="Monto"
              min="0"
              required
            />

            <button type="button" onClick={() => handleRemoveTransaccion(index)}>
              Eliminar
            </button>
            <div>
              <strong>Saldo proyectado: </strong>
              {saldosProyectados[transaccion.cuenta] !== undefined
                ? saldosProyectados[transaccion.cuenta].toFixed(2)
                : "0.00"}
            </div>
          </div>
        ))}
        <button type="button" onClick={handleAddTransaccion}>
          Agregar Transacción
        </button>
        <button type="submit">Registrar Asiento</button>
      </form>

      {mensajeError && <div style={{ color: "red" }}>{mensajeError}</div>}
      {mensajeExito && <div style={{ color: "green" }}>{mensajeExito}</div>}
    </div>
  );
};

export default CrearAsiento;
