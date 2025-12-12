package com.data.anac_api.dto.request;

import lombok.Builder;

@Builder
public record
ResetPasswordRequest(
        String token,
        String newPassword
) {}
