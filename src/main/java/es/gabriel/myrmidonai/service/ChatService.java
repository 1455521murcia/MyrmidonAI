package es.gabriel.myrmidonai.service;

public interface ChatService {

    //va a procesar la pregunta de un usuario, lo busca en la base de datos(la vectorial) y ahí genera la respuesta con la IA
    String processQuestion(String question, String username);
}
