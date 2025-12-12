package com.data.anac_api.service.impl;


import com.data.anac_api.dto.request.UtilisateurRequest;
import com.data.anac_api.dto.response.UtilisateurResponse;
import com.data.anac_api.entity.Utilisateur;
import com.data.anac_api.exception.RessourceNotFoundException;
import com.data.anac_api.mappers.UtilisateurMapper;
import com.data.anac_api.repository.UtilisateurRepository;
import com.data.anac_api.service.UtilisateurService;
import com.data.anac_api.utils.DataResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class UtilisateurServiceImpl implements UtilisateurService {

    private final UtilisateurRepository repository;
    private final UtilisateurMapper mapper;

    public UtilisateurServiceImpl(UtilisateurRepository repository, UtilisateurMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public DataResponse<UtilisateurResponse> create(UtilisateurRequest request) {

        Utilisateur entity = mapper.toEntity(request);

        repository.save(entity);


        UtilisateurResponse response = mapper.toResponse(entity);

        return new DataResponse<>(new Date(), false, "Utilisateur créée", response);
    }



    @Override
    public DataResponse<UtilisateurResponse> update(UUID trackingId, UtilisateurRequest request) {

        Utilisateur entity = repository.findByTrackingId(trackingId).orElseThrow(() -> new RessourceNotFoundException("Utilisateur non trouvée avec trackingId: " + trackingId));

        if (!entity.getNom().equals(request.nom())) {
            entity.setNom(request.nom());
        }

        repository.save(entity);



        UtilisateurResponse response = mapper.toResponse(entity);

        return new DataResponse<>(new Date(), false, "Modification effectuée", response);
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<UtilisateurResponse> getOneByTrackingId(UUID trackingId) {

        Utilisateur entity = repository.findByTrackingId(trackingId)
                .orElseThrow(() -> new RessourceNotFoundException("Utilisateur non trouvée avec trackingId: " + trackingId));

        UtilisateurResponse response = mapper.toResponse(entity);

        return new DataResponse<>(new Date(), false, "Trouvée", response);
    }


    @Override
    @Transactional(readOnly = true)
    public DataResponse<List<UtilisateurResponse>> findAll() {

        List<UtilisateurResponse> responses = this.repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();


        return new DataResponse<>(new Date(), false, "Liste renvoyée", responses);

    }

    @Override
    public DataResponse<Void> delete(UUID trackingId) {

        Utilisateur entity = repository.findByTrackingId((trackingId))
                .orElseThrow(() -> new RessourceNotFoundException("Utilisateur non trouvée avec trackingId: " + trackingId));

        return new DataResponse<>(new Date(), false, "Suppression effectuée", null);

    }

    @Override
    public DataResponse<Void> restore(UUID trackingId) {

        Utilisateur entity = repository.findByTrackingId(trackingId)
                .orElseThrow(() -> new RessourceNotFoundException("Utilisateur non trouvée avec trackingId: " + trackingId));

        return new DataResponse<>(new Date(), false, "Restoration effectuée", null);
    }

    @Override
    public DataResponse<Void> deleteDefinitively(UUID trackingId) {

        Utilisateur entity = repository.findByTrackingId(trackingId)
                .orElseThrow(() -> new RessourceNotFoundException("Utilisateur non trouvée avec trackingId: " + trackingId));

        repository.delete(entity);

        return new DataResponse<>(new Date(), false, "Suppression définitive effectuée", null);
    }

    @Override
    public long nombreUtilisateur() {
        return repository.count();
    }
}
