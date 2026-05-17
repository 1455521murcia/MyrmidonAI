package es.gabriel.myrmidonai.controller;

import es.gabriel.myrmidonai.dto.ChatRequest;
import es.gabriel.myrmidonai.dto.ChatResponse;
import es.gabriel.myrmidonai.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/ask")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChatResponse> askQuestion(@RequestBody ChatRequest request, Principal principal) {

        // Valido si no está vacio
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new ChatResponse("El mensaje no puede estar vacío."));
        }

        try {

            String respuestaIA = chatService.processQuestion(request.getMessage(), principal.getName());

            // Devolvemos la respuesta de la IA en nuestro DTO
            return ResponseEntity.ok(new ChatResponse(respuestaIA));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ChatResponse("Error interno al procesar la pregunta."));
        }
    }
}