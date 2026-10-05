package com.universidad.chatbot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * ─────────────────────────────────────────────────────────────────
 *  SpringAiChatService  —  NÚCLEO DEL NIVEL 2
 * ─────────────────────────────────────────────────────────────────
 *
 *  Este servicio reemplaza la llamada manual a la API REST de Groq
 *  (Nivel 1) con la abstracción de Spring AI: ChatClient.
 *
 *  DIFERENCIA CLAVE respecto al Nivel 1:
 *    - Nivel 1: WebClient → HTTP manual → parseo JSON a mano
 *    - Nivel 2: ChatClient → abstracción → mismo código para cualquier LLM
 *
 *  Spring AI auto-configura ChatClient.Builder gracias a la dependencia
 *  spring-ai-starter-model-openai y la configuración en application.yml.
 *  No necesitamos crear el bean manualmente.
 *
 *  NOTA Java 24: sin Lombok. Logger declarado explícitamente con SLF4J.
 */
@Service
public class SpringAiChatService {

    // Sin @Slf4j de Lombok → declaramos el logger explícitamente
    private static final Logger log = LoggerFactory.getLogger(SpringAiChatService.class);

    // ChatClient es la pieza central de Spring AI.
    // Funciona como un "cliente universal" de LLMs.
    private final ChatClient chatClient;

    // ── Constructor: Spring inyecta el ChatClient.Builder automáticamente ──
    //
    // El Builder viene preconfigurado con los valores de application.yml:
    //   - base-url:    https://api.groq.com/openai
    //   - api-key:     ${GROQ_API_KEY}
    //   - model:       openai/gpt-oss-120b
    //   - temperature: 0.7
    //
    // Usamos el Builder para agregar instrucciones de sistema (system prompt)
    // que siempre estarán presentes en cada conversación.
    public SpringAiChatService(ChatClient.Builder builder) {
        this.chatClient = builder
                // defaultSystem: prompt que el LLM siempre recibe como contexto.
                // {dominio} es un placeholder que se rellena en cada llamada.
                .defaultSystem("""
                        Eres un asistente experto en {dominio}.
                        Responde siempre en español, de forma clara y concisa.
                        Si no conoces la respuesta, dilo honestamente.
                        No inventes información.
                        """)
                .build();

        log.info("✅ SpringAiChatService inicializado con modelo openai/gpt-oss-120b (Groq)");
    }

    /**
     * Envía una pregunta al LLM con un contexto de dominio específico.
     *
     * @param pregunta  Texto del usuario.
     * @param dominio   Contexto temático (ej: "Java", "bases de datos", "Spring Boot")
     * @return          Respuesta generada por el LLM como String.
     */
    public String chat(String pregunta, String dominio) {
        log.info("📨 Nueva pregunta | dominio={} | pregunta={}", dominio, pregunta);

        String respuesta = chatClient.prompt()
                // system(): sobrescribe el placeholder {dominio} para esta llamada concreta
                .system(s -> s.param("dominio", dominio))
                // user(): el mensaje que el usuario envía al LLM
                .user(pregunta)
                // call(): ejecuta la llamada al modelo (bloqueante)
                .call()
                // content(): extrae el texto de la respuesta
                .content();

        log.info("✅ Respuesta recibida | {} chars", respuesta.length());
        return respuesta;
    }
}
