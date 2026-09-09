package es.gabriel.myrmidonai.service;

import es.gabriel.myrmidonai.dto.ChatResponse;
import es.gabriel.myrmidonai.model.*;
import es.gabriel.myrmidonai.repository.AuditLogRepository;
import es.gabriel.myrmidonai.repository.ConversationRepository;
import es.gabriel.myrmidonai.repository.MessageRepository;
import es.gabriel.myrmidonai.repository.UserRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final AuditLogRepository auditLogRepository;

    // Inyectamos el ChatClient.Builder (moderno), PGVector y nuestro Repo de usuarios
    public ChatServiceImpl(ChatClient.Builder builder, VectorStore vectorStore, UserRepository userRepository,
                           ConversationRepository conversationRepository, MessageRepository messageRepository,
                           AuditLogRepository auditLogRepository) {
        this.chatClient = builder.build();
        this.vectorStore = vectorStore;
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.auditLogRepository = auditLogRepository;
    }
    @Override
    @Transactional
    public ChatResponse processQuestion(String question, String username, Long conversationId) {

        // primero buscamos al usuario que ha hecho la pregunta
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado en el sistema"));

        // gestion de la conversacion
        Conversation conversation;
        if (conversationId == null) {
            conversation = new Conversation();
            conversation.setUser(user);
            String title = question.length() > 20 ? question.substring(0, 20) + "..." : question;
            conversation.setTitle(title);
            conversation = conversationRepository.save(conversation);
        } else {
            conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new RuntimeException("Conversación no encontrada"));

            if (!conversation.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Acceso denegado a esta conversación");
            }
        }

        // Construimos el filtro de PGVector basado de forma estricta en el Enum
        //Cogemos el rol para ver si tiene los privilegios suficientes para llevar acabo la pregunta
        String filterExpression = getFilterForRole(user.getRole());

        //Busqueda vectorial filtrada
        List<Document> chunks = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(5)
                        .similarityThreshold(0.3) // Para descartar documentos que no se parezcan en nada
                        .filterExpression(filterExpression)
                        .build()
        );

        //Juntamos el texto recuperado (añadimos el titulo del documento para que la IA sepa la fuente)
        String context = chunks.stream()
                .map(doc -> "Fuente: " + doc.getMetadata().get("title") + "\nTexto: " + doc.getText())
                .collect(Collectors.joining("\n\n---\n\n"));

        // Usamos una lista de la clase Message de Spring AI
        List<org.springframework.ai.chat.messages.Message> chatHistory = new java.util.ArrayList<>();

        if (conversationId != null) {
            List<es.gabriel.myrmidonai.model.Message> pastMessages =
                    messageRepository.findTop6ByConversationIdOrderByTimestampAsc(conversationId);

            for (es.gabriel.myrmidonai.model.Message pastMsg : pastMessages) {
                if ("USER".equals(pastMsg.getSender())) {
                    chatHistory.add(new org.springframework.ai.chat.messages.UserMessage(pastMsg.getContent()));
                } else if ("ASSISTANT".equals(pastMsg.getSender())) {
                    chatHistory.add(new org.springframework.ai.chat.messages.AssistantMessage(pastMsg.getContent()));
                }
            }
        }

        //Llamada a la ia (Usando mi ChatClient pero parametrizado)
        String aiResponse = chatClient.prompt()
                .system("Eres el asistente legal corporativo de Myrmidon. " +
                        "Responde de forma profesional basándote UNICA Y EXCLUSIVAMENTE en el contexto proporcionado. " +
                        "Si la respuesta no está en el contexto, indica que no tienes esa información. No inventes datos.")
                .messages(chatHistory)
                .user(u -> u.text("CONTEXTO:\n{contexto}\n\nPREGUNTA:\n{pregunta}")
                        .param("contexto", context)
                        .param("pregunta", question)
                )
                .call()
                .content();

        // Guarda la pregunta del usuario
        Message userMessage = new Message();
        userMessage.setConversation(conversation);
        userMessage.setSender("USER");
        userMessage.setContent(question);
        messageRepository.save(userMessage);
        // Guarda la respuesta de la IA
        Message aiMessage = new Message();
        aiMessage.setConversation(conversation);
        aiMessage.setSender("ASSISTANT");
        aiMessage.setContent(aiResponse);
        messageRepository.save(aiMessage);

        // Registro de auditoría
        AuditLog log = new AuditLog();
        log.setTimestamp(LocalDateTime.now());
        log.setUsername(user.getUsername()); // Usamos tu campo String username
        log.setAction("CHAT_QUERY");
        log.setDetails("Consulta en chat " + conversation.getId() + ". Pregunta: " + question);
        log.setAuthorized(true);
        auditLogRepository.save(log);


        // 7. DEVOLVER DTO
        return new ChatResponse(aiResponse, conversation.getId());
    }


    private String getFilterForRole(Role role) {
        return switch (role) {
            case ROLE_PARTNER -> "security_level in ['PUBLIC', 'INTERNAL', 'CONFIDENTIAL']";
            case ROLE_ASSOCIATE -> "security_level in ['PUBLIC', 'INTERNAL']";
            case ROLE_INTERN -> "security_level in ['PUBLIC']";
            default -> "security_level in ['PUBLIC']";
        };
    }
}
