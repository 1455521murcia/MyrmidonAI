package es.gabriel.myrmidonai.repository;

import es.gabriel.myrmidonai.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message,Long> {
}
