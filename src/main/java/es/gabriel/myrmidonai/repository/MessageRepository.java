package es.gabriel.myrmidonai.repository;

import es.gabriel.myrmidonai.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message,Long> {

    List<Message> findTop6ByConversationIdOrderByTimestampAsc(Long conversationId);
}
