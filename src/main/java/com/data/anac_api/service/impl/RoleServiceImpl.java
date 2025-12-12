package com.data.anac_api.service.impl;

import com.data.anac_api.dto.request.RoleRequest;
import com.data.anac_api.dto.response.RoleResponse;
import com.data.anac_api.entity.Role;
import com.data.anac_api.exception.RessourceNotFoundException;
import com.data.anac_api.mappers.RoleMapper;
import com.data.anac_api.repository.RoleRepository;
import com.data.anac_api.service.RoleService;
import com.data.anac_api.utils.DataResponse;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    public RoleServiceImpl(RoleRepository roleRepository, RoleMapper roleMapper) {
        this.roleRepository = roleRepository;
        this.roleMapper = roleMapper;
    }

    @Override
    public DataResponse<RoleResponse> create(RoleRequest request) {
        Role role = roleMapper.toEntity(request);
        roleRepository.save(role);
        return new DataResponse<>(new Date(), false, "Role created successfully", roleMapper.toResponse(role));
    }

    @Override
    public DataResponse<RoleResponse> update(UUID roleId, RoleRequest request) {
        Role role = roleRepository.findByTrackingId(roleId)
                .orElseThrow(() -> new RessourceNotFoundException("Role not found"));
        roleMapper.toEntity(request);
        roleRepository.save(role);
        return new DataResponse<>(new Date(), false, "Role updated successfully", roleMapper.toResponse(role));
    }

    @Override
    public DataResponse<RoleResponse> getOneById(UUID roleId) {
        Role role = roleRepository.findByTrackingId(roleId)
                .orElseThrow(() -> new RessourceNotFoundException("Role not found"));
        return new DataResponse<>(new Date(), false, "Role found", roleMapper.toResponse(role));
    }

    @Override
    public DataResponse<List<RoleResponse>> findAll() {
        List<RoleResponse> roles = roleRepository.findAll()
                .stream()
                .map(roleMapper::toResponse)
                .collect(Collectors.toList());
        return new DataResponse<>(new Date(), false, "Roles found", roles);
    }

    @Override
    public DataResponse<Void> delete(UUID roleId) {
        Role role = roleRepository.findByTrackingId(roleId)
                .orElseThrow(() -> new RessourceNotFoundException("Role not found"));
        roleRepository.delete(role);
        return new DataResponse<>(new Date(), false, "Role deleted successfully", null);
    }
}