package com.data.anac_api.repository;

import com.data.anac_api.entity.UtilisateurRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UtilisateurRoleRepository extends JpaRepository<UtilisateurRole, Long> {
    Optional<UtilisateurRole> findUtilisateurRoleByRole_Nom(String roleNom);
}
