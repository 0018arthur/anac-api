package com.data.anac_api.repository;

import com.data.anac_api.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByTrackingId(UUID trackingId);

    Optional<Role> findByNom(String nom);

    @Query("SELECT r FROM Role r WHERE r.nom = :nom")
    Role findRoleByNom(String nom);

    boolean existsByNom(String nom);
}
