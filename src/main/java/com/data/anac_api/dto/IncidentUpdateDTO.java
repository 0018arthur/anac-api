package com.data.anac_api.dto;

import com.data.anac_api.enums.StatutIncident;
import lombok.Builder;
import lombok.Data;

@Builder
public record IncidentUpdateDTO (
        String titre,

        String description,

        String localisation,
        Double latitude,
        Double longitude,
        StatutIncident statut,
        Long assigneAId
){
}