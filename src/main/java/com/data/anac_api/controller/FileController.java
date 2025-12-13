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
            
            log.info("Chemin du fichier extrait depuis l'URL: {}", filePath);
            
            // Construire le chemin complet du fichier
            // Gérer plusieurs cas :
            // 1. Chemin avec sous-dossier : incidents/file.jpg
            // 2. Nom de fichier seul : file.jpg (chercher directement dans uploadDirectory)
            // 3. Ancien format absolu : /tmp/uploads/incidents/file.jpg
            
            Path fileSystemPath = null;
            File file = null;
            
            if (filePath.startsWith("/tmp")) {
                // Ancien format avec chemin absolu : /tmp/uploads/incidents/file.jpg
                String fileName = Paths.get(filePath).getFileName().toString();
                fileSystemPath = Paths.get(uploadDirectory).resolve(fileName).normalize();
                file = fileSystemPath.toFile();
                log.info("Ancien format détecté, extraction du nom de fichier: {}, résolution: {}", fileName, fileSystemPath);
            } else {
                // Essayer plusieurs stratégies pour trouver le fichier
                String fileName = Paths.get(filePath).getFileName().toString();
                
                // Stratégie 1: Essayer avec le chemin tel quel (pour incidents/file.jpg)
                Path tryPath1 = Paths.get(uploadDirectory).resolve(filePath).normalize();
                File tryFile1 = tryPath1.toFile();
                if (tryFile1.exists() && tryFile1.isFile()) {
                    fileSystemPath = tryPath1;
                    file = tryFile1;
                    log.info("Fichier trouvé avec chemin complet: {}", fileSystemPath);
                } else {
                    // Stratégie 2: Essayer avec juste le nom du fichier (cas où le fichier est directement dans uploadDirectory)
                    Path tryPath2 = Paths.get(uploadDirectory).resolve(fileName).normalize();
                    File tryFile2 = tryPath2.toFile();
                    if (tryFile2.exists() && tryFile2.isFile()) {
                        fileSystemPath = tryPath2;
                        file = tryFile2;
                        log.info("Fichier trouvé avec nom de fichier seul: {}", fileSystemPath);
                    } else {
                        // Stratégie 3: Si le chemin commence par "incidents/", essayer sans ce préfixe
                        if (filePath.startsWith("incidents/")) {
                            String pathWithoutIncidents = filePath.substring("incidents/".length());
                            Path tryPath3 = Paths.get(uploadDirectory).resolve(pathWithoutIncidents).normalize();
                            File tryFile3 = tryPath3.toFile();
                            if (tryFile3.exists() && tryFile3.isFile()) {
                                fileSystemPath = tryPath3;
                                file = tryFile3;
                                log.info("Fichier trouvé après suppression du préfixe incidents/: {}", fileSystemPath);
                            }
                        }
                    }
                }
            }
            
            // Si aucun fichier n'a été trouvé, préparer le message d'erreur
            if (fileSystemPath == null) {
                fileSystemPath = Paths.get(uploadDirectory).resolve(filePath).normalize();
            }
            
            log.info("Répertoire d'upload configuré: {}", uploadDirectory);
            log.info("Chemin système du fichier final: {}", fileSystemPath);
            
            // Vérifier que le répertoire existe
            File uploadDir = Paths.get(uploadDirectory).toFile();
            if (!uploadDir.exists() || !uploadDir.isDirectory()) {
                log.error("Le répertoire d'upload n'existe pas ou n'est pas un répertoire: {}", uploadDirectory);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Répertoire d'upload non accessible : " + uploadDirectory);
            }
            
            // Lister les fichiers dans le répertoire pour le débogage (seulement si fichier non trouvé)
            if (file == null || !file.exists() || !file.isFile()) {
                File[] filesInDir = uploadDir.listFiles();
                if (filesInDir != null) {
                    log.warn("Fichiers trouvés dans le répertoire upload ({} fichiers):", filesInDir.length);
                    for (File f : filesInDir) {
                        log.warn("  - {}", f.getName());
                    }
                }
            }

            // Vérifier que le fichier existe et est dans le répertoire autorisé
            if (file == null || !file.exists() || !file.isFile()) {
                log.error("Fichier non trouvé - Chemin demandé: {}, Chemin système: {}", filePath, fileSystemPath);
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

