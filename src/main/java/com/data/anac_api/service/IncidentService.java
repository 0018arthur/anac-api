package com.data.anac_api.service;

import com.data.anac_api.dto.IncidentUpdateDTO;
import com.data.anac_api.dto.request.IncidentRequestDTO;
import com.data.anac_api.dto.response.IncidentResponseDTO;
import com.data.anac_api.enums.PrioriteIncident;
import com.data.anac_api.enums.StatutIncident;
import com.data.anac_api.enums.TypeIncident;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface IncidentService {
    
    IncidentResponseDTO createIncident(IncidentRequestDTO incidentRequestDTO);

    @Transactional
    TypeIncident analyzeExistingIncidentPhoto(Long incidentId);

    IncidentResponseDTO getIncidentById(Long id);
    
    IncidentResponseDTO getIncidentByTrackingId(UUID trackingId);
    
    Page<IncidentResponseDTO> getAllIncidents(Pageable pageable);
    
    Page<IncidentResponseDTO> getIncidentsByDeclarant(Long declarantId, Pageable pageable);
    
    Page<IncidentResponseDTO> getIncidentsByAssignee(Long assigneeId, Pageable pageable);
    
    List<IncidentResponseDTO> getIncidentsByType(TypeIncident type);
    
    List<IncidentResponseDTO> getIncidentsByStatus(StatutIncident status);

    @Transactional(readOnly = true)
    List<IncidentResponseDTO> getIncidentsByPriorite(PrioriteIncident priorite);

    @Transactional(readOnly = true)
    Page<IncidentResponseDTO> getIncidentsByPrioriteOrderByDate(PrioriteIncident priorite, Pageable pageable);

    Page<IncidentResponseDTO> searchIncidents(TypeIncident type, StatutIncident status,
                                              LocalDateTime startDate, LocalDateTime endDate,
                                              Pageable pageable);
    
    Page<IncidentResponseDTO> searchByKeyword(String keyword, Pageable pageable);
    
    IncidentResponseDTO updateIncident(Long id, IncidentUpdateDTO updateDTO);
    
    IncidentResponseDTO updateIncidentStatus(Long id, StatutIncident status);
    
    IncidentResponseDTO assignIncident(Long id, Long assigneeId);
    
    IncidentResponseDTO addPhotoToIncident(Long id, MultipartFile photo);
    
    void deleteIncident(Long id);
    
    Map<String, Object> getIncidentStats();
    
    Map<TypeIncident, Long> getIncidentCountByType();

    @Transactional(readOnly = true)
    Map<PrioriteIncident, Long> getIncidentCountByPriorite();
}