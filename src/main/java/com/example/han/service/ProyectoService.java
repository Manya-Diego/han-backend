package com.example.han.service;

import com.example.han.dto.ModeloMatematicoDTO;
import com.example.han.dto.ProyectoRequestDTO;
import com.example.han.dto.SolucionDTO;
import com.example.han.entity.EscenarioModelo;
import com.example.han.entity.HistorialResolucion;
import com.example.han.entity.Proyecto;
import com.example.han.repository.EscenarioModeloRepository;
import com.example.han.repository.HistorialResolucionRepository;
import com.example.han.repository.ProyectoRepository;
import tools.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProyectoService {

    private final ProyectoRepository proyectoRepository;
    private final EscenarioModeloRepository escenarioRepository;
    private final HistorialResolucionRepository historialRepository;
    private final ObjectMapper objectMapper;

    public Proyecto crearProyecto(ProyectoRequestDTO request) {
        Proyecto proyecto = Proyecto.builder()
                .titulo(request.titulo())
                .descripcionOriginal(request.descripcionOriginal())
                .build();
        return proyectoRepository.save(proyecto);
    }

    public List<Proyecto> listarProyectos() {
        return proyectoRepository.findAll();
    }

    public EscenarioModelo guardarEscenario(UUID proyectoId, ModeloMatematicoDTO modeloDTO, boolean esFinal) {
        try {
            Proyecto proyecto = proyectoRepository.findById(proyectoId)
                    .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));

            EscenarioModelo escenario = EscenarioModelo.builder()
                    .proyecto(proyecto)
                    .modeloJson(objectMapper.writeValueAsString(modeloDTO))
                    .esVersionFinal(esFinal)
                    .build();

            return escenarioRepository.save(escenario);
        } catch (Exception e) {
            throw new RuntimeException("Error procesando JSON", e);
        }
    }

    public HistorialResolucion guardarResolucion(UUID escenarioId, SolucionDTO solucionDTO) {
        try {
            EscenarioModelo escenario = escenarioRepository.findById(escenarioId)
                    .orElseThrow(() -> new RuntimeException("Escenario no encontrado"));

            HistorialResolucion historial = HistorialResolucion.builder()
                    .escenario(escenario)
                    .estadoFinal(solucionDTO.estado())
                    .valorZ(solucionDTO.valorOptimoZ())
                    .iteracionesJson(objectMapper.writeValueAsString(solucionDTO.historialIteraciones()))
                    .build();

            return historialRepository.save(historial);
        } catch (Exception e) {
            throw new RuntimeException("Error procesando JSON", e);
        }
    }
}