package com.universidad.chatbot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Manejo global de errores de la API.
 *
 * Si Groq no responde o la API Key es inválida, este handler
 * captura la excepción y devuelve un JSON de error amigable
 * en lugar de un stack trace.
 *
 * NOTA Java 24: sin @Slf4j de Lombok. Logger explícito con SLF4J.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Captura errores de conexión con Groq y cualquier excepción no controlada.
     *
     * Causas comunes:
     *   - "401 Unauthorized"  → API Key inválida o variable de entorno no configurada
     *   - "429 Too Many Reqs" → Límite del free tier de Groq superado
     *   - "model not found"   → Nombre de modelo incorrecto en application.yml
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception ex) {
        log.error("❌ Error en el chatbot: {}", ex.getMessage(), ex);

        String mensajeAmigable = obtenerMensajeAmigable(ex.getMessage());

        Map<String, Object> error = Map.of(
                "error", true,
                "mensaje", mensajeAmigable,
                "detalle", ex.getMessage() != null ? ex.getMessage() : "Error desconocido",
                "timestamp", LocalDateTime.now().toString()
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
        log.warn("⚠️ Petición inválida: {}", ex.getMessage());

        return ResponseEntity.badRequest().body(Map.of(
                "error", true,
                "mensaje", ex.getMessage(),
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    private String obtenerMensajeAmigable(String msg) {
        if (msg == null) return "Error desconocido en el servicio.";

        if (msg.contains("401") || msg.contains("Unauthorized")) {
            return "API Key inválida. Verificar la variable de entorno GROQ_API_KEY " +
                   "en Run → Edit Configurations → Environment Variables.";
        }
        if (msg.contains("429")) {
            return "Límite de peticiones de Groq alcanzado. Espera unos minutos e intenta de nuevo.";
        }
        if (msg.contains("model") && msg.contains("not found")) {
            return "Modelo no encontrado. Verificar el nombre en application.yml: openai/gpt-oss-120b";
        }
        if (msg.contains("Connection refused") || msg.contains("ConnectException")) {
            return "No se puede conectar con Groq. Verificar conexión a internet.";
        }
        return "Error en el servicio de chat. Revisa los logs para más detalles.";
    }
}
