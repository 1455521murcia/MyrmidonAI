package es.gabriel.myrmidonai.repository;

import es.gabriel.myrmidonai.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation,Long> {

    List<Conversation> findByUserIdOrderByLastUpdatedDesc(Long aLong);
}
