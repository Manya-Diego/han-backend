package com.example.han.controller;


import com.example.han.dto.ModeloMatematicoDTO;
import com.example.han.service.IaExtractorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ia")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class IaController {

    private final IaExtractorService iaExtractorService;

    @PostMapping("/extraer-modelo")
    public ResponseEntity<ModeloMatematicoDTO> extraer(@RequestBody Map<String, String> payload) {
        ModeloMatematicoDTO modelo = iaExtractorService.extraerModelo(payload.get("textoProblema"));
        return ResponseEntity.ok(modelo);
    }
}