package es.gabriel.myrmidonai.repository;

import es.gabriel.myrmidonai.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepository extends JpaRepository<Conversation,Long> {
}
