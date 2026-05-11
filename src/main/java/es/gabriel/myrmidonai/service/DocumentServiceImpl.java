package es.gabriel.myrmidonai.service;

import es.gabriel.myrmidonai.model.Document;
import es.gabriel.myrmidonai.model.SecurityLevel;
import es.gabriel.myrmidonai.model.User;
import es.gabriel.myrmidonai.repository.DocumentRepository;
import es.gabriel.myrmidonai.repository.UserRepository;
import es.gabriel.myrmidonai.service.DocumentService;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final VectorStore vectorStore;
    private final Path rootLocation;

    public DocumentServiceImpl(DocumentRepository documentRepository,
                               UserRepository userRepository,
                               VectorStore vectorStore,
                               @Value("${myrmidon.upload-dir}") String uploadDir) {

        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.vectorStore = vectorStore;
        this.rootLocation = Paths.get(uploadDir);

        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo inicializar la carpeta de almacenamiento", e);
        }
    }

    @Override
    @Transactional
    public Document saveDocument(MultipartFile file, String username, SecurityLevel level) throws IOException {

        //  Guardar archivo físico
        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Files.copy(file.getInputStream(), this.rootLocation.resolve(filename));

        // Buscar al usuario logueado en la bd
        User owner = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Crear el registro en la bd
        Document doc = new Document();
        doc.setFileName(file.getOriginalFilename());
        doc.setFilePath(this.rootLocation.resolve(filename).toString());
        doc.setUploadDate(LocalDateTime.now());
        doc.setSecurityLevel(level);
        doc.setOwner(owner);

        Document savedDoc = documentRepository.save(doc);

        //Lanzo la ingesta hacia la IA
        ingestDocument(savedDoc);

        return savedDoc;
    }

    // Hago otro metodo para separar un poco la logica
    private void ingestDocument(Document doc) {

        // Cargar el archivo guardado y pasarlo a Tika
        Resource fileResource = new FileSystemResource(doc.getFilePath());
        TikaDocumentReader reader = new TikaDocumentReader(fileResource);

        // Extraemos el texto
        List<org.springframework.ai.document.Document> rawDocuments = reader.get();

        // Chunking(Trocear texto)
        TokenTextSplitter splitter = new TokenTextSplitter();
        List<org.springframework.ai.document.Document> chunks = splitter.apply(rawDocuments);

        //Se añaden metadatos
        for (org.springframework.ai.document.Document chunk : chunks) {
            chunk.getMetadata().put("security_level", doc.getSecurityLevel().name());
            chunk.getMetadata().put("document_id", doc.getId());
            chunk.getMetadata().put("title", doc.getFileName());
        }

        // Vectorizar y guardar en el pgvector
        vectorStore.add(chunks);
        System.out.println("IA: Documento '" + doc.getFileName() + "' vectorizado y guardado con éxito.");
    }
}
