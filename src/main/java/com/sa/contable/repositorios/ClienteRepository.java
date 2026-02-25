package com.sa.contable.repositorios;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sa.contable.entidades.Cliente;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    // Podés agregar métodos personalizados si necesitás (por ejemplo, buscar por mail)
    
}
