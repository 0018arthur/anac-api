package com.data.anac_api.controller;

import com.data.anac_api.dto.IncidentUpdateDTO;
import com.data.anac_api.dto.request.IncidentRequestDTO;
import com.data.anac_api.dto.response.IncidentResponseDTO;
import com.data.anac_api.enums.StatutIncident;
import com.data.anac_api.enums.TypeIncident;
import com.data.anac_api.service.IncidentService;
import com.data.anac_api.utils.DataResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("incidents")
@RequiredArgsConstructor
@Tag(name = "Incidents", description = "API de gestion des incidents")
public class IncidentController {
    
    private final IncidentService incidentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Créer un nouvel incident")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Incident créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    public ResponseEntity<IncidentResponseDTO> createIncident(
            @RequestPart("titre") String titre,
            @RequestPart("description") String description,
            @RequestPart(value = "typeIncident", required = false) String typeIncidentStr,
            @RequestPart(value = "localisation", required = false) String localisation,
            @RequestPart(value = "latitude", required = false) String latitudeStr,
            @RequestPart(value = "longitude", required = false) String longitudeStr,
            @RequestPart("declareParId") String declareParIdStr,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {

        // Validation des champs obligatoires
        if (titre == null || titre.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le titre est obligatoire");
        }

        if (description == null || description.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La description est obligatoire");
        }

        // Conversion des types
        TypeIncident typeIncident = null;
        if (typeIncidentStr != null && !typeIncidentStr.trim().isEmpty()) {
            try {
                typeIncident = TypeIncident.valueOf(typeIncidentStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Type d'incident invalide. Valeurs acceptées : " +
                                Arrays.toString(TypeIncident.values()));
            }
        }

        Double latitude = null;
        if (latitudeStr != null && !latitudeStr.trim().isEmpty()) {
            try {
                latitude = Double.parseDouble(latitudeStr.trim());
            } catch (NumberFormatException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Latitude invalide. Doit être un nombre.");
            }
        }

        Double longitude = null;
        if (longitudeStr != null && !longitudeStr.trim().isEmpty()) {
            try {
                longitude = Double.parseDouble(longitudeStr.trim());
            } catch (NumberFormatException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Longitude invalide. Doit être un nombre.");
            }
        }

        long declareParId;
        try {
            declareParId = Long.parseLong(declareParIdStr.trim());
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "ID du déclarant invalide. Doit être un nombre.");
        }

        // Créer le DTO
        IncidentRequestDTO incidentRequestDTO = new IncidentRequestDTO(
                titre.trim(),
                description.trim(),
                typeIncident,
                localisation != null ? localisation.trim() : null,
                latitude,
                longitude,
                photo,
                declareParId
        );

        IncidentResponseDTO createdIncident = incidentService.createIncident(incidentRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdIncident);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un incident par son ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Incident trouvé"),
        @ApiResponse(responseCode = "404", description = "Incident non trouvé")
    })
    public ResponseEntity<IncidentResponseDTO> getIncidentById(@PathVariable Long id) {
        IncidentResponseDTO incident = incidentService.getIncidentById(id);
        return ResponseEntity.ok(incident);
    }
    
    @GetMapping("/tracking/{trackingId}")
    @Operation(summary = "Récupérer un incident par son tracking ID")
    public ResponseEntity<IncidentResponseDTO> getIncidentByTrackingId(@PathVariable UUID trackingId) {
        IncidentResponseDTO incident = incidentService.getIncidentByTrackingId(trackingId);
        return ResponseEntity.ok(incident);
    }
    
    @GetMapping
    @Operation(summary = "Récupérer tous les incidents (paginés)")
    public ResponseEntity<Page<IncidentResponseDTO>> getAllIncidents(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<IncidentResponseDTO> incidents = incidentService.getAllIncidents(pageable);
        return ResponseEntity.ok(incidents);
    }
    
    @GetMapping("/declarant/{declarantId}")
    @Operation(summary = "Récupérer les incidents déclarés par un utilisateur")
    public ResponseEntity<Page<IncidentResponseDTO>> getIncidentsByDeclarant(
            @PathVariable Long declarantId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<IncidentResponseDTO> incidents = incidentService.getIncidentsByDeclarant(declarantId, pageable);
        return ResponseEntity.ok(incidents);
    }
    
    @GetMapping("/assignee/{assigneeId}")
    @Operation(summary = "Récupérer les incidents assignés à un utilisateur")
    public ResponseEntity<Page<IncidentResponseDTO>> getIncidentsByAssignee(
            @PathVariable Long assigneeId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<IncidentResponseDTO> incidents = incidentService.getIncidentsByAssignee(assigneeId, pageable);
        return ResponseEntity.ok(incidents);
    }
    
    @GetMapping("/type/{type}")
    @Operation(summary = "Récupérer les incidents par type")
    public ResponseEntity<List<IncidentResponseDTO>> getIncidentsByType(@PathVariable TypeIncident type) {
        List<IncidentResponseDTO> incidents = incidentService.getIncidentsByType(type);
        return ResponseEntity.ok(incidents);
    }
    
    @GetMapping("/status/{status}")
    @Operation(summary = "Récupérer les incidents par statut")
    public ResponseEntity<List<IncidentResponseDTO>> getIncidentsByStatus(@PathVariable StatutIncident status) {
        List<IncidentResponseDTO> incidents = incidentService.getIncidentsByStatus(status);
        return ResponseEntity.ok(incidents);
    }
    
    @GetMapping("/search")
    @Operation(summary = "Rechercher des incidents avec filtres")
    public ResponseEntity<Page<IncidentResponseDTO>> searchIncidents(
            @RequestParam(required = false) TypeIncident type,
            @RequestParam(required = false) StatutIncident status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<IncidentResponseDTO> incidents = incidentService.searchIncidents(
                type, status, startDate, endDate, pageable);
        return ResponseEntity.ok(incidents);
    }
    
    @GetMapping("/search/keyword")
    @Operation(summary = "Rechercher des incidents par mot-clé")
    public ResponseEntity<Page<IncidentResponseDTO>> searchByKeyword(
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<IncidentResponseDTO> incidents = incidentService.searchByKeyword(keyword, pageable);
        return ResponseEntity.ok(incidents);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un incident")
    public ResponseEntity<IncidentResponseDTO> updateIncident(
            @PathVariable Long id,
            @Valid @RequestBody IncidentUpdateDTO updateDTO) {
        
        IncidentResponseDTO updatedIncident = incidentService.updateIncident(id, updateDTO);
        return ResponseEntity.ok(updatedIncident);
    }
    
    @PatchMapping("/{id}/status")
    @Operation(summary = "Mettre à jour le statut d'un incident")
    public ResponseEntity<IncidentResponseDTO> updateIncidentStatus(
            @PathVariable Long id,
            @RequestParam StatutIncident status) {
        
        IncidentResponseDTO updatedIncident = incidentService.updateIncidentStatus(id, status);
        return ResponseEntity.ok(updatedIncident);
    }
    
    @PatchMapping("/{id}/assign")
    @Operation(summary = "Assigner un incident à un utilisateur")
    public ResponseEntity<IncidentResponseDTO> assignIncident(
            @PathVariable Long id,
            @RequestParam Long assigneeId) {
        
        IncidentResponseDTO updatedIncident = incidentService.assignIncident(id, assigneeId);
        return ResponseEntity.ok(updatedIncident);
    }
    
    @PostMapping("/{id}/photo")
    @Operation(summary = "Ajouter une photo à un incident")
    public ResponseEntity<IncidentResponseDTO> addPhotoToIncident(
            @PathVariable Long id,
            @RequestParam("photo") MultipartFile photo) {
        
        IncidentResponseDTO updatedIncident = incidentService.addPhotoToIncident(id, photo);
        return ResponseEntity.ok(updatedIncident);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un incident")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Incident supprimé avec succès"),
        @ApiResponse(responseCode = "404", description = "Incident non trouvé")
    })
    public ResponseEntity<Void> deleteIncident(@PathVariable Long id) {
        incidentService.deleteIncident(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats/type")
    @Operation(summary = "Obtenir le nombre d'incidents par type")
    public ResponseEntity<DataResponse<Map<TypeIncident, Long>>> getIncidentCountByType() {
        Map<TypeIncident, Long> counts = incidentService.getIncidentCountByType();
        DataResponse<Map<TypeIncident, Long>> response = new DataResponse<>(
                new java.util.Date(),
                false,
                "Nombre d'incidents par type récupéré avec succès",
                counts
        );
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/stats")
    @Operation(summary = "Obtenir les statistiques des incidents")
    public ResponseEntity<DataResponse<Map<String, Object>>> getIncidentStats() {
        Map<String, Object> stats = incidentService.getIncidentStats();
        DataResponse<Map<String, Object>> response = new DataResponse<>(
                new java.util.Date(),
                false,
                "Statistiques récupérées avec succès",
                stats
        );
        return ResponseEntity.ok(response);
    }
}