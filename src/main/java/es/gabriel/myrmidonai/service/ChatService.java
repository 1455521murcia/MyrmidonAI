package es.gabriel.myrmidonai.service;

import es.gabriel.myrmidonai.dto.ChatResponse;
import org.springframework.transaction.annotation.Transactional;

public interface ChatService {

    //va a procesar la pregunta de un usuario, lo busca en la base de datos(la vectorial) y ahí genera la respuesta con la IA
    ChatResponse processQuestion(String question, String username, Long conversationId);
}
