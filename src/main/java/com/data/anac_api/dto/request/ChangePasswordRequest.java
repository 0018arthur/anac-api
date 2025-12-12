package com.data.anac_api.dto.request;

public record ChangePasswordRequest(
        String oldPassword,
        String newPassword,
        String confirmPassword
) {
}