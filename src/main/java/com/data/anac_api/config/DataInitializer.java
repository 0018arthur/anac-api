package com.data.anac_api.config;

import com.data.anac_api.entity.Role;
import com.data.anac_api.entity.Utilisateur;
import com.data.anac_api.entity.UtilisateurRole;
import com.data.anac_api.repository.RoleRepository;
import com.data.anac_api.repository.UtilisateurRepository;
import com.data.anac_api.repository.UtilisateurRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Initialise les données de base de l'application au démarrage
 * Crée un utilisateur administrateur par défaut UNE SEULE FOIS
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final UtilisateurRoleRepository utilisateurRoleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    private static final String ADMIN_EMAIL = "admin@anac.tg";

    @Override
    public void run(String... args) throws Exception {
        log.info("🚀 Initialisation des données de base...");
        
        try {
            // Vérifier si c'est la première initialisation
            boolean isFirstInitialization = isFirstInitialization();
            
            if (isFirstInitialization) {
                log.info("📍 Première initialisation de l'application détectée");
                
                // 1. Créer les rôles par défaut
                initializeRoles();
                
                // 2. Créer l'utilisateur administrateur
                initializeAdminUser();
                
                log.info("✅ Initialisation des données terminée avec succès");
            } else {
                log.info("ℹ️  Application déjà initialisée - Aucune création nécessaire");
            }
        } catch (Exception e) {
            log.error("❌ Erreur lors de l'initialisation des données", e);
            throw e;
        }
    }

    /**
     * Vérifie si c'est la première initialisation de l'application
     * @return true si l'admin n'existe pas encore, false sinon
     */
    private boolean isFirstInitialization() {
        return utilisateurRepository.findByEmail(ADMIN_EMAIL).isEmpty();
    }

    /**
     * Initialise les rôles par défaut
     */
    private void initializeRoles() {
        String[] roleNames = {"ADMIN", "USER", "OWNER"};
        
        for (String roleName : roleNames) {
            if (roleRepository.findByNom(roleName).isEmpty()) {
                Role role = Role.builder()
                        .nom(roleName)
                        .trackingId(UUID.randomUUID())
                        .build();
                roleRepository.save(role);
                log.info("✓ Rôle créé: {}", roleName);
            } else {
                log.info("✓ Rôle existe déjà: {}", roleName);
            }
        }
    }

    /**
     * Initialise l'utilisateur administrateur par défaut UNE SEULE FOIS
     */
    private void initializeAdminUser() {
        // Créer l'utilisateur administrateur
        Utilisateur admin = Utilisateur.builder()
                .nom("Admin")
                .prenoms("ANAC")
                .email(ADMIN_EMAIL)
                .password(passwordEncoder.encode("Admin123!"))
                .trackingId(UUID.randomUUID())
                .enabled(true)
                .build();

        admin = utilisateurRepository.save(admin);
        log.info("✓ Utilisateur administrateur créé: {} ({})", admin.getNom(), admin.getEmail());

        // Assigner le rôle ADMIN
        Role adminRole = roleRepository.findByNom("ADMIN")
                .orElseThrow(() -> new RuntimeException("Le rôle ADMIN n'a pas pu être trouvé"));

        UtilisateurRole utilisateurRole = UtilisateurRole.builder()
                .utilisateur(admin)
                .role(adminRole)
                .trackingId(UUID.randomUUID())
                .build();

        utilisateurRoleRepository.save(utilisateurRole);
        log.info("✓ Rôle ADMIN assigné à l'utilisateur: {}", ADMIN_EMAIL);
    }
}
