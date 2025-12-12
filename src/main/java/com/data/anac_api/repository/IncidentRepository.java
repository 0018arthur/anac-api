package com.data.anac_api.repository;

import com.data.anac_api.entity.Incident;
import com.data.anac_api.enums.PrioriteIncident;
import com.data.anac_api.enums.StatutIncident;
import com.data.anac_api.enums.TypeIncident;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    Optional<Incident> findByTrackingId(UUID trackingId);

    Page<Incident> findByDeclareParId(Long declarantId, Pageable pageable);

    Page<Incident> findByAssigneA_Id(Long assigneeId, Pageable pageable);

    List<Incident> findByTypeIncident(TypeIncident type);

    List<Incident> findByStatut(StatutIncident status);

    List<Incident> findByPriorite(PrioriteIncident priorite);

    Page<Incident> findByPrioriteOrderByCreatedAtDesc(PrioriteIncident priorite, Pageable pageable);

    long countByPriorite(PrioriteIncident priorite);

    @Query("SELECT i.priorite, COUNT(i) FROM Incident i GROUP BY i.priorite")
    List<Object[]> countByPrioriteGroup();

    long countByStatut(StatutIncident statut);

    @Query("SELECT i.typeIncident, COUNT(i) FROM Incident i GROUP BY i.typeIncident")
    List<Object[]> countByTypeIncident();

    @Query("SELECT i FROM Incident i WHERE " +
            "(:type IS NULL OR i.typeIncident = :type) AND " +
            "(:status IS NULL OR i.statut = :status) AND " +
            "(:startDate IS NULL OR i.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR i.createdAt <= :endDate)")
    Page<Incident> searchIncidents(@Param("type") TypeIncident type,
                                   @Param("status") StatutIncident status,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate,
                                   Pageable pageable);

    @Query("SELECT i FROM Incident i WHERE " +
            "LOWER(i.titre) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Incident> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT i FROM Incident i WHERE " +
            "(:type IS NULL OR i.typeIncident = :type) AND " +
            "(:status IS NULL OR i.statut = :status) AND " +
            "(:priorite IS NULL OR i.priorite = :priorite) AND " +
            "(:startDate IS NULL OR i.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR i.createdAt <= :endDate)")
    Page<Incident> searchIncidentsWithPriorite(@Param("type") TypeIncident type,
                                               @Param("status") StatutIncident status,
                                               @Param("priorite") PrioriteIncident priorite,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate,
                                               Pageable pageable);
}