package com.example.han.dto;

import java.util.List;

public record ModeloMatematicoDTO(
    String tipoOptimizacion,
    String[] variables,
    double[] funcionObjetivo,
    List<RestriccionDTO> restricciones
) {}