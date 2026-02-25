package com.sa.contable.servicios;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sa.contable.entidades.Cliente;
import com.sa.contable.repositorios.ClienteRepository;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    public List<Cliente> obtenerTodos() {
        return clienteRepository.findAll();
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

    