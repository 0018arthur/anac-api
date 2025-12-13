package com.data.anac_api.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@RestController
@RequestMapping("uploads")
@RequiredArgsConstructor
public class FileController {

    @Value("${file.upload.directory}")
    private String uploadDirectory;

    /**
     * Sert un fichier uploadé depuis le répertoire de stockage
     * L'URL attendue est de la forme : /api/v1/uploads/incidents/filename.jpg
     * 
     * @param request La requête HTTP pour extraire le chemin
     * @return Le fichier avec les en-têtes appropriés
     */
    @GetMapping("/**")
    public ResponseEntity<Resource> serveFile(HttpServletRequest request) {
        try {
            // Extraire le chemin depuis l'URL
            // Exemple: /api/v1/uploads/incidents/file.jpg -> incidents/file.jpg
            String requestURI = request.getRequestURI();
            String contextPath = request.getContextPath(); // /api/v1 ou vide
            
            log.debug("Requête de fichier - URI: {}, ContextPath: {}", requestURI, contextPath);
            
            // Enlever le context-path si présent
            String filePath = requestURI;
            if (contextPath != null && !contextPath.isEmpty() && requestURI.startsWith(contextPath)) {
                filePath = requestURI.substring(contextPath.length());
            }
            
            // Enlever le préfixe /uploads (le contrôleur est mappé sur "uploads")
            // Après avoir enlevé le context-path, on devrait avoir /uploads/incidents/file.jpg
            if (filePath.startsWith("/uploads/")) {
                filePath = filePath.substring("/uploads/".length());
            } else if (filePath.startsWith("uploads/")) {
                filePath = filePath.substring("uploads/".length());
            } else if (filePath.startsWith("/")) {
                // Si le context-path n'a pas été enlevé correctement, essayer directement
                filePath = filePath.substring(1);
            }
            
            log.debug("Chemin du fichier extrait: {}", filePath);
            
            // Construire le chemin complet du fichier
            // Gérer les anciens chemins qui pourraient être absolus (/tmp/uploads/incidents/file.jpg)
            Path fileSystemPath;
            if (filePath.startsWith("/") && !filePath.startsWith("/tmp")) {
                // Chemin relatif normal : incidents/file.jpg
                fileSystemPath = Paths.get(uploadDirectory).resolve(filePath).normalize();
            } else if (filePath.startsWith("/tmp")) {
                // Ancien format avec chemin absolu : /tmp/uploads/incidents/file.jpg
                // Extraire le nom du fichier depuis le chemin complet
                String fileName = Paths.get(filePath).getFileName().toString();
                // Chercher dans le répertoire upload
                fileSystemPath = Paths.get(uploadDirectory).resolve(fileName).normalize();
                log.debug("Ancien format détecté, extraction du nom de fichier: {}", fileName);
            } else {
                // Chemin relatif simple
                fileSystemPath = Paths.get(uploadDirectory).resolve(filePath).normalize();
            }
            
            log.debug("Répertoire d'upload configuré: {}", uploadDirectory);
            log.debug("Chemin système du fichier: {}", fileSystemPath);
            
            File file = fileSystemPath.toFile();

            // Vérifier que le fichier existe et est dans le répertoire autorisé
            if (!file.exists() || !file.isFile()) {
                log.warn("Fichier non trouvé - Chemin demandé: {}, Chemin système: {}", filePath, fileSystemPath);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Fichier non trouvé : " + filePath + " (cherché dans : " + fileSystemPath + ")");
            }
            
            log.info("Fichier trouvé et servi: {}", fileSystemPath);

            // Vérifier la sécurité : s'assurer que le fichier est dans le répertoire upload
            Path uploadPath = Paths.get(uploadDirectory).normalize().toAbsolutePath();
            Path requestedPath = fileSystemPath.normalize().toAbsolutePath();
            
            if (!requestedPath.startsWith(uploadPath)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                    "Accès non autorisé au fichier");
            }

            // Déterminer le type MIME
            String contentType = Files.probeContentType(fileSystemPath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            Resource resource = new FileSystemResource(file);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "inline; filename=\"" + file.getName() + "\"")
                    .body(resource);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Erreur lors de la récupération du fichier : " + e.getMessage());
        }
    }
}

