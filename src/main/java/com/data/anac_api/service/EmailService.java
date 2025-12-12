package com.data.anac_api.service;

import com.data.anac_api.dto.response.IncidentResponseDTO;

public interface EmailService {
    void envoyerMessage(String to, String userName, String verificationCode);

    void envoyerAlertePriorite(IncidentResponseDTO incident);
}