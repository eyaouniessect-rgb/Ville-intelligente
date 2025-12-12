package com.ville.gestionincidents.service.incident;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

/**
 * Gère le stockage local des images envoyées dans un dossier uploads/.
 */
@Service
public class PhotoStorageService {

    @Value("${uploads.path}")
    private String uploadDir;

    public String save(MultipartFile file) {

        try {
            Files.createDirectories(Paths.get(uploadDir));

            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, filename);

            Files.write(filePath, file.getBytes());

            return filename;

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors du stockage de l'image : " + e.getMessage());
        }
    }
}
