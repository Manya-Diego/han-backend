package com.example.han.service;

import com.example.han.dto.ModeloMatematicoDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Service
public class IaExtractorService {

    @Value("${groq.api.key:missing_key}")
    private String apiKey;

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public IaExtractorService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    public ModeloMatematicoDTO extraerModelo(String textoProblema) {
        try {
            String prompt = """
                Eres un experto en Investigación de Operaciones. Extrae el modelo matemático del siguiente problema.
                Debes responder estrictamente con un objeto JSON válido que tenga esta estructura exacta:
                {
                  "tipoOptimizacion": "MAXIMIZAR",
                  "variables": ["x1", "x2"],
                  "funcionObjetivo": [30.0, 50.0],
                  "restricciones": [
                    { "coeficientes": [1.0, 2.0], "signo": "<=", "valorDerecho": 12.0 }
                  ]
                }
                
                Problema del usuario:
                %s
                """.formatted(textoProblema);

            Map<String, Object> requestBody = Map.of(
                    "model", "openai/gpt-oss-20b",
                    "messages", List.of(
                            Map.of("role", "user", "content", prompt)
                    ),
                    "response_format", Map.of("type", "json_object")
            );

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            URI targetUri = UriComponentsBuilder.fromUriString("https://api.groq.com/openai/v1/chat/completions")
                    .build()
                    .toUri();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(targetUri)
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey.trim())
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Error de la API de Groq: " + response.body());
            }

            JsonNode rootNode = objectMapper.readTree(response.body());
            String jsonGenerado = rootNode
                    .path("choices").get(0)
                    .path("message")
                    .path("content").asText();

            return objectMapper.readValue(jsonGenerado, ModeloMatematicoDTO.class);

        } catch (Exception e) {
            throw new RuntimeException("Fallo al procesar el modelo con IA: " + e.getMessage(), e);
        }
    }
}