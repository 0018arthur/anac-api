package com.data.anac_api.enums;

/**
 * OACI/ICAO Compliant Aviation Incident Categories
 * Based on ICAO Annex 13 - Aircraft Accident and Incident Investigation
 * Agence Nationale de l'Aviation Civile (ANAC) - Togo
 * Airport: Gnassingbé Eyadéma International Airport (IATA: LFW)
 */
public enum TypeIncident {
    /**
     * RI - Runway Incursion
     * Unauthorized entry onto runway protected area
     * Severity: CRITICAL
     */
    RUNWAY_INCURSION,

    /**
     * FOD - Foreign Object Debris
     * Foreign objects on operational surfaces (runways, taxiways, aprons)
     * Severity: HIGH
     */
    FOD,

    /**
     * BS - Bird Strike / Wildlife Hazard
     * Wildlife hazard or bird strike incident
     * Severity: HIGH
     */
    BIRD_STRIKE,

    /**
     * SEC - Security Breach
     * Aviation security violation or unauthorized access
     * Severity: CRITICAL
     */
    SECURITY_BREACH,

    /**
     * FM - Facility Maintenance
     * Airport infrastructure maintenance issue
     * Severity: MEDIUM
     */
    FACILITY_MAINTENANCE,

    /**
     * GH - Ground Handling
     * Ground support equipment or handling issue
     * Severity: MEDIUM
     */
    GROUND_HANDLING,

    /**
     * PS - Passenger Safety
     * Passenger safety or medical emergency
     * Severity: HIGH
     */
    PASSENGER_SAFETY,

    /**
     * ENV - Environmental / Housekeeping
     * Environmental concern or housekeeping issue
     * Severity: LOW
     */
    ENVIRONMENTAL,

    /**
     * OTH - Other Operational Concern
     * Other aviation operational concern not classified above
     * Severity: LOW
     */
    OTHER
}
