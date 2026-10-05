package com.universidad.chatbot;

import com.universidad.chatbot.service.SpringAiChatService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests básicos del Nivel 2.
 *
 * NOTA: Estos tests usan @MockitoBean para simular ChatClient.Builder
 * y así no requerir que Ollama esté corriendo al ejecutar los tests.
 *
 * Para un test de integración real (con Ollama corriendo), ver:
 *   ChatbotIntegrationTest.java (comentado abajo)
 */
@SpringBootTest
class Nivel2SpringaiApplicationTests {

    // Simula el Builder de ChatClient para no necesitar Ollama en los tests
    @MockitoBean
    private ChatClient.Builder chatClientBuilder;

    @Test
    void contextLoads() {
        // Verifica que el contexto de Spring Boot carga correctamente
        // con todas las clases: Service, Controller, ExceptionHandler
    }

    @Test
    void serviceSeInstancia() {
        // Verifica que Spring puede instanciar el servicio
        ChatClient mockClient = org.mockito.Mockito.mock(ChatClient.class);
        org.mockito.Mockito.when(chatClientBuilder.defaultSystem(
                org.mockito.ArgumentMatchers.anyString())).thenReturn(chatClientBuilder);
        org.mockito.Mockito.when(chatClientBuilder.build()).thenReturn(mockClient);

        SpringAiChatService service = new SpringAiChatService(chatClientBuilder);
        assertNotNull(service, "El servicio debe instanciarse correctamente");
    }
}
