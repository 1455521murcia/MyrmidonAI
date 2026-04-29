package es.gabriel.myrmidonai.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime timestamp;
    private String username;
    private String action; // "Tipos de acciones: Consulta , Subida de archivo,acceso denegado"

    @Column(length = 2000)//respuesta de la ia con una maxima ya que puede ser muy larga la respuesta
    private String details;

    private boolean authorized;
}
