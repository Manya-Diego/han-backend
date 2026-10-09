package com.example.han.dto;

public record SimplexIteracionDTO(
    int numeroIteracion,
    String fase,
    double[][] matrizTableau,
    Integer indiceFilaPivote,
    Integer indiceColumnaPivote,
    String variableEntrante,
    String variableSaliente
) {}