package com.data.anac_api.mappers;

import com.data.anac_api.dto.request.LoginRequest;
import com.data.anac_api.dto.request.UtilisateurRequest;
import com.data.anac_api.dto.response.UtilisateurResponse;
import com.data.anac_api.entity.Utilisateur;
import com.data.anac_api.exception.BadRequestException;
import com.data.anac_api.exception.RessourceNotFoundException;
import com.data.anac_api.repository.UtilisateurRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UtilisateurMapper {

    private final UtilisateurRepository utilisateurRepository;

    public UtilisateurMapper(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    public Utilisateur toEntity(LoginRequest request) {

        return utilisateurRepository.findByEmail(request.email())
                .orElseThrow(() -> new RessourceNotFoundException("Utilisateur non trouvé"));
    }

    public Utilisateur toEntity(UtilisateurRequest request) {
        if (request == null) throw new BadRequestException("Requête invalide");

        return Utilisateur.builder()
                .trackingId(UUID.randomUUID())
                .nom(request.nom())
                .prenoms(request.prenom())
                .email(request.email())
                .build();
        
    }

    public UtilisateurResponse toResponse(Utilisateur entity) {
        if (entity == null) throw new BadRequestException("Données invalides");

        return UtilisateurResponse.builder()
                .id(entity.getTrackingId())
                .nom(entity.getNom())
                .prenom(entity.getPrenoms())
                .email(entity.getEmail())

                .build();
    }

}
