package com.universidad.chatbot.controller;

import com.universidad.chatbot.config.ChatDtos.ChatRequest;
import com.universidad.chatbot.config.ChatDtos.ChatResponse;
import com.universidad.chatbot.service.SpringAiChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ─────────────────────────────────────────────────────────────────
 *  SpringAiChatController  —  Endpoints REST del Chatbot (Nivel 2)
 * ─────────────────────────────────────────────────────────────────
 *
 *  Expone tres endpoints:
 *    POST /api/v2/chat          → recibe JSON con pregunta y dominio
 *    POST /api/v2/chat/rapido   → parámetros en la URL (fácil con curl)
 *    GET  /api/v2/chat/salud    → verificación de que el servicio está activo
 *
 *  NOTA Java 24: sin @RequiredArgsConstructor ni @Slf4j de Lombok.
 *  Constructor y logger declarados explícitamente.
 */
@RestController
@RequestMapping("/api/v2/chat")
public class SpringAiChatController {

    private static final Logger log = LoggerFactory.getLogger(SpringAiChatController.class);

    private final SpringAiChatService chatService;

    // Sin @RequiredArgsConstructor → constructor explícito
    public SpringAiChatController(SpringAiChatService chatService) {
        this.chatService = chatService;
    }

    // ──────────────────────────────────────────────────────────────
    //  ENDPOINT 1: Recibe JSON en el cuerpo de la petición
    //
    //  curl -X POST http://localhost:8080/api/v2/chat \
    //       -H "Content-Type: application/json" \
    //       -d '{"pregunta": "¿Qué es Spring AI?", "dominio": "Java"}'
    // ──────────────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        log.info("🌐 POST /api/v2/chat | dominio={}", request.dominio());

        String respuesta = chatService.chat(request.pregunta(), request.dominio());

        return ResponseEntity.ok(new ChatResponse(respuesta, "openai/gpt-oss-120b", request.dominio()));
    }

    // ──────────────────────────────────────────────────────────────
    //  ENDPOINT 2: Parámetros en la URL (conveniente para pruebas)
    //
    //  curl -X POST "http://localhost:8080/api/v2/chat/rapido?\
    //       pregunta=Explica%20Maven&dominio=Java"
    // ──────────────────────────────────────────────────────────────
    @PostMapping("/rapido")
    public ResponseEntity<ChatResponse> chatRapido(
            @RequestParam String pregunta,
            @RequestParam(defaultValue = "tecnología") String dominio) {

        log.info("🌐 POST /api/v2/chat/rapido | dominio={}", dominio);

        String respuesta = chatService.chat(pregunta, dominio);

        return ResponseEntity.ok(new ChatResponse(respuesta, "openai/gpt-oss-120b", dominio));
    }

    // ──────────────────────────────────────────────────────────────
    //  ENDPOINT 3: GET simple para verificar que el servicio funciona
    //
    //  curl http://localhost:8080/api/v2/chat/salud
    // ──────────────────────────────────────────────────────────────
    @GetMapping("/salud")
    public ResponseEntity<String> salud() {
        return ResponseEntity.ok("✅ Nivel 2 - Spring AI + Groq activo y listo.");
    }
}
