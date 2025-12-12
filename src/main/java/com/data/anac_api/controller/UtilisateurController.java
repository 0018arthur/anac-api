package com.data.anac_api.controller;


import com.data.anac_api.dto.request.UtilisateurRequest;
import com.data.anac_api.dto.response.UtilisateurResponse;
import com.data.anac_api.service.UtilisateurService;
import com.data.anac_api.utils.DataResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/personnes")
public class UtilisateurController {

    private final UtilisateurService service;

    public UtilisateurController(UtilisateurService service) {
        this.service = service;
    }

    @PutMapping("/{trackingId}")
    public ResponseEntity<DataResponse<UtilisateurResponse>> update(
            @PathVariable UUID trackingId,
            @RequestBody UtilisateurRequest request) {
        return ResponseEntity.ok(service.update(trackingId, request));
    }

    @GetMapping("/{trackingId}")
    public ResponseEntity<DataResponse<UtilisateurResponse>> findById(@PathVariable UUID trackingId) {
        return ResponseEntity.ok(service.getOneByTrackingId(trackingId));
    }

    @GetMapping
    public ResponseEntity<DataResponse<List<UtilisateurResponse>>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }


    @DeleteMapping("/{trackingId}")
    public ResponseEntity<DataResponse<Void>> delete(@PathVariable UUID trackingId) {
        return ResponseEntity.ok(service.delete(trackingId));
    }


    @PutMapping("/restore/{trackingId}")
    public ResponseEntity<DataResponse<Void>> restore(@PathVariable UUID trackingId) {
        return ResponseEntity.ok(service.restore(trackingId));
    }


    @DeleteMapping("/delete-def/{trackingId}")
    public ResponseEntity<DataResponse<Void>> deleteDef(@PathVariable UUID trackingId) {
        return ResponseEntity.ok(service.deleteDefinitively(trackingId));
    }

    @GetMapping("/nombreEtudiants")
    public ResponseEntity<Long> countUsers() {
        return ResponseEntity.ok(service.nombreUtilisateur());
    }
}