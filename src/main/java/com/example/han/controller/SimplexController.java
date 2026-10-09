package com.example.han.controller;

import com.example.han.dto.ModeloMatematicoDTO;
import com.example.han.dto.SolucionDTO;

import com.example.han.service.SimplexService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/simplex")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SimplexController {

    private final SimplexService simplexService;

    @PostMapping("/resolver")
    public ResponseEntity<SolucionDTO> resolver(@RequestBody ModeloMatematicoDTO modelo) {
        SolucionDTO solucion = simplexService.resolverModelo(modelo);
        return ResponseEntity.ok(solucion);
    }
}