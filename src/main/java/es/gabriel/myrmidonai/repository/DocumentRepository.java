package es.gabriel.myrmidonai.repository;

import es.gabriel.myrmidonai.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document,Long> {
}
