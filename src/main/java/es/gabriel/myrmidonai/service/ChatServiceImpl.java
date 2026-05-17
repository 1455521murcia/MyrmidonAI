package es.gabriel.myrmidonai.service;

import org.springframework.stereotype.Service;

@Service
public class ChatServiceImpl implements ChatService {

    @Override
    public String processQuestion(String question, String username) {
        return "Hola " + username + ", soy la IA y voy a responder a tu pregunta: " + question ;
    }
}
