package com.data.anac_api.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UtilisateurResponse(
        UUID id,
        String nom,
        String prenom,
        String email,
        String telephone
) {
}
