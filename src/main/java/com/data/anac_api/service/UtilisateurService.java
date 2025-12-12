package com.data.anac_api.service;

import com.data.anac_api.dto.request.UtilisateurRequest;
import com.data.anac_api.dto.response.UtilisateurResponse;
import com.data.anac_api.utils.DataResponse;
import java.util.List;
import java.util.UUID;

public interface UtilisateurService {

    DataResponse<UtilisateurResponse> create(UtilisateurRequest request);
    DataResponse<UtilisateurResponse> update(UUID trackingId, UtilisateurRequest request);
    DataResponse<UtilisateurResponse> getOneByTrackingId(UUID trackingId);
    DataResponse<List<UtilisateurResponse>> findAll();
    DataResponse<Void> delete(UUID trackingId);
    DataResponse<Void> restore(UUID trackingId);
    DataResponse<Void> deleteDefinitively(UUID trackingId);
    long nombreUtilisateur();
}
