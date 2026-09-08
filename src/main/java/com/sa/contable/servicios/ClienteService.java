package com.sa.contable.servicios;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sa.contable.DTO.ClienteDTO;
import com.sa.contable.DTO.MovimientoCuentaDTO;
import com.sa.contable.entidades.Cliente;
import com.sa.contable.entidades.Venta;
import com.sa.contable.repositorios.ClienteRepository;
import com.sa.contable.repositorios.VentaRepositorio;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private VentaRepositorio ventaRepositorio;

    public List<ClienteDTO> obtenerTodos() {
        System.out.println("--- Petición recibida en obtenerTodos() ---");

        var clientesBD = clienteRepository.findAll();
        System.out.println("Clientes encontrados en la Base de Datos: " + clientesBD.size());

        List<ClienteDTO> clientesDTO = clientesBD.stream()
                .map(cliente -> {
                    System.out.println("Mapeando cliente -> ID: " + cliente.getId() + ", Nombre: " + cliente.getNombre());
                    return new ClienteDTO(cliente.getId(), cliente.getNombre());
                })
                .toList();

        System.out.println("--- Fin de obtenerTodos() - Enviando " + clientesDTO.size() + " clientes al frontend ---");

        return clientesDTO;
    }

    public Optional<Cliente> obtenerPorId(Long id) {
        return clienteRepository.findById(id);
    }

    public Cliente registrar(Cliente cliente) {
        clienteExistente(cliente.getMail(), cliente.getNombre(), cliente.getTelefono());
        return clienteRepository.save(cliente);
    }

    public Cliente actualizar(Long id, Cliente clienteActualizado) {
        return clienteRepository.findById(id)
                .map(cliente -> {
                    cliente.setNombre(clienteActualizado.getNombre());
                    cliente.setMail(clienteActualizado.getMail());
                    cliente.setTelefono(clienteActualizado.getTelefono());
                    return clienteRepository.save(cliente);
                })
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
    }

    public void eliminar(Long id) {
        // CORREGIDO: Usamos el repositorio inyectado directamente en lugar de instanciar el controlador
        List<Venta> ventasDelCliente = ventaRepositorio.findByClienteId(id);
        if (!ventasDelCliente.isEmpty()) {
            throw new RuntimeException("No se puede eliminar el cliente porque tiene ventas asociadas");
        }
        clienteRepository.deleteById(id);
    }

    public void clienteExistente(String mail, String nombre, String telefono) {
        List<Cliente> clientes = clienteRepository.findAll();
        for (Cliente cliente : clientes) {
            if (cliente.getMail().equals(mail)) {
                throw new IllegalArgumentException("El mail ya está registrado"); 
            }else if (cliente.getNombre().equals(nombre)) {
                throw new IllegalArgumentException("El nombre ya está registrado"); 
            }else if (cliente.getTelefono().equals(telefono)) {
                throw new IllegalArgumentException("El teléfono ya está registrado");
            }
        }
    }

    public List<MovimientoCuentaDTO> obtenerDeudasPorCliente(Long id, String desdeStr, String hastaStr) {
    LocalDateTime desde = (desdeStr != null && !desdeStr.trim().isEmpty()) 
            ? LocalDate.parse(desdeStr).atStartOfDay() 
            : LocalDateTime.of(2000, 1, 1, 0, 0);

    LocalDateTime hasta = (hastaStr != null && !hastaStr.trim().isEmpty()) 
            ? LocalDate.parse(hastaStr).atTime(LocalTime.MAX) 
            : LocalDate.now().atTime(LocalTime.MAX);

    return ventaRepositorio.findVentasPendientesByClienteId(id, desde, hasta);
    }
}
