package com.data.anac_api.entity;

import com.data.anac_api.enums.PrioriteIncident;
import com.data.anac_api.enums.StatutIncident;
import com.data.anac_api.enums.TypeIncident;
import com.data.anac_api.utils.Auditable;
import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "incidents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incident extends Auditable implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(updatable = false, nullable = false, unique = true)
    private UUID trackingId;

    @Column(nullable = false)
    private String titre;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String analyseIA;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeIncident typeIncident;

    @Enumerated(EnumType.STRING)
    private StatutIncident statut;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrioriteIncident priorite;

    private String localisation;

    private Double latitude;

    private Double longitude;

    @Column(name = "photo_url", length = 1000)
    private String photoUrl;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur declarePar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigne_a")
    private Utilisateur assigneA;
}