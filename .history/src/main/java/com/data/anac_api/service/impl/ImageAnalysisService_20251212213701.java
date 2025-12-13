package com.data.anac_api.service.impl;

import com.data.anac_api.enums.PrioriteIncident;
import com.data.anac_api.enums.TypeIncident;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ImageAnalysisService {

    private final WebClient webClient;

    @Value("${ai.huggingface.api-url}")
    private String HUGGING_FACE_API_URL;

    @Value("${ai.huggingface.api-token}")
    private String API_TOKEN;

    @Value("${ai.huggingface.model-name}")
    private String MODEL_NAME;

    public ImageAnalysisService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public Mono<Map<String, Object>> analyzeImageForIncident(MultipartFile imageFile) {
        // Vérifier si Hugging Face est configuré
        if (HUGGING_FACE_API_URL == null || HUGGING_FACE_API_URL.isEmpty() ||
            API_TOKEN == null || API_TOKEN.isEmpty() ||
            MODEL_NAME == null || MODEL_NAME.isEmpty()) {
            log.warn("Hugging Face API non configurée. L'analyse d'image sera ignorée.");
            return Mono.just(Map.of("error", "Hugging Face API non configurée"));
        }

        try {
            byte[] imageBytes = imageFile.getBytes();

            return webClient.post()
                    .uri(HUGGING_FACE_API_URL + MODEL_NAME)
                    .header("Authorization", "Bearer " + API_TOKEN)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .bodyValue(imageBytes)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .map(response -> {
                        Map<String, Object> result = new HashMap<>();
                        result.put("labels", response);
                        return result;
                    })
                    .doOnSuccess(response -> log.info("Analyse d'image réussie: {}", response))
                    .doOnError(error -> log.error("Erreur lors de l'analyse d'image", error))
                    .onErrorResume(error -> Mono.just(Map.of("error", error.getMessage())));

        } catch (IOException e) {
            log.error("Erreur de lecture du fichier image", e);
            return Mono.just(Map.of("error", "Erreur de lecture du fichier"));
        }
    }

    /**
     * Génère une analyse détaillée de l'incident basée sur l'image et la description
     * Cette analyse sera stockée dans le champ analyseIA
     */
    public String generateDetailedAnalysis(Map<String, Object> analysisResult,
                                           String description,
                                           TypeIncident typeIncident,
                                           PrioriteIncident priorite) {
        StringBuilder analysis = new StringBuilder();

        analysis.append("=== ANALYSE IA DE L'INCIDENT ===\n\n");

        // 1. Analyse de l'image
        if (analysisResult != null && !analysisResult.containsKey("error")) {
            analysis.append("📸 ANALYSE DE L'IMAGE:\n");

            List<Map<String, Object>> labels = (List<Map<String, Object>>) analysisResult.get("labels");
            if (labels != null && !labels.isEmpty()) {
                analysis.append("Éléments détectés:\n");

                // Trier les labels par score de confiance
                List<Map<String, Object>> topLabels = labels.stream()
                        .limit(5)
                        .collect(Collectors.toList());

                for (Map<String, Object> label : topLabels) {
                    String labelName = (String) label.get("label");
                    Double score = (Double) label.get("score");
                    analysis.append(String.format("  • %s (confiance: %.1f%%)\n",
                            labelName, score * 100));
                }
            }
            analysis.append("\n");
        }

        // 2. Classification de l'incident
        analysis.append("🏷️ CLASSIFICATION:\n");
        analysis.append(String.format("  • Type: %s\n", typeIncident.name()));
        analysis.append(String.format("  • Priorité: %s\n\n", priorite.name()));

        // 3. Évaluation de la gravité
        analysis.append("⚠️ ÉVALUATION DE LA GRAVITÉ:\n");
        String gravityAssessment = assessGravity(analysisResult, description, typeIncident, priorite);
        analysis.append(gravityAssessment).append("\n\n");

        // 4. Recommandations
        analysis.append("💡 RECOMMANDATIONS:\n");
        String recommendations = generateRecommendations(typeIncident, priorite, analysisResult, description);
        analysis.append(recommendations).append("\n\n");

        // 5. Actions suggérées
        analysis.append("✓ ACTIONS SUGGÉRÉES:\n");
        String actions = suggestActions(typeIncident, priorite);
        analysis.append(actions).append("\n");

        // 6. Horodatage
        analysis.append("\n---\n");
        analysis.append("Analyse générée le: ").append(java.time.LocalDateTime.now().toString());

        return analysis.toString();
    }

    /**
     * Évalue la gravité de l'incident
     */
    private String assessGravity(Map<String, Object> analysisResult,
                                 String description,
                                 TypeIncident type,
                                 PrioriteIncident priorite) {
        StringBuilder assessment = new StringBuilder();

        String labels = analysisResult != null ? analysisResult.toString().toLowerCase() : "";
        String descLower = description != null ? description.toLowerCase() : "";

        switch (priorite) {
            case CRITIQUE:
                assessment.append("  🔴 SITUATION CRITIQUE DÉTECTÉE\n");
                assessment.append("  • Danger immédiat pour la sécurité ou la vie\n");
                assessment.append("  • Intervention urgente requise\n");
                assessment.append("  • Mobilisation immédiate des services d'urgence recommandée\n");

                if (labels.contains("fire") || descLower.contains("feu") || descLower.contains("incendie")) {
                    assessment.append("  • Risque d'incendie détecté - contacter les pompiers\n");
                }
                if (labels.contains("injury") || descLower.contains("blessé")) {
                    assessment.append("  • Présence de blessés - contacter les services médicaux\n");
                }
                break;

            case ELEVEE:
                assessment.append("  🟠 SITUATION SÉRIEUSE\n");
                assessment.append("  • Risque significatif identifié\n");
                assessment.append("  • Intervention rapide nécessaire (sous 24-48h)\n");
                assessment.append("  • Surveillance accrue recommandée\n");
                break;

            case MOYENNE:
                assessment.append("  🟡 SITUATION À SURVEILLER\n");
                assessment.append("  • Problème nécessitant une attention\n");
                assessment.append("  • Intervention dans un délai raisonnable (3-7 jours)\n");
                assessment.append("  • Pas de danger immédiat identifié\n");
                break;

            case FAIBLE:
                assessment.append("  🟢 SITUATION MINEURE\n");
                assessment.append("  • Problème d'inconfort ou esthétique\n");
                assessment.append("  • Peut être traité selon la planification normale\n");
                assessment.append("  • Aucun risque immédiat\n");
                break;
        }

        return assessment.toString();
    }

    /**
     * Génère des recommandations spécifiques
     */
    private String generateRecommendations(TypeIncident type,
                                           PrioriteIncident priorite,
                                           Map<String, Object> analysisResult,
                                           String description) {
        StringBuilder recommendations = new StringBuilder();

        switch (type) {
            case SECURITY_BREACH:
            case RUNWAY_INCURSION:
                recommendations.append("  • Sécuriser le périmètre immédiatement\n");
                recommendations.append("  • Signaler aux autorités OACI compétentes\n");
                recommendations.append("  • Éviter l'accès à la zone concernée\n");
                if (priorite == PrioriteIncident.CRITIQUE) {
                    recommendations.append("  • Évacuer si nécessaire\n");
                }
                break;

            case FACILITY_MAINTENANCE:
                recommendations.append("  • Évaluer les dommages structurels\n");
                recommendations.append("  • Mettre en place une signalisation temporaire\n");
                recommendations.append("  • Planifier les travaux de réparation\n");
                break;

            case ENVIRONMENTAL:
                recommendations.append("  • Identifier la source de pollution\n");
                recommendations.append("  • Évaluer l'impact environnemental\n");
                recommendations.append("  • Prévoir un nettoyage approprié\n");
                break;

            case PASSENGER_SAFETY:
                recommendations.append("  • Contacter les services de santé\n");
                recommendations.append("  • Assurer la sécurité des passagers\n");
                recommendations.append("  • Suivre les protocoles médicaux OACI\n");
                break;

            case FOD:
                recommendations.append("  • Inspecter immédiatement la zone opérationnelle\n");
                recommendations.append("  • Retirer tout corps étranger détecté\n");
                recommendations.append("  • Vérifier l'intégrité des surfaces\n");
                break;

            case BIRD_STRIKE:
                recommendations.append("  • Évaluer les dégâts potentiels sur les aéronefs\n");
                recommendations.append("  • Activer le plan de gestion de la faune\n");
                recommendations.append("  • Documenter l'incident selon OACI Annexe 14\n");
                break;

            case GROUND_HANDLING:
                recommendations.append("  • Évaluer l'impact sur les opérations au sol\n");
                recommendations.append("  • Coordonner avec les équipes de handling\n");
                recommendations.append("  • Vérifier les équipements GSE\n");
                break;

            default:
                recommendations.append("  • Analyser la situation en détail\n");
                recommendations.append("  • Déterminer les ressources nécessaires\n");
                recommendations.append("  • Établir un plan d'action conforme OACI\n");
        }

        return recommendations.toString();
    }

    /**
     * Suggère des actions concrètes
     */
    private String suggestActions(TypeIncident type, PrioriteIncident priorite) {
        StringBuilder actions = new StringBuilder();

        // Actions basées sur la priorité
        if (priorite == PrioriteIncident.CRITIQUE || priorite == PrioriteIncident.ELEVEE) {
            actions.append("  1. Assigner immédiatement à un technicien qualifié\n");
            actions.append("  2. Informer les responsables et parties prenantes\n");
            actions.append("  3. Mobiliser les ressources nécessaires\n");
            actions.append("  4. Mettre en place un suivi en temps réel\n");
        } else {
            actions.append("  1. Ajouter à la file d'attente de traitement\n");
            actions.append("  2. Planifier l'intervention selon les priorités\n");
            actions.append("  3. Rassembler les informations complémentaires si nécessaire\n");
        }

        // Actions spécifiques au type OACI
        actions.append("  5. ");
        switch (type) {
            case SECURITY_BREACH:
            case RUNWAY_INCURSION:
                actions.append("Coordonner avec les forces de sécurité aéroportuaire\n");
                break;
            case FACILITY_MAINTENANCE:
                actions.append("Évaluer par un ingénieur qualifié OACI\n");
                break;
            case ENVIRONMENTAL:
                actions.append("Consulter un expert environnemental aviation\n");
                break;
            case PASSENGER_SAFETY:
                actions.append("Impliquer les services sanitaires et médicaux\n");
                break;
            case FOD:
                actions.append("Déployer équipe FOD avec inspection complète\n");
                break;
            case BIRD_STRIKE:
                actions.append("Activer protocole wildlife management\n");
                break;
            case GROUND_HANDLING:
                actions.append("Coordonner avec les équipes handling et GSE\n");
                break;
            default:
                actions.append("Déterminer les expertises requises selon OACI\n");
        }

        return actions.toString();
    }

    /**
     * Suggests aviation-specific incident type based on AI image analysis
     * OACI/ICAO compliant categorization for airport operations
     */
    public String suggestIncidentType(Map<String, Object> analysisResult) {
        if (analysisResult.containsKey("error")) {
            return TypeIncident.OTHER.name();
        }

        String labels = analysisResult.toString().toLowerCase();

        // RUNWAY_INCURSION - Unauthorized runway access
        if (labels.contains("runway") || labels.contains("aircraft on ground") ||
            labels.contains("taxiway") || labels.contains("unauthorized") ||
            labels.contains("piste") || labels.contains("incursion")) {
            return TypeIncident.RUNWAY_INCURSION.name();
        }

        // FOD - Foreign Object Debris on operational surfaces
        else if (labels.contains("debris") || labels.contains("object") ||
                 labels.contains("metal") || labels.contains("tire") ||
                 labels.contains("tool") || labels.contains("fod") ||
                 labels.contains("debris sur piste")) {
            return TypeIncident.FOD.name();
        }

        // BIRD_STRIKE - Wildlife hazard
        else if (labels.contains("bird") || labels.contains("animal") ||
                 labels.contains("wildlife") || labels.contains("oiseau") ||
                 labels.contains("faune")) {
            return TypeIncident.BIRD_STRIKE.name();
        }

        // SECURITY_BREACH - Security violations
        else if (labels.contains("unauthorized access") || labels.contains("breach") ||
                 labels.contains("intrusion") || labels.contains("fence") ||
                 labels.contains("perimeter") || labels.contains("sécurité") ||
                 labels.contains("violation")) {
            return TypeIncident.SECURITY_BREACH.name();
        }

        // FACILITY_MAINTENANCE - Infrastructure issues
        else if (labels.contains("crack") || labels.contains("damage") ||
                 labels.contains("broken") || labels.contains("building") ||
                 labels.contains("structure") || labels.contains("infrastructure") ||
                 labels.contains("fissure") || labels.contains("endommagé")) {
            return TypeIncident.FACILITY_MAINTENANCE.name();
        }

        // GROUND_HANDLING - Ground support equipment
        else if (labels.contains("vehicle") || labels.contains("equipment") ||
                 labels.contains("tug") || labels.contains("loader") ||
                 labels.contains("baggage cart") || labels.contains("ground support") ||
                 labels.contains("véhicule") || labels.contains("équipement")) {
            return TypeIncident.GROUND_HANDLING.name();
        }

        // PASSENGER_SAFETY - Medical or safety emergencies
        else if (labels.contains("medical") || labels.contains("injury") ||
                 labels.contains("ambulance") || labels.contains("emergency") ||
                 labels.contains("passenger") || labels.contains("médical") ||
                 labels.contains("blessure") || labels.contains("passager")) {
            return TypeIncident.PASSENGER_SAFETY.name();
        }

        // ENVIRONMENTAL - Cleanliness and environmental issues
        else if (labels.contains("trash") || labels.contains("waste") ||
                 labels.contains("spill") || labels.contains("pollution") ||
                 labels.contains("dirty") || labels.contains("déchet") ||
                 labels.contains("propreté")) {
            return TypeIncident.ENVIRONMENTAL.name();
        }

        // Default to OTHER for unclassified incidents
        return TypeIncident.OTHER.name();
    }

    public PrioriteIncident analyzePriority(Map<String, Object> analysisResult, String description, TypeIncident type) {
        if (analysisResult.containsKey("error")) {
            return PrioriteIncident.MOYENNE;
        }

        String labels = analysisResult.toString().toLowerCase();
        String descLower = description != null ? description.toLowerCase() : "";

        // Aviation-specific critical keywords (OACI safety standards)
        Set<String> criticalKeywords = Set.of(
                "runway incursion", "aircraft collision", "fire", "flame", "explosion",
                "death", "fatality", "multiple injuries", "structural collapse",
                "incursion piste", "collision avion", "feu", "incendie", "mort", "effondrement"
        );

        Set<String> highKeywords = Set.of(
                "fod", "foreign object", "bird strike", "wildlife", "smoke", "injury",
                "medical emergency", "fuel spill", "hazardous material", "unauthorized access",
                "corps étranger", "péril animalier", "oiseau", "fumée", "blessure",
                "urgence médicale", "fuite carburant", "accès non autorisé"
        );

        Set<String> mediumKeywords = Set.of(
                "equipment failure", "malfunction", "crack", "damage", "delay",
                "panne équipement", "dysfonctionnement", "fissure", "endommagé", "retard"
        );

        // Check keywords in labels and description
        for (String keyword : criticalKeywords) {
            if (labels.contains(keyword) || descLower.contains(keyword)) {
                log.info("Priorité CRITIQUE détectée pour mot-clé aviation: {}", keyword);
                return PrioriteIncident.CRITIQUE;
            }
        }

        for (String keyword : highKeywords) {
            if (labels.contains(keyword) || descLower.contains(keyword)) {
                log.info("Priorité ELEVEE détectée pour mot-clé aviation: {}", keyword);
                return PrioriteIncident.ELEVEE;
            }
        }

        // Aviation incident type-based priority (OACI severity levels)
        if (type == TypeIncident.RUNWAY_INCURSION || type == TypeIncident.SECURITY_BREACH) {
            log.info("Priorité CRITIQUE automatique pour type: {}", type);
            return PrioriteIncident.CRITIQUE;
        }

        if (type == TypeIncident.FOD || type == TypeIncident.BIRD_STRIKE ||
            type == TypeIncident.PASSENGER_SAFETY) {
            log.info("Priorité ELEVEE automatique pour type: {}", type);
            return PrioriteIncident.ELEVEE;
        }

        if (type == TypeIncident.FACILITY_MAINTENANCE || type == TypeIncident.GROUND_HANDLING) {
            return PrioriteIncident.MOYENNE;
        }

        if (type == TypeIncident.ENVIRONMENTAL || type == TypeIncident.OTHER) {
            return PrioriteIncident.FAIBLE;
        }

        for (String keyword : mediumKeywords) {
            if (labels.contains(keyword) || descLower.contains(keyword)) {
                return PrioriteIncident.MOYENNE;
            }
        }

        return PrioriteIncident.FAIBLE;
    }

    public PrioriteIncident analyzePriorityWithoutImage(String description, TypeIncident type) {
        String descLower = description != null ? description.toLowerCase() : "";

        Set<String> criticalKeywords = Set.of(
                "urgent", "critique", "grave", "danger", "risque", "vie", "mort",
                "feu", "incendie", "explosion", "effondrement", "inondation", "blessé"
        );

        Set<String> highKeywords = Set.of(
                "important", "prioritaire", "accident", "fuite", "gaz", "bloqué"
        );

        for (String keyword : criticalKeywords) {
            if (descLower.contains(keyword)) {
                return PrioriteIncident.CRITIQUE;
            }
        }

        for (String keyword : highKeywords) {
            if (descLower.contains(keyword)) {
                return PrioriteIncident.ELEVEE;
            }
        }

        if (type == TypeIncident.SECURITY_BREACH || type == TypeIncident.PASSENGER_SAFETY ||
            type == TypeIncident.RUNWAY_INCURSION) {
            return PrioriteIncident.ELEVEE;
        }

        return PrioriteIncident.MOYENNE;
    }

    /**
     * Génère une analyse sans image (basée uniquement sur la description et le type)
     */
    public String generateAnalysisWithoutImage(String description,
                                               TypeIncident typeIncident,
                                               PrioriteIncident priorite) {
        return generateDetailedAnalysis(null, description, typeIncident, priorite);
    }
}