package com.sa.contable.servicios;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sa.contable.dto.ClienteDTO;
import com.sa.contable.entidades.Cliente;
import com.sa.contable.repositorios.ClienteRepository;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

        
    public List<ClienteDTO> obtenerTodos() {
        System.out.println("--- Petición recibida en obtenerTodos() ---");

        // 1. Buscamos en la base de datos y logueamos cuántos encuentra
        var clientesBD = clienteRepository.findAll();
        System.out.println("Clientes encontrados en la Base de Datos: " + clientesBD.size());

        // 2. Mapeamos y logueamos cada cliente que se está convirtiendo a DTO
        List<ClienteDTO> clientesDTO = clientesBD.stream()
            .map(cliente -> {
                System.out.println("Mapeando cliente -> ID: " + cliente.getId() + ", Nombre: " + cliente.getNombre());
                return new ClienteDTO(cliente.getId(), cliente.getNombre());
            })
            .toList();

        System.out.println("--- Fin de obtenerTodos() - Enviando " + clientesDTO.size() + " clientes al frontend ---");

        // 3. Retornamos la lista final
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
        clienteRepository.deleteById(id);
    }

    public void clienteExistente(String mail, String nombre, String telefono) {
        List<Cliente> clientes = clienteRepository.findAll();
        for (Cliente cliente : clientes) {
            if (cliente.getMail().equals(mail))
                throw new IllegalArgumentException("El mail ya está registrado");
            else if (cliente.getNombre().equals(nombre))
                throw new IllegalArgumentException("El nombre ya está registrado");
            else if (cliente.getTelefono().equals(telefono))
                throw new IllegalArgumentException("El teléfono ya está registrado");
            }
        }
    }

    