package com.data.anac_api.mappers;

import com.data.anac_api.dto.request.RoleRequest;
import com.data.anac_api.dto.response.RoleResponse;
import com.data.anac_api.entity.Role;
import com.data.anac_api.entity.UtilisateurRole;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RoleMapper {

    public RoleMapper() {
    }

    public RoleResponse toResponseFromUtilisateurRole(UtilisateurRole utilisateurRole) {
        Role role = utilisateurRole.getRole();
        return RoleResponse.builder()
                .id(role.getTrackingId())
                .name(role.getNom())
                .build();
    }

    public RoleResponse toResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getTrackingId())
                .name(role.getNom())
                .build();
    }

    public Role toEntity(RoleRequest request) {
        return Role.builder()
                .trackingId(UUID.randomUUID())
                .nom("ROLE_"+request.name())
                .build();
    }
}

