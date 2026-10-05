# SpringAI-Groq Chatbot API

API REST construida con **Spring Boot** y **Spring AI** que consume modelos de lenguaje (LLM) alojados en **Groq** a través de `ChatClient`. Groq expone una API compatible con OpenAI ChatCompletions, por lo que el proyecto usa el starter de OpenAI de Spring AI cambiando únicamente la `base-url` y la `api-key`.

> Laboratorio **Nivel 2 — Spring AI + Groq** · Desarrollo Web Avanzado · Programa de Ingeniería de Sistemas, Tecnológico Comfenalco.

---

## Tabla de contenido

1. [Tecnologías](#tecnologías)
2. [Estructura del proyecto](#estructura-del-proyecto)
3. [Requisitos previos](#requisitos-previos)
4. [Configuración](#configuración)
5. [Ejecución](#ejecución)
6. [Endpoints](#endpoints)
7. [Cambio de proveedor LLM](#cambio-de-proveedor-llm)
8. [Solución de problemas](#solución-de-problemas)
9. [Observaciones y mejoras](#observaciones-y-mejoras)
10. [Créditos](#créditos)

---

## Tecnologías

| Componente | Detalle |
|---|---|
| Lenguaje | Java (probado con JDK 22) |
| Framework | Spring Boot 3.3.5 |
| IA | Spring AI 1.0.0 (`spring-ai-starter-model-openai`) |
| Proveedor LLM | Groq (API compatible con OpenAI) |
| Modelo | `openai/gpt-oss-120b` |
| Build | Maven |

## Estructura del proyecto

```
nivel2-springai/
├── pom.xml                                  # Spring AI BOM + starter OpenAI
└── src/main/
    ├── java/com/universidad/chatbot/
    │   ├── Nivel2SpringaiApplication.java   # Punto de entrada
    │   ├── config/
    │   │   ├── ChatDtos.java                # Records ChatRequest y ChatResponse
    │   │   └── GlobalExceptionHandler.java  # Manejo de errores amigable
    │   ├── service/
    │   │   └── SpringAiChatService.java     # NÚCLEO: ChatClient de Spring AI
    │   └── controller/
    │       └── SpringAiChatController.java  # Endpoints REST /api/v2/chat
    └── resources/
        └── application.yml                  # base-url de Groq + api-key
```

## Requisitos previos

- **JDK 22** (con el que se probó). Si usas otra versión, ajusta la propiedad `java.version` del `pom.xml`.
- **Maven** (o IntelliJ IDEA con Maven integrado).
- Una **API Key de Groq** (gratuita, sin tarjeta): https://console.groq.com → *API Keys* → *Create API Key*. La clave tiene formato `gsk_...` y Groq la muestra una sola vez.

## Configuración

La API Key **nunca** se escribe en el código ni en `application.yml`: se lee de la variable de entorno `GROQ_API_KEY`.

```yaml
spring:
  ai:
    openai:
      api-key: ${GROQ_API_KEY}
      base-url: https://api.groq.com/openai
      chat:
        options:
          model: openai/gpt-oss-120b
          temperature: 0.7
          max-tokens: 512

server:
  port: 8080
```



### Definir la variable de entorno

**IntelliJ IDEA:** *Run → Edit Configurations → Nivel2SpringaiApplication → Environment variables* y agregar:

```
GROQ_API_KEY=gsk_TU_KEY_AQUI
```

**PowerShell (Windows):**

```powershell
$env:GROQ_API_KEY = "gsk_TU_KEY_AQUI"
```

**Linux / macOS:**

```bash
export GROQ_API_KEY=gsk_TU_KEY_AQUI
```

## Ejecución

Clonar el repositorio y ejecutar:

```bash
git clone https://github.com/<tu-usuario>/springai-groq-chatbot.git
cd springai-groq-chatbot
mvn spring-boot:run
```

O desde IntelliJ IDEA: *File → Open* (carpeta del proyecto) y *Run → Nivel2SpringaiApplication*.

Si todo va bien verás en consola el arranque de Spring Boot, Tomcat en el puerto 8080 y el mensaje de que `SpringAiChatService` fue inicializado.

## Endpoints

Base: `http://localhost:8080/api/v2/chat`

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/v2/chat` | Consulta principal con cuerpo JSON |
| POST | `/api/v2/chat/rapido` | Consulta rápida con parámetros en la URL |
| GET | `/api/v2/chat/salud` | Verificación de estado del servicio |

> En PowerShell usa `curl.exe` (con `.exe`), porque `curl` es un alias de otro comando.

### 1. POST con JSON (principal)

```bash
curl -X POST http://localhost:8080/api/v2/chat \
     -H "Content-Type: application/json" \
     -d '{"pregunta": "¿Qué es Spring Boot en 2 oraciones?", "dominio": "Java"}'
```

Respuesta (estructura):

```json
{
  "respuesta": "Spring Boot es un framework...",
  "modelo": "openai/gpt-oss-120b",
  "dominio": "Java"
}
```

- `pregunta` es obligatoria: si viene vacía, la petición es rechazada.
- `dominio` es opcional: si se omite, se usa el valor por defecto `tecnología`.

### 2. POST con parámetros en la URL

```bash
curl -X POST "http://localhost:8080/api/v2/chat/rapido?pregunta=Explica%20Maven&dominio=Java"
```

### 3. GET de verificación de salud

```bash
curl http://localhost:8080/api/v2/chat/salud
```

Respuesta esperada: `Nivel 2 - Spring AI + Groq activo y listo.`

## Cambio de proveedor LLM

La lógica del servicio (`SpringAiChatService`) no depende del proveedor: según la guía del laboratorio, solo se modifican `pom.xml` y `application.yml`. Este ejercicio se documentó como **análisis teórico** (no se ejecutó por limitaciones de rendimiento del equipo).

### Groq → Ollama (local, sin internet)

`pom.xml`:

```xml
<!-- Quitar: -->
<artifactId>spring-ai-starter-model-openai</artifactId>
<!-- Poner: -->
<artifactId>spring-ai-starter-model-ollama</artifactId>
```

`application.yml`:

```yaml
spring:
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        options:
          model: llama3.2:1b
```

### Groq → OpenAI

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      # base-url: no se pone (usa la de OpenAI por defecto)
      chat:
        options:
          model: gpt-4o-mini
```

## Solución de problemas

| Error | Causa | Solución |
|---|---|---|
| `401 Unauthorized` | API Key inválida o no configurada | Verificar la variable de entorno `GROQ_API_KEY` y reiniciar la aplicación |
| `model_not_found` (404) | Nombre de modelo incorrecto o **modelo retirado por Groq** | Usar un modelo vigente (por ejemplo `openai/gpt-oss-120b`) y revisar https://console.groq.com/docs/deprecations |
| `429 Too Many Requests` | Límite del plan gratuito superado | Esperar unos minutos |
| Puerto 8080 ocupado | Otro proceso usa el puerto | Cambiar `server.port` en `application.yml` |
| Acentos deformados en PowerShell | `Invoke-RestMethod` decodifica la respuesta con otra codificación | Usar `curl.exe` o Postman |

### Nota sobre el modelo

La guía original del laboratorio usaba `llama-3.3-70b-versatile`, pero Groq lo retiró el **16 de agosto de 2026**; por eso este proyecto usa `openai/gpt-oss-120b`. Los nombres de modelos cambian con frecuencia, así que conviene revisar la página de deprecaciones del proveedor.

## Observaciones y mejoras

- Leer el nombre del modelo desde `application.yml` (por ejemplo con `@Value`) en lugar de escribirlo en el código.
- Manejar `HttpMessageNotReadableException` y `NoResourceFoundException` por separado, devolviendo 400 y 404 con mensajes claros.
- Aumentar `max-tokens` (por ejemplo a 1024) si se requieren respuestas completas; con 512 las respuestas largas pueden quedar cortadas.
- Declarar la codificación UTF-8 en las respuestas JSON del controlador.
- Actualizar el `parent` de Spring Boot a 3.4.x o superior, acorde con Spring AI 1.0.x.

## Créditos

- Proyecto base del laboratorio: [fagarra/nivel2-springai](https://github.com/fagarra/nivel2-springai).
- **Docente:** Fabio Garcia.
- **Integrantes:** Owen Meléndez González, Juan Tejada, Harley Cassiani, Harvey SanJuan.

Proyecto con fines académicos · Desarrollo Web Avanzado · Tecnológico Comfenalco, Cartagena de Indias, Colombia.
