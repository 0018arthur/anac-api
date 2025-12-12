package com.data.anac_api.repository;

import com.data.anac_api.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    @Query("SELECT u FROM Utilisateur u WHERE u.email = :email")
    Optional<Utilisateur> findByEmail(String email);

    @Query("SELECT u FROM Utilisateur u LEFT JOIN FETCH u.utilisateurRoles WHERE u.email= :email")
    Optional<Utilisateur> findByEmailOrMatriculeWithRoles(@Param("email") String email);

    Optional<Utilisateur> findByTrackingId(UUID trackingId);

    Optional<Utilisateur> findByAccessToken(String token);

    @Modifying
    @Transactional
    @Query("UPDATE Utilisateur u SET u.accessToken = NULL, u.isTokenValid = false")
    void revokeAllTokens();

    @Query("SELECT COUNT(u) = 0 FROM Utilisateur u WHERE u.email = :email")
    boolean existsByEmail(String email);

    @Transactional
    @Modifying
    @Query("UPDATE Utilisateur u SET u.accessToken = :accessToken, " +
            "u.isTokenValid = TRUE " +
            "WHERE u.id = :userId")
    void updateTokens(Long userId, String accessToken);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE Utilisateur u SET u.accessToken = :accessToken, " +
            "u.isTokenValid = :isValid " +
            "WHERE u.email = :email")
    void updateTokensByEmail(
            @Param("email") String email,
            @Param("accessToken") String accessToken,
            @Param("refreshToken") String refreshToken,
            @Param("isValid") boolean isValid
    );

}
