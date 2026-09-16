package es.gabriel.myrmidonai.controller;

import es.gabriel.myrmidonai.dto.ChatRequest;
import es.gabriel.myrmidonai.dto.ChatResponse;
import es.gabriel.myrmidonai.dto.ConversationDTO;
import es.gabriel.myrmidonai.dto.MessageDTO;
import es.gabriel.myrmidonai.model.Conversation;
import es.gabriel.myrmidonai.model.Message;
import es.gabriel.myrmidonai.model.User;
import es.gabriel.myrmidonai.repository.ConversationRepository;
import es.gabriel.myrmidonai.repository.MessageRepository;
import es.gabriel.myrmidonai.repository.UserRepository;
import es.gabriel.myrmidonai.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.user.UserDestinationResolver;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    public ChatController(ChatService chatService, ConversationRepository conversationRepository, UserRepository userRepository, MessageRepository messageRepository) {
        this.chatService = chatService;
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
    }

    @PostMapping("/ask")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChatResponse> askQuestion(@RequestBody ChatRequest request, Principal principal) {

        // Valido si no está vacio
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new ChatResponse("El mensaje no puede estar vacío.", null));
        }
        try {
            // Delegamos la lógica al servicio
            // Pasamos: la pregunta, el nombre del usuario logueado y el ID del chat (puede ser null)
            ChatResponse response = chatService.processQuestion(
                    request.getMessage(),
                    principal.getName(),
                    request.getConversationId()
            );

            // Devolvemos el DTO completo con estado 200 OK
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // Manejo de errores (importante para que no colapse el servidor)
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ChatResponse("Error interno al procesar la pregunta: " + e.getMessage(), null));
        }
    }

    //Devuelve la lista de chats del usuario para la barra lateral

    @GetMapping("/conversations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ConversationDTO>> getUserConversations(Principal principal){

        //Hacemos comprobación del ID del usuario
        User user = userRepository.findUserByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Buscamos las conversaciones en la base de datos
        List<Conversation> convs =
                conversationRepository.findByUserIdOrderByLastUpdatedDesc(user.getId());

        //Y lo convierto a DTOs para el frontend
        List<ConversationDTO> dtos = convs.stream()
                .map(conversation -> new ConversationDTO(conversation.getId(),conversation.getTitle(),conversation.getStartDate()))
                .toList();
        return ResponseEntity.ok(dtos);
    }

    // Devuelve todos los mensajes de un chat especifico
    @GetMapping("/conversations/{id}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MessageDTO>> getConversationMessages(@PathVariable Long id,Principal principal){

        //Verificamos si la conversación existe y es de este usuario
        Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chat no encontrado"));

        if (!conversation.getUser().getUsername().equals(principal.getName())){
            return ResponseEntity.status(403).build(); // Prohibido para que no se puedan ver los chats de otro usuario que no sea el propio
        }

        //Buscamos los mensajes de esa conversación
        List<Message> messages = messageRepository.findTop6ByConversationIdOrderByTimestampAsc(id);

        // Y los convertimos a DTOs
        List<MessageDTO> dtos = messages.stream()
                .map(m -> new MessageDTO(m.getId(),m.getSender(),m.getContent(),m.getTimestamp()))
                .toList();
        return ResponseEntity.ok(dtos);
    }

}