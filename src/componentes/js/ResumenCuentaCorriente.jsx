import React, { useState, useEffect } from "react";
import axios from "axios";

const ResumenCuentaCorriente = () => {
  const [clientes, setClientes] = useState([]);
  const [clienteSeleccionado, setClienteSeleccionado] = useState("");
  const [fechaInicio, setFechaInicio] = useState("");
  const [fechaFin, setFechaFin] = useState("");
  const [cuenta, setCuenta] = useState(null);
  const [loading, setLoading] = useState(false);
  const [showAnularConfirm, setShowAnularConfirm] = useState(false);
  const [cobroToAnular, setCobroToAnular] = useState(null);
  const [motivoAnulacion, setMotivoAnulacion] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  useEffect(() => {
    const cargarClientes = async () => {
      try {
        const response = await axios.get("http://localhost:8080/api/clientes");
        setClientes(response.data);
      } catch (err) {
        console.error("Error al traer lista de clientes:", err);
      }
    };
    cargarClientes();

    const hoy = new Date().toISOString().split("T")[0];
    setFechaFin(hoy);
  }, []);

  const obtenerResumen = async () => {
    if (!clienteSeleccionado) {
      alert("Por favor, seleccione un cliente.");
      return;
    }
    setLoading(true);
    setErrorMessage("");
    try {
      const [respuestaDeudas, respuestaCobros] = await Promise.all([
        axios.get(
          `http://localhost:8080/api/clientes/${clienteSeleccionado}/deudas`,
          {
            params: { desde: fechaInicio, hasta: fechaFin },
          }
        ),
        // CORREGIDO: Ahora los params están DENTRO de los argumentos del axios.get
        axios.get(`http://localhost:8080/api/cobros/${clienteSeleccionado}`, {
            params: { desde: fechaInicio, hasta: fechaFin },
        })
      ]);

      const movimientos = [
        ...respuestaDeudas.data.filter(mov => mov.estado !== "ANULADA"),
        ...respuestaCobros.data
          .filter(cobro => !cobro.anulado)
          .map(cobro => ({
            ...cobro,
            tipo: "COBRO"
          }))
      ].sort((a, b) => new Date(a.fecha) - new Date(b.fecha));

      setCuenta(movimientos);
    } catch (error) {
      console.error("Error al traer la cuenta corriente:", error);
      setErrorMessage("Error al cargar la cuenta corriente");
      setCuenta(null);
    } finally {
      setLoading(false);
    }
  };

  const nombreCliente =
    clientes.find((c) => String(c.id) === String(clienteSeleccionado))
      ?.nombre || "";

  // Cálculo de deudas, cobros y saldo neto
  const deudas = cuenta
    ? cuenta
        .filter((mov) => mov.tipo !== "COBRO")
        .reduce((total, mov) => total + mov.monto, 0)
    : 0;

  const cobrosPagados = cuenta
    ? cuenta
        .filter((mov) => mov.tipo === "COBRO")
        .reduce((total, mov) => total + mov.monto, 0)
    : 0;

  const saldoNeto = deudas - cobrosPagados;

  const confirmAnularCobro = (cobro) => {
    setCobroToAnular(cobro);
    setMotivoAnulacion("");
    setShowAnularConfirm(true);
  };

  const cerrarAnulacion = () => {
    setShowAnularConfirm(false);
    setCobroToAnular(null);
    setMotivoAnulacion("");
    setErrorMessage("");
  };

  const handleAnularCobro = async () => {
    if (!cobroToAnular?.id) return;

    const motivo = motivoAnulacion.trim();
    if (!motivo) {
      setErrorMessage("El motivo de anulación es obligatorio.");
      return;
    }

    try {
      await axios.delete(`http://localhost:8080/api/cobros/anular/${cobroToAnular.id}`, {
        data: { motivo },
      });
      cerrarAnulacion();
      setSuccessMessage("Cobro anulado con éxito.");
      setErrorMessage("");
      obtenerResumen();
      
      setTimeout(() => {
        setSuccessMessage("");
      }, 3000);
    } catch (error) {
      console.error("Error al anular cobro:", error);
      let mensajeError = "Error al anular el cobro.";
      if (error.response?.data) {
        mensajeError = typeof error.response.data === "string" ? error.response.data : error.response.data.message || mensajeError;
      }
      setErrorMessage(mensajeError);
    }
  };

  return (
    <div style={{ fontFamily: "Arial, sans-serif", padding: "20px" }}>
      {/* ENCABEZADO */}
      <div style={{ textAlign: "center", marginBottom: "30px" }}>
        <h1
          style={{ color: "#4a90e2", fontSize: "32px", marginBottom: "20px" }}
        >
          Cuenta Corriente de Clientes
        </h1>

        <div
          style={{
            display: "inline-block",
            textAlign: "right",
            backgroundColor: "#f8f9fa",
            padding: "30px",
            borderRadius: "8px",
            boxShadow: "0 2px 4px rgba(0,0,0,0.05)",
          }}
        >
          <div style={{ marginBottom: "15px" }}>
            <label style={{ marginRight: "10px", fontWeight: "bold" }}>
              Seleccionar Cliente:
            </label>
            <select
              value={clienteSeleccionado}
              onChange={(e) => setClienteSeleccionado(e.target.value)}
              style={{
                padding: "8px",
                borderRadius: "4px",
                border: "1px solid #ccc",
                width: "250px",
              }}
            >
              <option value="">Seleccione un cliente</option>
              {clientes.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.nombre}
                </option>
              ))}
            </select>
          </div>

          <div style={{ marginBottom: "15px" }}>
            <label style={{ marginRight: "10px", fontWeight: "bold" }}>
              Fecha Inicio:
            </label>
            <input
              type="date"
              value={fechaInicio}
              onChange={(e) => setFechaInicio(e.target.value)}
              style={{
                padding: "8px",
                borderRadius: "4px",
                border: "1px solid #ccc",
                width: "250px",
              }}
            />
          </div>

          <div style={{ marginBottom: "25px" }}>
            <label style={{ marginRight: "10px", fontWeight: "bold" }}>
              Fecha Fin:
            </label>
            <input
              type="date"
              value={fechaFin}
              onChange={(e) => setFechaFin(e.target.value)}
              style={{
                padding: "8px",
                borderRadius: "4px",
                border: "1px solid #ccc",
                width: "250px",
              }}
            />
          </div>

          <div style={{ textAlign: "right" }}>
            <button
              onClick={obtenerResumen}
              style={{
                backgroundColor: "#007bff",
                color: "white",
                border: "none",
                padding: "12px 25px",
                borderRadius: "4px",
                cursor: "pointer",
                fontSize: "16px",
                fontWeight: "bold",
              }}
            >
              Obtener Estado de Cuenta
            </button>
          </div>
        </div>
      </div>

      {/* TABLA DE RESULTADOS */}
      {loading && (
        <p style={{ textAlign: "center" }}>Cargando movimientos...</p>
      )}

      {!loading && cuenta && (
        <div style={{ maxWidth: "950px", margin: "0 auto", marginTop: "40px" }}>
          <div
            style={{
              display: "flex",
              justifyContent: "space-between",
              borderBottom: "2px solid #4a90e2",
              paddingBottom: "15px",
              marginBottom: "20px",
            }}
          >
            <h2 style={{ margin: 0, color: "#333" }}>
              Movimientos - {nombreCliente}
            </h2>
            <div style={{ textAlign: "right" }}>
              <div style={{ marginBottom: "8px" }}>
                <strong style={{ color: "#d9534f" }}>Deudas: </strong>
                <span
                  style={{
                    fontSize: "16px",
                    fontWeight: "bold",
                    color: "#d9534f",
                  }}
                >
                  $
                  {deudas.toLocaleString("es-AR", {
                    minimumFractionDigits: 2,
                  })}
                </span>
              </div>
              <div style={{ marginBottom: "10px" }}>
                <strong style={{ color: "#28a745" }}>Cobros Pagados: </strong>
                <span
                  style={{
                    fontSize: "16px",
                    fontWeight: "bold",
                    color: "#28a745",
                  }}
                >
                  $
                  {cobrosPagados.toLocaleString("es-AR", {
                    minimumFractionDigits: 2,
                  })}
                </span>
              </div>
              <div style={{ paddingTop: "8px", borderTop: "1px solid #ddd" }}>
                <strong
                  style={{
                    color: saldoNeto > 0 ? "#d9534f" : "#28a745",
                  }}
                >
                  {saldoNeto > 0 ? "Saldo Adeudado: " : "Saldo a Favor: "}
                </strong>
                <span
                  style={{
                    fontSize: "18px",
                    fontWeight: "bold",
                    color: saldoNeto > 0 ? "#d9534f" : "#28a745",
                  }}
                >
                  $
                  {Math.abs(saldoNeto).toLocaleString("es-AR", {
                    minimumFractionDigits: 2,
                  })}
                </span>
              </div>
            </div>
          </div>

          {cuenta.length > 0 ? (
            <table style={{ width: "100%", borderCollapse: "collapse" }}>
              <thead>
                <tr style={{ backgroundColor: "#4a90e2" }}>
                  <th
                    style={{
                      padding: "12px",
                      border: "1px solid #ddd",
                      color: "white",
                      textAlign: "left",
                      width: "120px",
                    }}
                  >
                    Fecha
                  </th>
                  <th
                    style={{
                      padding: "12px",
                      border: "1px solid #ddd",
                      color: "white",
                      textAlign: "left",
                    }}
                  >
                    Detalle / Comprobante
                  </th>
                  <th
                    style={{
                      padding: "12px",
                      border: "1px solid #ddd",
                      color: "white",
                      textAlign: "right",
                      width: "140px",
                    }}
                  >
                    Debe (+)
                  </th>
                  <th
                    style={{
                      padding: "12px",
                      border: "1px solid #ddd",
                      color: "white",
                      textAlign: "right",
                      width: "140px",
                    }}
                  >
                    Haber (-)
                   </th>
                   <th
                     style={{
                       padding: "12px",
                       border: "1px solid #ddd",
                       color: "white",
                       textAlign: "center",
                       width: "80px",
                     }}
                   >
                     Acciones
                   </th>
                 </tr>
              </thead>
              <tbody>
                {cuenta.map((mov, index) => {
                  const esCobro = mov.tipo === "COBRO";
                  return (
                    <tr
                      key={index}
                      style={{
                        borderBottom: "1px solid #ddd",
                        backgroundColor: index % 2 === 0 ? "#fff" : "#f9f9f9",
                      }}
                    >
                      <td style={{ padding: "10px", border: "1px solid #ddd" }}>
                        {new Date(mov.fecha).toLocaleDateString("es-AR")}
                      </td>
                      <td style={{ padding: "10px", border: "1px solid #ddd" }}>
                        {esCobro
                          ? `Recibo de Cobro Nro ${mov.id} (${mov.metodoPago || "EFECTIVO"})`
                          : `Factura de Venta Nro ${mov.numeroComprobante || mov.id}`}
                        {mov.observaciones && (
                          <span
                            style={{
                              display: "block",
                              fontSize: "12px",
                              color: "#777",
                              fontStyle: "italic",
                            }}
                          >
                            Obs: {mov.observaciones}
                          </span>
                        )}
                      </td>
                      <td
                        style={{
                          padding: "10px",
                          border: "1px solid #ddd",
                          textAlign: "right",
                          color: "#d9534f",
                          fontWeight: !esCobro ? "bold" : "normal",
                        }}
                      >
                        {!esCobro ? `$${mov.monto.toFixed(2)}` : "-"}
                      </td>
                      <td
                        style={{
                          padding: "10px",
                          border: "1px solid #ddd",
                          textAlign: "right",
                          color: "#28a745",
                          fontWeight: esCobro ? "bold" : "normal",
                        }}
                      >
                        {esCobro ? `$${mov.monto.toFixed(2)}` : "-"}
                      </td>
                       <td
                         style={{
                           padding: "10px",
                           border: "1px solid #ddd",
                           textAlign: "center",
                         }}
                       >
                         {esCobro && (
                           <button
                              onClick={() => confirmAnularCobro(mov)}
                             style={{
                               backgroundColor: "#dc3545",
                               color: "white",
                               border: "none",
                               padding: "6px 12px",
                               borderRadius: "4px",
                               cursor: "pointer",
                               fontSize: "12px",
                               fontWeight: "bold",
                               opacity: 1,
                             }}
                           >
                              Anular
                            </button>
                          )}
                       </td>
                       </tr>
                  );
                })}
              </tbody>
            </table>
          ) : (
            <p
              style={{
                color: "#777",
                textAlign: "center",
                marginTop: "20px",
                fontStyle: "italic",
              }}
            >
              Este cliente no registra movimientos en el rango de fechas
              seleccionado.
            </p>
          )}
        </div>
      )}

      {/* Alertas Flotantes */}
      {successMessage && (
        <div style={{ position: 'fixed', bottom: '20px', right: '20px', zIndex: 1300, backgroundColor: '#d1e7dd', color: '#0f5132', padding: '10px 14px', borderRadius: '6px', border: '1px solid #badbcc' }}>
          {successMessage}
        </div>
      )}

      {errorMessage && (
        <div style={{ position: 'fixed', bottom: '20px', right: '20px', zIndex: 1300, backgroundColor: '#f8d7da', color: '#842029', padding: '10px 14px', borderRadius: '6px', border: '1px solid #f5c2c7', boxShadow: '0px 4px 6px rgba(0,0,0,0.1)' }}>
          <strong>Error: </strong> {errorMessage}
        </div>
      )}

      {/* Modal de confirmación para anulación */}
      {showAnularConfirm && (
        <div style={{ position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', backgroundColor: 'rgba(0, 0, 0, 0.5)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1100 }}>
          <div style={{ backgroundColor: '#fff', padding: '18px', borderRadius: '8px', width: '90%', maxWidth: '480px' }}>
            <h3 style={{ marginTop: 0 }}>Confirmar anulación de cobro</h3>
            <p>¿Estás seguro de que deseas anular el cobro Nro {cobroToAnular?.id}?</p>
            <div style={{ marginBottom: '12px' }}>
              <label htmlFor="motivo-anulacion"><strong>Motivo de la anulación</strong></label>
              <textarea
                id="motivo-anulacion"
                value={motivoAnulacion}
                onChange={(event) => setMotivoAnulacion(event.target.value)}
                rows={4}
                placeholder="Explicá por qué se anula el cobro"
                style={{ width: '100%', marginTop: '8px', padding: '10px', resize: 'vertical' }}
              />
            </div>
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px' }}>
              <button type="button" onClick={cerrarAnulacion}>No</button>
              <button type="button" onClick={handleAnularCobro} disabled={!motivoAnulacion.trim()}>Sí, anular</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ResumenCuentaCorriente;