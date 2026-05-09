package es.gabriel.myrmidonai.controller;


import es.gabriel.myrmidonai.model.SecurityLevel;
import es.gabriel.myrmidonai.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {


    private final DocumentService documentService;

    @Autowired
    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> uploadDocument(
            @RequestParam("file") MultipartFile file, // el archivo PDF
            @RequestParam("securityLevel") SecurityLevel securityLevel, // El nivel de seguridad que tendrá el pdf
            Principal principal // Info del usuario logueado
    ) {
        // Validaciones básicas del archivo, tipo y tamaño
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("El archivo está vacío.");

        }
        if (!"application/pdf".equals(file.getContentType())) {
            return ResponseEntity.badRequest().body("Solo se permiten archivos PDF.");

        }

        try {
            documentService.ingestPdfDocument(file, securityLevel, principal.getName());
            return ResponseEntity.ok("Documento '" + file.getOriginalFilename() + "' subido y procesado correctamente.");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al procesar el documento: " + e.getMessage());

        }
    }

}
