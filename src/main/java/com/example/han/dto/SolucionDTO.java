package com.example.han.dto;

import java.util.List;

public record SolucionDTO(
    String estado,
    double valorOptimoZ,
    double[] valoresVariables,
    List<SimplexIteracionDTO> historialIteraciones
) {}