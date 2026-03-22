package com.sa.contable.servicios;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sa.contable.entidades.Nota;
import com.sa.contable.repositorios.NotaRepositorio;

@Service
public class NotaServicio {

    @Autowired
    private NotaRepositorio notaRepositorio;

    public List<Nota> obtenerTodas() {
        return notaRepositorio.findAll();
    }

    public List<Nota> obtenerPorVenta(Long idVenta) {
        return notaRepositorio.findByIdVenta(idVenta);
    }

    @Transactional
    public Nota crearNota(Nota nota) {
        // Aseguramos que la fecha sea la actual si no viene en el JSON
        if (nota.getFecha() == null) {
            nota.setFecha(LocalDate.now());
        }
        
        // ACÁ IRÍA LA LÓGICA DEL ASIENTO CONTABLE
        // AsientoService.crearAsientoPorNota(nota);
        
        return notaRepositorio.save(nota);
    }
}