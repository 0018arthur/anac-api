package com.data.anac_api.dto.request;

public record UtilisateurRequest(
        String nom,
        String prenom,
        String email,
        String telephone,
        String nci,
        String matricule
) {
}
