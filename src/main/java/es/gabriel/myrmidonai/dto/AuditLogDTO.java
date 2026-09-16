package es.gabriel.myrmidonai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AuditLogDTO {

    private Long id;
    private LocalDateTime timestamp;
    private String username;
    private String action;
    private String details;

}
