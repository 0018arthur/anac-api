package com.data.anac_api.service;

import com.data.anac_api.dto.request.RoleRequest;
import com.data.anac_api.dto.response.RoleResponse;
import com.data.anac_api.utils.DataResponse;

import java.util.List;
import java.util.UUID;

public interface RoleService {
    DataResponse<RoleResponse> create(RoleRequest request);
    DataResponse<RoleResponse> update(UUID roleId, RoleRequest request);
    DataResponse<RoleResponse> getOneById(UUID roleId);
    DataResponse<List<RoleResponse>> findAll();
    DataResponse<Void> delete(UUID roleId);
}