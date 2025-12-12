package com.data.anac_api.dto.request;

import lombok.Builder;

import java.util.List;

@Builder
public record
RegisterRequest(
        String nom,
        String prenoms,
        String password,
        String email,
        String telephone,
        List<String> roles
) {}

