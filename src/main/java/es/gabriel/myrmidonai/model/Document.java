package es.gabriel.myrmidonai.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private String filePath;
    private LocalDateTime uploadDate;

    @Enumerated(EnumType.STRING)
    private SecurityLevel securityLevel;

    @ManyToOne
    private User owner;
}
