package com.data.anac_api.service.impl;

import com.data.anac_api.dto.request.ChangePasswordRequest;
import com.data.anac_api.dto.request.LoginRequest;
import com.data.anac_api.dto.request.RegisterRequest;
import com.data.anac_api.dto.response.LoginResponse;
import com.data.anac_api.dto.response.RegisterResponse;
import com.data.anac_api.entity.Role;
import com.data.anac_api.entity.Utilisateur;
import com.data.anac_api.entity.UtilisateurRole;
import com.data.anac_api.exception.AlreadyExistException;
import com.data.anac_api.mappers.RegistrationMapper;
import com.data.anac_api.repository.RoleRepository;
import com.data.anac_api.repository.UtilisateurRepository;
import com.data.anac_api.repository.UtilisateurRoleRepository;
import com.data.anac_api.service.AuthService;
import com.data.anac_api.service.EmailService;
import com.data.anac_api.service.JwtService;
import com.data.anac_api.utils.DataResponse;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final RegistrationMapper registrationMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RoleRepository roleRepository;
    private final UtilisateurRoleRepository utilisateurRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UtilisateurRepository utilisateurRepository, RegistrationMapper registrationMapper,
                           AuthenticationManager authenticationManager, JwtService jwtService,
                           RoleRepository roleRepository, UtilisateurRoleRepository utilisateurRoleRepository,
                           EmailService emailService, PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.registrationMapper = registrationMapper;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.roleRepository = roleRepository;
        this.utilisateurRoleRepository = utilisateurRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public DataResponse<RegisterResponse> register(RegisterRequest registerRequest) {
        // Vérification de l'email existant
        if (this.utilisateurRepository.findByEmail(registerRequest.email()).isPresent()) {
            throw new AlreadyExistException("Cet email est déjà relié à un compte");
        }

        // Conversion en entité utilisateur
        Utilisateur utilisateur = registrationMapper.toEntity(registerRequest);

        utilisateur.setEnabled(true);

        // Sauvegarde initiale de l'utilisateur
        utilisateur = utilisateurRepository.save(utilisateur);

        // Gestion des rôles (avec valeur par défaut si null/vide)
        Utilisateur finalUtilisateur = utilisateur;
        List<UtilisateurRole> utilisateurRoles = Optional.ofNullable(registerRequest.roles())
                .orElse(Collections.emptyList())
                .stream()
                .map(roleNom -> {
                    UtilisateurRole utilisateurRole = new UtilisateurRole();
                    utilisateurRole.setTrackingId(UUID.randomUUID());
                    utilisateurRole.setUtilisateur(finalUtilisateur);
                    utilisateurRole.setRole(getRole(roleNom));
                    return utilisateurRole;
                })
                .collect(Collectors.toList());

        // Attribuer ROLE_USER par défaut
        if (utilisateurRoles.isEmpty()) {
            UtilisateurRole defaultRole = new UtilisateurRole();
            defaultRole.setTrackingId(UUID.randomUUID());
            defaultRole.setUtilisateur(utilisateur);
            defaultRole.setRole(getRole("ROLE_OWNER"));
            utilisateurRoles.add(defaultRole);
        }

        // Sauvegarde des rôles
        utilisateurRoleRepository.saveAll(utilisateurRoles);
        utilisateur.setUtilisateurRoles(utilisateurRoles);

        log.info("Inscription réussie pour l'utilisateur {}", utilisateur.getTrackingId());
        return new DataResponse<>(new Date(), false,
                "Inscription réussie", new RegisterResponse(
                utilisateur.getTrackingId(),
                utilisateur.getNom(),
                utilisateur.getPrenoms(),
                utilisateur.getEmail()
        ));
    }

    private Role getRole(String roleNom) {
        return roleRepository.findByNom(roleNom)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setNom(roleNom);
                    newRole.setTrackingId(UUID.randomUUID());
                    return roleRepository.save(newRole);
                });
    }


    @Override
    @Transactional
    public DataResponse<LoginResponse> login(LoginRequest loginRequest) {
        try {
            Utilisateur user = utilisateurRepository.findByEmail(loginRequest.email())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            if (!user.isEnabled()) {
                throw new RuntimeException("Compte non vérifié");
            }

            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.email(),
                            loginRequest.password()
                    )
            );

            if (auth.isAuthenticated()) {
                Map<String, String> tokens = jwtService.generateTokens(loginRequest);

                // Alternative à save() :
                jwtService.updateUserTokens(user.getEmail(), tokens.get("access_token"), tokens.get("refresh_token"));

                // Récupérer le nom et le rôle de l'utilisateur
                String userName = user.getNom();
                String userRole = user.getUtilisateurRoles() != null && !user.getUtilisateurRoles().isEmpty()
                        ? user.getUtilisateurRoles().get(0).getRole().getNom()
                        : "USER";

                return new DataResponse<>(
                        new Date(),
                        false,
                        "Connexion réussie",
                        new LoginResponse(
                                tokens.get("access_token"),
                                tokens.get("refresh_token"),
                                jwtService.tokenExpireIn(),
                                userName,
                                userRole
                        )
                );
            }
        } catch (AuthenticationException e) {
            log.error("Erreur d'authentification", e);
            throw new RuntimeException("Identifiants invalides");
        }

        return new DataResponse<>(new Date(), true, "Échec de l'authentification", null);
    }


    @Override
    public DataResponse<Void> logoutUser(HttpServletRequest request) {
        try {
            log.info("Déconnexion de l'utilisateur");

            final String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Invalid or missing Authorization header");
                return new DataResponse<>(new Date(), false, "Déconnexion réussie", null);
            }

            final String token = authHeader.substring(7);

            return new DataResponse<>(new Date(), false, "Déconnexion réussie", null);
        } catch (Exception e) {
            log.error("Unexpected error during logout: {}", e.getMessage(), e);
            // Déconnexion toujours
            return new DataResponse<>(new Date(), false, "Déconnexion réussie", null);
        }
    }

    @Override
    @Transactional
    public DataResponse<String> changePassword(String email, ChangePasswordRequest request) {
        // Vérifier que les deux nouveaux mots de passe correspondent
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new RuntimeException("Les mots de passe ne correspondent pas");
        }

        // Récupérer l'utilisateur
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Vérifier l'ancien mot de passe
        if (!passwordEncoder.matches(request.oldPassword(), utilisateur.getPassword())) {
            throw new RuntimeException("Ancien mot de passe incorrect");
        }

        // Vérifier que le nouveau mot de passe est différent de l'ancien
        if (passwordEncoder.matches(request.newPassword(), utilisateur.getPassword())) {
            throw new RuntimeException("Le nouveau mot de passe doit être différent de l'ancien");
        }

        // Encoder et mettre à jour le mot de passe
        String encodedPassword = passwordEncoder.encode(request.newPassword());
        utilisateur.setPassword(encodedPassword);
        utilisateurRepository.save(utilisateur);

        // Effacer les tokens
        utilisateur.setAccessToken(null);

        utilisateurRepository.save(utilisateur);

        return new DataResponse<>(new Date(), false, "Mot de passe modifié avec succès", "Mot de passe modifié avec succès");
    }

}
