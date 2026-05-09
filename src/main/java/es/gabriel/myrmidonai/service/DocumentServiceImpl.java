package es.gabriel.myrmidonai.service;

import es.gabriel.myrmidonai.model.SecurityLevel;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentServiceImpl implements DocumentService{

    @Override
    public void ingestPdfDocument(MultipartFile file, SecurityLevel securityLevel, String username) throws Exception {
        // TODO Aqui va toda la lógica de extraccion chunking embeddings y guardado en DB
        System.out.println("Procesando documento: " + file.getOriginalFilename() +
                " con nivel de seguridad: " + securityLevel +
                " por usuario: " + username);
    }
}
