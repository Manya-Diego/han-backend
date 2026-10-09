package com.example.han.dto;

public record RestriccionDTO(
    double[] coeficientes,
    String signo,
    double valorDerecho
) {}