package com.example.han.controller;


import com.example.han.dto.ModeloMatematicoDTO;
import com.example.han.dto.ProyectoRequestDTO;
import com.example.han.dto.SolucionDTO;
import com.example.han.entity.EscenarioModelo;
import com.example.han.entity.HistorialResolucion;
import com.example.han.entity.Proyecto;
import com.example.han.service.ProyectoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/proyectos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProyectoController {

    private final ProyectoService proyectoService;

    @PostMapping
    public ResponseEntity<Proyecto> crear(@RequestBody ProyectoRequestDTO request) {
        return ResponseEntity.ok(proyectoService.crearProyecto(request));
    }

    @GetMapping
    public ResponseEntity<List<Proyecto>> listar() {
        return ResponseEntity.ok(proyectoService.listarProyectos());
    }

    @PostMapping("/{id}/escenarios")
    public ResponseEntity<EscenarioModelo> guardarEscenario(
            @PathVariable UUID id,
            @RequestBody ModeloMatematicoDTO modelo,
            @RequestParam(defaultValue = "false") boolean esFinal) {
        return ResponseEntity.ok(proyectoService.guardarEscenario(id, modelo, esFinal));
    }

    @PostMapping("/escenarios/{escenarioId}/resolucion")
    public ResponseEntity<HistorialResolucion> guardarResolucion(
            @PathVariable UUID escenarioId,
            @RequestBody SolucionDTO solucion) {
        return ResponseEntity.ok(proyectoService.guardarResolucion(escenarioId, solucion));
    }
}