package es.gabriel.myrmidonai.repository;

import es.gabriel.myrmidonai.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog,Long> {

    //Esto traerá automaticamente todos los logs ordenados desde el más nuevo al más antiguo
    List<AuditLog> findAllByOrderByTimestampDesc();

}
