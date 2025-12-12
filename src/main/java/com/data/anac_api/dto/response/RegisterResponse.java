package com.data.anac_api.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record RegisterResponse(
        UUID id,
        String nom,
        String prenoms,
        String email
) {
}
