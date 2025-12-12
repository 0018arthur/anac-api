package com.data.anac_api.service.impl;

import com.data.anac_api.dto.response.IncidentResponseDTO;
import com.data.anac_api.enums.PrioriteIncident;
import com.data.anac_api.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender emailSender;

    @Value("${app.alert.email:toto@gmail.com}")
    private String alertEmail;

    public EmailServiceImpl(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Override
    public void envoyerMessage(String to, String userName, String verificationCode) {
        String expirationTime = "15 minutes";

        String htmlContent = """
                Bonjour %s,
                
                Merci pour votre inscription. Voici votre code de vérification :
                
                %s
                
                Ce code expirera dans %s.
                
                Si vous n'avez pas demandé cette vérification, veuillez ignorer ce message.
                
                Cordialement,
                %s L'équipe de support
                
                """.formatted(userName, verificationCode, expirationTime, java.time.Year.now().getValue());

        MimeMessage message = emailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Vérification de votre compte");
            helper.setText(htmlContent, true);

            // Ajout du logo
            ClassPathResource logoImage = new ClassPathResource("static/logo.jpg");
            helper.addInline("logoImage", logoImage);

            emailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email", e);
        }
    }

    @Override
    public void envoyerAlertePriorite(IncidentResponseDTO incident) {
        log.info("Envoi d'alerte pour incident prioritaire - ID: {}, Priorité: {}",
                incident.id(), incident.priorite());

        String prioriteLabel = getPrioriteLabel(incident.priorite());
        String prioriteColor = getPrioriteColor(incident.priorite());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");
        String dateCreation = incident.createdAt() != null ?
                incident.createdAt().format(formatter) : "Non spécifié";

        String htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body {
                    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                    background-color: #eceff1;
                    margin: 0;
                    padding: 20px;
                    display: flex;
                    justify-content: center;
                }

                .card {
                    background-color: #ffffff;
                    width: 400px;
                    border-radius: 10px;
                    box-shadow: 0 5px 15px rgba(0,0,0,0.2);
                    overflow: hidden;
                    border-left: 6px solid %s;
                }

                .card-header {
                    background-color: %s;
                    padding: 15px 20px;
                    text-align: center;
                    font-size: 20px;
                    font-weight: bold;
                }

                .priority-badge {
                    display: inline-block;
                    background-color: %s;
                    color: white;
                    padding: 6px 12px;
                    border-radius: 20px;
                    font-weight: bold;
                    margin-top: 10px;
                    font-size: 14px;
                }

                .card-content {
                    padding: 20px;
                    color: #333;
                    font-size: 14px;
                }

                .card-row {
                    margin-bottom: 12px;
                }

                .card-label {
                    font-weight: bold;
                    color: #555;
                }

                .card-value {
                    margin-left: 6px;
                    color: #212121;
                }

                .card-footer {
                    text-align: center;
                    font-size: 12px;
                    color: #777;
                    padding: 10px 20px;
                    border-top: 1px solid #ddd;
                }
            </style>
        </head>
        <body>

        <div class="card">
            <div class="card-header">
               Incident Prioritaire
                <div class="priority-badge">%s</div>
            </div>

            <div class="card-content">
                <div class="card-row"><span class="card-label">Titre:</span> <span class="card-value">%s</span></div>
                <div class="card-row"><span class="card-label">Type:</span> <span class="card-value">%s</span></div>
                <div class="card-row"><span class="card-label">Localisation:</span> <span class="card-value">%s</span></div>
                <div class="card-row"><span class="card-label">Date:</span> <span class="card-value">%s</span></div>
            </div>

            <div class="card-footer">
                Email généré automatiquement — © %d Système de Gestion des Incidents
            </div>
        </div>

        </body>
        </html>
        """.formatted(
                prioriteColor,
                prioriteColor,
                prioriteColor,
                prioriteLabel.toUpperCase(),
                incident.titre(),
                incident.typeIncident(),
                incident.localisation() != null ? incident.localisation() : "Non spécifié",
                dateCreation,
                java.time.Year.now().getValue()
        );

        MimeMessage message = emailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(alertEmail);
            helper.setSubject("🚨 ALERTE - Incident " + prioriteLabel + " - " + incident.titre());
            helper.setText(htmlContent, true);

            // Ajout du logo si disponible
            try {
                ClassPathResource logoImage = new ClassPathResource("static/logo.jpg");
                if (logoImage.exists()) {
                    helper.addInline("logoImage", logoImage);
                }
            } catch (Exception e) {
                log.warn("Logo non trouvé, l'email sera envoyé sans logo");
            }

            emailSender.send(message);

        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de l'alerte email", e);
            throw new RuntimeException("Erreur lors de l'envoi de l'alerte email", e);
        }
    }

    private String getPrioriteLabel(PrioriteIncident priorite) {
        return switch (priorite) {
            case CRITIQUE -> "CRITIQUE";
            case ELEVEE -> "ÉLEVÉE";
            case MOYENNE -> "MOYENNE";
            case FAIBLE -> "FAIBLE";
        };
    }

    private String getPrioriteColor(PrioriteIncident priorite) {
        return switch (priorite) {
            case CRITIQUE -> "#DC2626"; // Rouge foncé
            case ELEVEE -> "#EA580C"; // Orange
            case MOYENNE -> "#F59E0B"; // Jaune-orange
            case FAIBLE -> "#10B981"; // Vert
        };
    }
}