package es.gabriel.myrmidonai.service;

import es.gabriel.myrmidonai.model.Role;
import es.gabriel.myrmidonai.model.User;
import es.gabriel.myrmidonai.repository.UserRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
@Service
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final UserRepository userRepository;

    // Inyectamos el ChatClient.Builder (moderno), PGVector y nuestro Repo de usuarios
    public ChatServiceImpl(ChatClient.Builder builder, VectorStore vectorStore, UserRepository userRepository) {
        this.chatClient = builder.build();
        this.vectorStore = vectorStore;
        this.userRepository = userRepository;
    }

    @Override
    public String processQuestion(String message, String username) {

        // primero buscamos al usuario que ha hecho la pregunta
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado en el sistema"));

        // Construimos el filtro de PGVector basado de forma estricta en el Enum
        //Cogemos el rol para ver si tiene los privilegios suficientes para llevar acabo la pregunta
        String filterExpression = getFilterForRole(user.getRole());

        //Busqueda vectorial filtrada
        List<Document> chunks = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(message)
                        .topK(5)
                        .similarityThreshold(0.3) // Para descartar documentos que no se parezcan en nada
                        .filterExpression(filterExpression)
                        .build()
        );

        //Juntamos el texto recuperado (añadimos el titulo del documento para que la IA sepa la fuente)
        String context = chunks.stream()
                .map(doc -> "Fuente: " + doc.getMetadata().get("title") + "\nTexto: " + doc.getText())
                .collect(Collectors.joining("\n\n---\n\n"));

        //LLAMADA A LA IA (Usando mi ChatClient pero parametrizado)
        return chatClient.prompt()
                .system("Eres el asistente legal corporativo de Myrmidon. " +
                        "Responde de forma profesional basándote UNICA Y EXCLUSIVAMENTE en el contexto proporcionado. " +
                        "Si la respuesta no está en el contexto, indica que no tienes esa información. No inventes datos.")
                .user(u -> u.text("CONTEXTO RECUPERADO:\n{contexto}\n\nPREGUNTA DEL USUARIO:\n{pregunta}")
                        .param("contexto", context)
                        .param("pregunta", message)
                )
                .call()
                .content();
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
