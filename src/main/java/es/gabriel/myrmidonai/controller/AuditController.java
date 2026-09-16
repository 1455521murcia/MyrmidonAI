package es.gabriel.myrmidonai.controller;

import es.gabriel.myrmidonai.dto.AuditLogDTO;
import es.gabriel.myrmidonai.repository.AuditLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    public AuditController(AuditLogRepository auditLogRepository1){
        this.auditLogRepository = auditLogRepository1;
    }

    //Solo los usuarios con el rol PARTNER pueden ver los logs
    @GetMapping("/logs")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<List<AuditLogDTO>> getAuditLogs(){

        //Hace una lista de los logs y los ordena por mas nuevo primero
        List<AuditLogDTO> logs = auditLogRepository.findAllByOrderByTimestampDesc()
                .stream()
                .map(log -> new AuditLogDTO(
                        log.getId(),
                        log.getTimestamp(),
                        log.getUsername(),
                        log.getAction(),
                        log.getDetails()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(logs);
    }
}
