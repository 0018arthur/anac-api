package com.data.anac_api.dto.response;

import com.data.anac_api.enums.PrioriteIncident;
import com.data.anac_api.enums.StatutIncident;
import com.data.anac_api.enums.TypeIncident;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record IncidentResponseDTO(
        Long id,
        UUID trackingId,
        String titre,
        String description,
        TypeIncident typeIncident,
        StatutIncident statut,
        String localisation,
        Double latitude,
        Double longitude,
        String photoUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime resolvedAt,
        PrioriteIncident priorite,
        String analyseIA,
        String modifyBy,

        // Informations sur le déclarant
        Long declareParId,
        String declareParNom,
        String declareParEmail,

        // Informations sur l'assigné
        Long assigneAId,
        String assigneANom,
        String assigneAEmail
) {
}