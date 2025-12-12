package com.data.anac_api.dto.request;

public record VerificationRequest(
        String email,
        String code
) {
}
