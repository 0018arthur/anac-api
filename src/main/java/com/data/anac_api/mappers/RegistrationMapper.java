package com.data.anac_api.mappers;

import com.data.anac_api.dto.request.RegisterRequest;
import com.data.anac_api.dto.response.RegisterResponse;
import com.data.anac_api.entity.Role;
import com.data.anac_api.entity.Utilisateur;
import com.data.anac_api.entity.UtilisateurRole;
import com.data.anac_api.repository.RoleRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class RegistrationMapper {

    private final BCryptPasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    private RegistrationMapper(BCryptPasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    public Utilisateur toEntity(RegisterRequest request) {
        List<UtilisateurRole> utilisateurRoles = new ArrayList<>();

        if (request.roles() == null || request.roles().isEmpty()) {

            Role ownerRole = roleRepository.findByNom("ROLE_USER")
                    .orElseGet(() -> {
                        Role oRole = Role.builder()
                                .trackingId(UUID.randomUUID())
                                .nom("ROLE_USER")
                                .build();
                        return roleRepository.save(oRole);
                    });

            UtilisateurRole ur = new UtilisateurRole();
            ur.setRole(ownerRole);
            ur.setTrackingId(UUID.randomUUID());
            utilisateurRoles.add(ur);

            //this.urRepository.save(ur);

        } else {
            // Traitement des rôles personnalisés
            utilisateurRoles = request.roles().stream()
                    .map(roleName -> {
                        Optional<Role> roleOpt = roleRepository.findByNom(roleName);
                        if (roleOpt.isPresent()) {
                            UtilisateurRole ur = new UtilisateurRole();
                            ur.setRole(roleOpt.get());
                            ur.setTrackingId(UUID.randomUUID());
                            return ur;
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        // Création de l'utilisateur
        Utilisateur utilisateur = Utilisateur.builder()
                .nom(request.nom())
                .prenoms(request.prenoms())
                .trackingId(UUID.randomUUID())
                .password(passwordEncoder.encode(request.password()))
                .email(request.email())
                .utilisateurRoles(utilisateurRoles)
                .build();

        // Établir la relation bidirectionnelle
        utilisateurRoles.forEach(ur -> ur.setUtilisateur(utilisateur));

        return utilisateur;
    }

    public RegisterResponse toResponse(Utilisateur utilisateur) {
        
        return RegisterResponse.builder()
                .id(utilisateur.getTrackingId())
                .nom(utilisateur.getNom())
                .prenoms(utilisateur.getPrenoms())
                .email(utilisateur.getEmail())
                .build();
    }
}