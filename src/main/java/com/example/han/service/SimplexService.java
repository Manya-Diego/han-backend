package com.example.han.service;

import com.example.han.dto.ModeloMatematicoDTO;
import com.example.han.dto.RestriccionDTO;
import com.example.han.dto.SimplexIteracionDTO;
import com.example.han.dto.SolucionDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SimplexService {

    public SolucionDTO resolverModelo(ModeloMatematicoDTO modelo) {
        int numVariables = modelo.variables().length;

        int varsHolgura = 0;
        int varsExceso = 0;
        for (RestriccionDTO res : modelo.restricciones()) {
            if (res.signo().equals("<=")) varsHolgura++;
            else if (res.signo().equals(">=")) varsExceso++;
        }
        int numColumnasValidas = numVariables + varsHolgura + varsExceso;

        double[][] tableau = construirTableauInicial(modelo);
        List<SimplexIteracionDTO> historial = new ArrayList<>();
        int iteracion = 1;

        prepararFase1(tableau, modelo);

        boolean factible = resolverFase(tableau, 1, numColumnasValidas, modelo, historial, iteracion, "FASE_1");

        if (!factible || verificarInfactibilidad(tableau)) {
            return new SolucionDTO("INFACTIBLE", 0.0, new double[numVariables], historial);
        }

        iteracion = historial.size() + 1;

        boolean acotado = resolverFase(tableau, 0, numColumnasValidas, modelo, historial, iteracion, "FASE_2");

        if (!acotado) {
            return new SolucionDTO("NO_ACOTADO", 0.0, new double[numVariables], historial);
        }

        return extraerSolucion(tableau, modelo, historial);
    }

    private double[][] construirTableauInicial(ModeloMatematicoDTO modelo) {
        int numRestricciones = modelo.restricciones().size();
        int numVarsOriginales = modelo.variables().length;
        int varsHolgura = 0;
        int varsExceso = 0;
        int varsArtificiales = 0;

        for (RestriccionDTO res : modelo.restricciones()) {
            if (res.signo().equals("<=")) varsHolgura++;
            else if (res.signo().equals(">=")) { varsExceso++; varsArtificiales++; }
            else if (res.signo().equals("=")) varsArtificiales++;
        }

        int numColumnas = numVarsOriginales + varsHolgura + varsExceso + varsArtificiales + 1;
        int numFilas = numRestricciones + 2;
        double[][] tableau = new double[numFilas][numColumnas];

        double multiplicador = modelo.tipoOptimizacion().equalsIgnoreCase("MAXIMIZAR") ? -1.0 : 1.0;
        for (int j = 0; j < numVarsOriginales; j++) {
            tableau[0][j] = modelo.funcionObjetivo()[j] * multiplicador;
        }

        int colHolgura = numVarsOriginales;
        int colExceso = numVarsOriginales + varsHolgura;
        int colArtificial = numVarsOriginales + varsHolgura + varsExceso;

        for (int i = 0; i < numRestricciones; i++) {
            RestriccionDTO res = modelo.restricciones().get(i);
            int fila = i + 2;

            for (int j = 0; j < numVarsOriginales; j++) {
                tableau[fila][j] = res.coeficientes()[j];
            }
            tableau[fila][numColumnas - 1] = res.valorDerecho();

            if (res.signo().equals("<=")) {
                tableau[fila][colHolgura++] = 1.0;
            } else if (res.signo().equals(">=")) {
                tableau[fila][colExceso++] = -1.0;
                tableau[fila][colArtificial] = 1.0;
                tableau[1][colArtificial] = 1.0;
                colArtificial++;
            } else if (res.signo().equals("=")) {
                tableau[fila][colArtificial] = 1.0;
                tableau[1][colArtificial] = 1.0;
                colArtificial++;
            }
        }
        return tableau;
    }

    private void prepararFase1(double[][] tableau, ModeloMatematicoDTO modelo) {
        int numColumnas = tableau[0].length;
        int numRestricciones = modelo.restricciones().size();
        int numVarsOriginales = modelo.variables().length;

        int varsHolgura = 0;
        int varsExceso = 0;
        for (RestriccionDTO res : modelo.restricciones()) {
            if (res.signo().equals("<=")) varsHolgura++;
            else if (res.signo().equals(">=")) varsExceso++;
        }

        int colArtificialInicio = numVarsOriginales + varsHolgura + varsExceso;

        for (int i = 2; i < numRestricciones + 2; i++) {
            boolean tieneArtificial = false;
            for (int j = colArtificialInicio; j < numColumnas - 1; j++) {
                if (tableau[i][j] == 1.0) {
                    tieneArtificial = true;
                    break;
                }
            }
            if (tieneArtificial) {
                for (int j = 0; j < numColumnas; j++) {
                    tableau[1][j] -= tableau[i][j];
                }
            }
        }
    }

    private boolean resolverFase(double[][] tableau, int filaObjetivo, int numColumnasValidas, ModeloMatematicoDTO modelo, List<SimplexIteracionDTO> historial, int iteracion, String fase) {
        int numFilas = tableau.length;
        int numColumnas = tableau[0].length;

        while (true) {
            historial.add(new SimplexIteracionDTO(iteracion++, fase, clonarMatriz(tableau), null, null, "", ""));

            int columnaPivote = -1;
            double valorMasNegativo = -1e-9;

            for (int j = 0; j < numColumnasValidas; j++) {
                if (tableau[filaObjetivo][j] < valorMasNegativo) {
                    valorMasNegativo = tableau[filaObjetivo][j];
                    columnaPivote = j;
                }
            }

            if (columnaPivote == -1) {
                return true;
            }

            int filaPivote = -1;
            double menorProporcion = Double.MAX_VALUE;

            for (int i = 2; i < numFilas; i++) {
                double coeficiente = tableau[i][columnaPivote];
                if (coeficiente > 1e-9) {
                    double proporcion = tableau[i][numColumnas - 1] / coeficiente;
                    if (proporcion < menorProporcion) {
                        menorProporcion = proporcion;
                        filaPivote = i;
                    }
                }
            }

            if (filaPivote == -1) {
                return false;
            }

            int ultimaIteracion = historial.size() - 1;
            SimplexIteracionDTO dtoAnterior = historial.get(ultimaIteracion);

            String nombreEntrante = "x" + (columnaPivote + 1);
            String nombreSaliente = "Fila " + (filaPivote - 1);

            historial.set(ultimaIteracion, new SimplexIteracionDTO(
                    dtoAnterior.numeroIteracion(),
                    dtoAnterior.fase(),
                    dtoAnterior.matrizTableau(),
                    filaPivote,
                    columnaPivote,
                    nombreEntrante,
                    nombreSaliente
            ));

            pivotearMatriz(tableau, filaPivote, columnaPivote);
        }
    }

    private void pivotearMatriz(double[][] tableau, int filaPivote, int columnaPivote) {
        int numFilas = tableau.length;
        int numColumnas = tableau[0].length;
        double elementoPivote = tableau[filaPivote][columnaPivote];

        for (int j = 0; j < numColumnas; j++) {
            tableau[filaPivote][j] /= elementoPivote;
        }

        for (int i = 0; i < numFilas; i++) {
            if (i != filaPivote) {
                double factor = tableau[i][columnaPivote];
                for (int j = 0; j < numColumnas; j++) {
                    tableau[i][j] -= factor * tableau[filaPivote][j];
                }
            }
        }
    }

    private boolean verificarInfactibilidad(double[][] tableau) {
        int numColumnas = tableau[0].length;
        return Math.abs(tableau[1][numColumnas - 1]) > 1e-9;
    }

    private SolucionDTO extraerSolucion(double[][] tableau, ModeloMatematicoDTO modelo, List<SimplexIteracionDTO> historial) {
        int numVariables = modelo.variables().length;
        int numFilas = tableau.length;
        int numColumnas = tableau[0].length;

        double[] valores = new double[numVariables];

        for (int j = 0; j < numVariables; j++) {
            int filaBase = -1;
            boolean esBase = true;

            for (int i = 2; i < numFilas; i++) {
                if (Math.abs(tableau[i][j] - 1.0) < 1e-9) {
                    if (filaBase == -1) filaBase = i;
                    else esBase = false;
                } else if (Math.abs(tableau[i][j]) > 1e-9) {
                    esBase = false;
                }
            }

            if (esBase && filaBase != -1) {
                valores[j] = tableau[filaBase][numColumnas - 1];
            }
        }

        double valorZ = tableau[0][numColumnas - 1];
        if (modelo.tipoOptimizacion().equalsIgnoreCase("MAXIMIZAR")) {
            valorZ = Math.abs(valorZ) < 1e-9 ? 0.0 : valorZ;
        } else {
            valorZ = -valorZ;
        }

        return new SolucionDTO("OPTIMO", valorZ, valores, historial);
    }

    private double[][] clonarMatriz(double[][] original) {
        double[][] copia = new double[original.length][];
        for (int i = 0; i < original.length; i++) {
            copia[i] = original[i].clone();
        }
        return copia;
    }
}