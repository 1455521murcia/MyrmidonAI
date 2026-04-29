package es.gabriel.myrmidonai.repository;

import es.gabriel.myrmidonai.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog,Long> {
}
