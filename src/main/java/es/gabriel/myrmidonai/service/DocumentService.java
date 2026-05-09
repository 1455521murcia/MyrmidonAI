package es.gabriel.myrmidonai.service;

import es.gabriel.myrmidonai.model.SecurityLevel;
import org.springframework.web.multipart.MultipartFile;


public interface DocumentService {
    void ingestPdfDocument(MultipartFile file, SecurityLevel securityLevel, String username) throws Exception;
}
