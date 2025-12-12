package com.data.anac_api.service.impl;

import com.data.anac_api.dto.IncidentUpdateDTO;
import com.data.anac_api.dto.request.IncidentRequestDTO;
import com.data.anac_api.dto.response.IncidentResponseDTO;
import com.data.anac_api.entity.Incident;
import com.data.anac_api.entity.Utilisateur;
import com.data.anac_api.enums.PrioriteIncident;
import com.data.anac_api.enums.StatutIncident;
import com.data.anac_api.enums.TypeIncident;
import com.data.anac_api.exception.RessourceNotFoundException;
import com.data.anac_api.mappers.IncidentMapper;
import com.data.anac_api.repository.IncidentRepository;
import com.data.anac_api.repository.UtilisateurRepository;
import com.data.anac_api.service.EmailService;
import com.data.anac_api.service.IncidentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final IncidentMapper incidentMapper;
    private final ImageAnalysisService imageAnalysisService;
    private final EmailService emailService;

    @Override
    @Transactional
    public IncidentResponseDTO createIncident(IncidentRequestDTO incidentRequestDTO) {
        log.info("Création d'un nouvel incident: {}", incidentRequestDTO.getTitre());

        Utilisateur declarant = utilisateurRepository.findById(incidentRequestDTO.getDeclareParId())
                .orElseThrow(() -> new RessourceNotFoundException("Utilisateur non trouvé avec l'ID: " + incidentRequestDTO.getDeclareParId()));

        Incident incident = incidentMapper.toEntity(incidentRequestDTO, declarant);

        TypeIncident typeIncident = incidentRequestDTO.getTypeIncident();
        PrioriteIncident priorite = null;
        Map<String, Object> analysisResult = null;
        String analyseIA = null;

        // Si une photo est fournie, analyser pour suggérer le type et la priorité
        if (incidentRequestDTO.getPhoto() != null && !incidentRequestDTO.getPhoto().isEmpty()) {
            try {
                // Sauvegarder la photo
                String photoUrl = incidentMapper.savePhoto(incidentRequestDTO.getPhoto());
                incident.setPhotoUrl(photoUrl);

                // Analyser l'image avec IA
                analysisResult = imageAnalysisService
                        .analyzeImageForIncident(incidentRequestDTO.getPhoto())
                        .block();

                // Si le type n'est pas spécifié, utiliser l'IA pour le déterminer
                if (typeIncident == null && analysisResult != null) {
                    String suggestedType = imageAnalysisService.suggestIncidentType(analysisResult);
                    typeIncident = TypeIncident.valueOf(suggestedType);
                    incident.setTypeIncident(typeIncident);
                    log.info("Type d'incident suggéré par IA: {}", typeIncident);
                }

                // Analyser la priorité avec l'IA
                if (analysisResult != null) {
                    priorite = imageAnalysisService.analyzePriority(
                            analysisResult,
                            incidentRequestDTO.getDescription(),
                            typeIncident
                    );
                    log.info("Priorité d'incident déterminée par IA: {}", priorite);
                }

            } catch (IOException e) {
                log.error("Erreur lors de la sauvegarde ou analyse de la photo", e);
                throw new RuntimeException("Erreur lors du traitement de la photo");
            }
        }

        // Si pas de photo ou échec de l'analyse, déterminer la priorité sans image
        if (priorite == null) {
            priorite = imageAnalysisService.analyzePriorityWithoutImage(
                    incidentRequestDTO.getDescription(),
                    typeIncident != null ? typeIncident : TypeIncident.OTHER
            );
            log.info("Priorité d'incident déterminée sans image: {}", priorite);
        }

        // Définir la priorité
        incident.setPriorite(priorite);

        //Générer l'analyse détaillée de l'IA**
        if (analysisResult != null) {
            // Analyse avec image
            analyseIA = imageAnalysisService.generateDetailedAnalysis(
                    analysisResult,
                    incidentRequestDTO.getDescription(),
                    typeIncident != null ? typeIncident : TypeIncident.OTHER,
                    priorite
            );
        } else {
            // Analyse sans image
            analyseIA = imageAnalysisService.generateAnalysisWithoutImage(
                    incidentRequestDTO.getDescription(),
                    typeIncident != null ? typeIncident : TypeIncident.OTHER,
                    priorite
            );
        }

        // Stocker l'analyse IA dans l'entité
        incident.setAnalyseIA(analyseIA);
        log.info("Analyse IA générée et stockée pour l'incident");

        // Sauvegarder l'incident
        Incident savedIncident = incidentRepository.save(incident);
        log.info("Incident créé avec l'ID: {}, trackingId: {} et priorité: {}",
                savedIncident.getId(), savedIncident.getTrackingId(), savedIncident.getPriorite());

        // Convertir en DTO de réponse
        IncidentResponseDTO responseDTO = incidentMapper.toDto(savedIncident);

        // Envoyer une alerte email si la priorité est élevée ou critique
        if (priorite == PrioriteIncident.ELEVEE || priorite == PrioriteIncident.CRITIQUE) {
            try {
                emailService.envoyerAlertePriorite(responseDTO);
                log.info("Alerte email envoyée pour incident prioritaire: {}", savedIncident.getTrackingId());
            } catch (Exception e) {
                log.error("Erreur lors de l'envoi de l'alerte email, l'incident a été créé mais l'alerte a échoué", e);
            }
        }

        return responseDTO;
    }

    private TypeIncident analyzeImageAndSuggestType(MultipartFile imageFile) {
        try {
            Map<String, Object> analysisResult = imageAnalysisService
                    .analyzeImageForIncident(imageFile)
                    .block();

            String suggestedType = imageAnalysisService.suggestIncidentType(analysisResult);
            return TypeIncident.valueOf(suggestedType);

        } catch (Exception e) {
            log.warn("Échec de l'analyse d'image, utilisation du type par défaut", e);
            return TypeIncident.OTHER;
        }
    }

    @Transactional
    @Override
    public TypeIncident analyzeExistingIncidentPhoto(Long incidentId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new RessourceNotFoundException("Incident non trouvé"));

        if (incident.getPhotoUrl() == null) {
            throw new RuntimeException("Aucune photo disponible pour cet incident");
        }

        return TypeIncident.OTHER;
    }

    @Override
    @Transactional(readOnly = true)
    public IncidentResponseDTO getIncidentById(Long id) {
        log.info("Récupération de l'incident avec l'ID: {}", id);

        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Incident non trouvé avec l'ID: " + id));

        return incidentMapper.toDto(incident);
    }

    @Override
    @Transactional(readOnly = true)
    public IncidentResponseDTO getIncidentByTrackingId(UUID trackingId) {
        log.info("Récupération de l'incident avec le trackingId: {}", trackingId);

        Incident incident = incidentRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new RessourceNotFoundException("Incident non trouvé avec le trackingId: " + trackingId));

        return incidentMapper.toDto(incident);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<IncidentResponseDTO> getAllIncidents(Pageable pageable) {
        log.info("Récupération de tous les incidents paginés");

        return incidentRepository.findAll(pageable)
                .map(incidentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<IncidentResponseDTO> getIncidentsByDeclarant(Long declarantId, Pageable pageable) {
        log.info("Récupération des incidents déclarés par l'utilisateur ID: {}", declarantId);

        return incidentRepository.findByDeclareParId(declarantId, pageable)
                .map(incidentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<IncidentResponseDTO> getIncidentsByAssignee(Long assigneeId, Pageable pageable) {
        log.info("Récupération des incidents assignés à l'utilisateur ID: {}", assigneeId);

        return incidentRepository.findByAssigneA_Id(assigneeId, pageable)
                .map(incidentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidentResponseDTO> getIncidentsByType(TypeIncident type) {
        log.info("Récupération des incidents par type: {}", type);

        return incidentRepository.findByTypeIncident(type)
                .stream()
                .map(incidentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidentResponseDTO> getIncidentsByStatus(StatutIncident status) {
        log.info("Récupération des incidents par statut: {}", status);

        return incidentRepository.findByStatut(status)
                .stream()
                .map(incidentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<IncidentResponseDTO> getIncidentsByPriorite(PrioriteIncident priorite) {
        log.info("Récupération des incidents par priorité: {}", priorite);

        return incidentRepository.findByPriorite(priorite)
                .stream()
                .map(incidentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public Page<IncidentResponseDTO> getIncidentsByPrioriteOrderByDate(PrioriteIncident priorite, Pageable pageable) {
        log.info("Récupération des incidents par priorité {} triés par date", priorite);

        return incidentRepository.findByPrioriteOrderByCreatedAtDesc(priorite, pageable)
                .map(incidentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<IncidentResponseDTO> searchIncidents(TypeIncident type, StatutIncident status,
                                                     LocalDateTime startDate, LocalDateTime endDate,
                                                     Pageable pageable) {
        log.info("Recherche d'incidents avec filtres - Type: {}, Statut: {}", type, status);

        return incidentRepository.searchIncidents(type, status, startDate, endDate, pageable)
                .map(incidentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<IncidentResponseDTO> searchByKeyword(String keyword, Pageable pageable) {
        log.info("Recherche d'incidents avec le mot-clé: {}", keyword);

        return incidentRepository.searchByKeyword(keyword, pageable)
                .map(incidentMapper::toDto);
    }

    @Override
    @Transactional
    public IncidentResponseDTO updateIncident(Long id, IncidentUpdateDTO updateDTO) {
        log.info("Mise à jour de l'incident ID: {}", id);

        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Incident non trouvé avec l'ID: " + id));

        incidentMapper.updateEntity(incident, updateDTO);

        if (updateDTO.assigneAId() != null) {
            Utilisateur assigne = utilisateurRepository.findById(updateDTO.assigneAId())
                    .orElseThrow(() -> new RessourceNotFoundException("Utilisateur assigné non trouvé avec l'ID: " + updateDTO.assigneAId()));
            incident.setAssigneA(assigne);
        }

        Incident updatedIncident = incidentRepository.save(incident);
        log.info("Incident ID: {} mis à jour avec succès", id);

        return incidentMapper.toDto(updatedIncident);
    }

    @Override
    @Transactional
    public IncidentResponseDTO updateIncidentStatus(Long id, StatutIncident status) {
        log.info("Mise à jour du statut de l'incident ID: {} vers {}", id, status);

        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Incident non trouvé avec l'ID: " + id));

        incident.setStatut(status);

        Incident updatedIncident = incidentRepository.save(incident);
        log.info("Statut de l'incident ID: {} mis à jour avec succès", id);

        return incidentMapper.toDto(updatedIncident);
    }

    @Override
    @Transactional
    public IncidentResponseDTO assignIncident(Long id, Long assigneeId) {
        log.info("Assignation de l'incident ID: {} à l'utilisateur ID: {}", id, assigneeId);

        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Incident non trouvé avec l'ID: " + id));

        Utilisateur assigne = utilisateurRepository.findById(assigneeId)
                .orElseThrow(() -> new RessourceNotFoundException("Utilisateur assigné non trouvé avec l'ID: " + assigneeId));

        incident.setAssigneA(assigne);
        incident.setStatut(StatutIncident.EN_COURS);

        Incident updatedIncident = incidentRepository.save(incident);
        log.info("Incident ID: {} assigné avec succès à l'utilisateur ID: {}", id, assigneeId);

        return incidentMapper.toDto(updatedIncident);
    }

    @Override
    @Transactional
    public IncidentResponseDTO addPhotoToIncident(Long id, MultipartFile photo) {
        log.info("Ajout d'une photo à l'incident ID: {}", id);

        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Incident non trouvé avec l'ID: " + id));

        if (photo != null && !photo.isEmpty()) {
            try {
                String photoUrl = incidentMapper.savePhoto(photo);
                incident.setPhotoUrl(photoUrl);
            } catch (IOException e) {
                log.error("Erreur lors de la sauvegarde de la photo", e);
                throw new RuntimeException("Erreur lors de la sauvegarde de la photo");
            }
        }

        Incident updatedIncident = incidentRepository.save(incident);
        log.info("Photo ajoutée avec succès à l'incident ID: {}", id);

        return incidentMapper.toDto(updatedIncident);
    }

    @Override
    @Transactional
    public void deleteIncident(Long id) {
        log.info("Suppression de l'incident ID: {}", id);

        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Incident non trouvé avec l'ID: " + id));

        incidentRepository.delete(incident);
        log.info("Incident ID: {} supprimé avec succès", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getIncidentStats() {
        log.info("Calcul des statistiques des incidents");

        Map<String, Object> stats = new HashMap<>();

        long totalIncidents = incidentRepository.count();
        stats.put("totalIncidents", totalIncidents);

        // Statistiques par statut - mapping des noms pour le frontend
        Map<StatutIncident, String> statutMapping = new HashMap<>();
        statutMapping.put(StatutIncident.EN_ATTENTE, "enAttente");
        statutMapping.put(StatutIncident.EN_COURS, "enCours");
        statutMapping.put(StatutIncident.RESOLU, "resolus");
        statutMapping.put(StatutIncident.REJETE, "rejetes");

        for (StatutIncident statut : StatutIncident.values()) {
            long count = incidentRepository.countByStatut(statut);
            String key = statutMapping.getOrDefault(statut, statut.name().toLowerCase());
            stats.put(key, count);
        }

        // Statistiques par priorité - mapping des noms pour le frontend
        Map<PrioriteIncident, String> prioriteMapping = new HashMap<>();
        prioriteMapping.put(PrioriteIncident.CRITIQUE, "critique");
        prioriteMapping.put(PrioriteIncident.ELEVEE, "elevee");
        prioriteMapping.put(PrioriteIncident.MOYENNE, "moyenne");
        prioriteMapping.put(PrioriteIncident.FAIBLE, "faible");

        for (PrioriteIncident priorite : PrioriteIncident.values()) {
            long count = incidentRepository.countByPriorite(priorite);
            String key = prioriteMapping.getOrDefault(priorite, priorite.name().toLowerCase());
            stats.put(key, count);
        }

        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        long recentIncidents = incidentRepository.searchIncidents(
                null, null, sevenDaysAgo, LocalDateTime.now(), Pageable.unpaged()
        ).getTotalElements();
        stats.put("recentIncidents", recentIncidents);

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<TypeIncident, Long> getIncidentCountByType() {
        log.info("Calcul du nombre d'incidents par type");

        Map<TypeIncident, Long> counts = new HashMap<>();
        List<Object[]> results = incidentRepository.countByTypeIncident();

        for (Object[] result : results) {
            TypeIncident type = (TypeIncident) result[0];
            Long count = (Long) result[1];
            counts.put(type, count);
        }

        return counts;
    }

    @Transactional(readOnly = true)
    @Override
    public Map<PrioriteIncident, Long> getIncidentCountByPriorite() {
        log.info("Calcul du nombre d'incidents par priorité");

        Map<PrioriteIncident, Long> counts = new HashMap<>();
        List<Object[]> results = incidentRepository.countByPrioriteGroup();

        for (Object[] result : results) {
            PrioriteIncident priorite = (PrioriteIncident) result[0];
            Long count = (Long) result[1];
            counts.put(priorite, count);
        }

        return counts;
    }
}