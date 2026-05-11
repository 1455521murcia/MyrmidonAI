package es.gabriel.myrmidonai.service;

import es.gabriel.myrmidonai.model.Document;
import es.gabriel.myrmidonai.model.SecurityLevel;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


public interface DocumentService {
    Document saveDocument(MultipartFile file, String username, SecurityLevel level) throws IOException;
}
