package es.gabriel.myrmidonai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ConversationDTO {
    private Long id;
    private String title;
    private LocalDateTime startDate;
}
