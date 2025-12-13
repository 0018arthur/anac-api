package com.data.anac_api.mappers;

import com.data.anac_api.dto.IncidentUpdateDTO;
import com.data.anac_api.dto.request.IncidentRequestDTO;
import com.data.anac_api.dto.response.IncidentResponseDTO;
import com.data.anac_api.entity.Incident;
import com.data.anac_api.entity.Utilisateur;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class IncidentMapper {

    @Value("${file.upload.directory}")
    private String UPLOAD_DIR;
    
    public Incident toEntity(IncidentRequestDTO dto, Utilisateur declarePar) {
        return Incident.builder()
                .trackingId(UUID.randomUUID())
                .titre(dto.getTitre())
                .description(dto.getDescription())
                .typeIncident(dto.getTypeIncident())
                .localisation(dto.getLocalisation())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .declarePar(declarePar)
                .build();
    }

    public IncidentResponseDTO toDto(Incident incident) {
        IncidentResponseDTO.IncidentResponseDTOBuilder builder = IncidentResponseDTO.builder()
                .id(incident.getId())
                .trackingId(incident.getTrackingId())
                .titre(incident.getTitre())
                .description(incident.getDescription())
                .typeIncident(incident.getTypeIncident())
                .statut(incident.getStatut())
                .localisation(incident.getLocalisation())
                .latitude(incident.getLatitude())
                .priorite(incident.getPriorite())
                .longitude(incident.getLongitude())
                .analyseIA(incident.getAnalyseIA())
                .photoUrl(incident.getPhotoUrl())
                .createdAt(incident.getCreatedAt())
                .updatedAt(incident.getModifyAt())
                .resolvedAt(incident.getResolvedAt());

        if (incident.getDeclarePar() != null) {
            builder
                    .declareParId(incident.getDeclarePar().getId())
                    .declareParNom(incident.getDeclarePar().getNom() + " " + incident.getDeclarePar().getPrenoms())
                    .declareParEmail(incident.getDeclarePar().getEmail());
        }

        if (incident.getAssigneA() != null) {
            builder
                    .assigneAId(incident.getAssigneA().getId())
                    .assigneANom(incident.getAssigneA().getNom() + " " + incident.getAssigneA().getPrenoms())
                    .assigneAEmail(incident.getAssigneA().getEmail());
        }

        return builder.build();
    }


    public void updateEntity(Incident incident, IncidentUpdateDTO dto) {
        Optional.ofNullable(dto.titre()).ifPresent(incident::setTitre);
        Optional.ofNullable(dto.description()).ifPresent(incident::setDescription);
        Optional.ofNullable(dto.localisation()).ifPresent(incident::setLocalisation);
        Optional.ofNullable(dto.latitude()).ifPresent(incident::setLatitude);
        Optional.ofNullable(dto.longitude()).ifPresent(incident::setLongitude);
        Optional.ofNullable(dto.statut()).ifPresent(incident::setStatut);
    }


    public String savePhoto(MultipartFile photo) throws IOException {
        if (photo == null || photo.isEmpty()) {
            return null;
        }
        
        // Créer le répertoire s'il n'existe pas
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Générer un nom de fichier unique
        String fileName = UUID.randomUUID().toString() + "_" + 
                         photo.getOriginalFilename().replaceAll("\\s+", "_");
        Path filePath = uploadPath.resolve(fileName);
        
        // Sauvegarder le fichier
        Files.copy(photo.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Retourner le chemin relatif pour l'endpoint /api/v1/uploads/...
        // UPLOAD_DIR est généralement /tmp/uploads/incidents/ ou uploads/incidents/
        // On extrait la partie après "uploads/" pour construire le chemin relatif
        String normalizedDir = UPLOAD_DIR.replaceAll("\\\\", "/");
        String relativePath;
        
        // Si le chemin contient "uploads/", extraire la partie après
        int uploadsIndex = normalizedDir.indexOf("uploads/");
        if (uploadsIndex >= 0) {
            String afterUploads = normalizedDir.substring(uploadsIndex + "uploads/".length());
            // Nettoyer les slashes en début/fin
            afterUploads = afterUploads.replaceAll("^/+|/+$", "");
            relativePath = afterUploads.isEmpty() ? fileName : afterUploads + "/" + fileName;
        } else {
            // Si pas de "uploads/", utiliser le dernier segment du chemin
            Path dirPath = Paths.get(normalizedDir);
            String lastSegment = dirPath.getFileName() != null ? dirPath.getFileName().toString() : "";
            relativePath = lastSegment.isEmpty() ? fileName : lastSegment + "/" + fileName;
        }
        
        return relativePath;
    }
}