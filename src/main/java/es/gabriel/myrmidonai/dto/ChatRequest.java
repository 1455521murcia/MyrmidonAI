package es.gabriel.myrmidonai.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChatRequest {
    private String message;
    private Long conversationId; // Devuelve el ID del chat actual
}
