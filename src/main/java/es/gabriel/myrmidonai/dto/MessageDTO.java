package es.gabriel.myrmidonai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class MessageDTO {
    private Long id;
    private String sender; // Que será USER o ASSISTANT
    private String content;
    private LocalDateTime timestamp;

}
