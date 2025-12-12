package com.data.anac_api.service.impl;

import com.data.anac_api.dto.request.LoginRequest;
import com.data.anac_api.entity.Utilisateur;
import com.data.anac_api.mappers.UtilisateurMapper;
import com.data.anac_api.repository.UtilisateurRepository;
import com.data.anac_api.service.JwtService;
import com.data.anac_api.utils.DataResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Il s'agit d'une implémentation de l'interface JwtService
 * qui permet de générer un token JWT, d'extraire l'email
 * de l'utilisateur à partir du token et de vérifier la validité
 * du token.
 */
@Slf4j
@Service
public class JwtServiceImpl implements JwtService {

    private final UtilisateurMapper utilisateurMapper;
    private final UtilisateurRepository utilisateurRepository;

    /**
     * La clé secrète utilisée pour signer le token JWT
     */

    @Value("${jwt.jwtSecret}")
    private String key;

    @Value("${jwt.jwtExpiration}")
    private long duration;

    public JwtServiceImpl(UtilisateurMapper utilisateurMapper, UtilisateurRepository utilisateurRepository) {
        this.utilisateurMapper = utilisateurMapper;
        this.utilisateurRepository = utilisateurRepository;
    }

    /**
     * Génère un token JWT à partir des informations de l'utilisateur
     */
    @Override
    public Map<String, String> generateTokens(LoginRequest user) {
        Utilisateur utilisateur = utilisateurMapper.toEntity(user);

        List<String> roles = utilisateur.getUtilisateurRoles().stream()
                .map(utilisateurRole -> utilisateurRole.getRole().getNom())
                .collect(Collectors.toList());

        //Génération du token d'accès
        Map<String, Object> claims = new HashMap<>();
        claims.put("trackingId", utilisateur.getTrackingId());
        claims.put("roles", roles);
        claims.put("username", utilisateur.getNom());
        claims.put("prenoms", utilisateur.getPrenoms());
        claims.put("tokenType", "ACCESS");

        String accessToken = Jwts.builder()
                .claims(claims)
                .subject(user.email())
                .issuer("anac_code")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + duration))
                .signWith(generateKey())
                .compact();

        // Sauvegarde des tokens non chiffrés
        utilisateurRepository.updateTokens(
                utilisateur.getId(),
                accessToken
        );

        return Map.of(
                "access_token", accessToken,
                "expiresIn", String.valueOf(duration / 1000)
        );
    }



    /**
     * Rafraîchit le token JWT
     */
    @Override
    public DataResponse<Map<String, String>> refreshToken(String refreshToken) {

        // Vérifier expiration
        if (isTokenExpired(refreshToken)) {
            throw new RuntimeException("Refresh token expiré");
        }

        // Email et utilisateur
        String email = extractUserEmail(refreshToken);
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        String storedToken = utilisateur.getAccessToken();

        if (!refreshToken.equals(storedToken)) {
            throw new RuntimeException("Refresh token invalide");
        }

        // Ne pas révoquer, on le conserve car toujours valide
        // Générer uniquement un nouveau **access token**
        Map<String, Object> claims = new HashMap<>();
        claims.put("trackingId", utilisateur.getTrackingId());
        claims.put("roles", utilisateur.getUtilisateurRoles().stream()
                .map(r -> r.getRole().getNom())
                .collect(Collectors.toList()));
        claims.put("username", utilisateur.getNom());
        claims.put("prenoms", utilisateur.getPrenoms());
        claims.put("tokenType", "ACCESS");

        String accessToken = Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuer("anac_code")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + duration))
                .signWith(generateKey())
                .compact();

        utilisateurRepository.updateTokens(
                utilisateur.getId(),
                accessToken
        );

        return new DataResponse<>(
                new Date(),
                false,
                "Access token renouvelé",
                Map.of(
                        "access_token", accessToken,
                        "expiresIn", String.valueOf(duration / 1000)
                )
        );
    }


    /**
     * Extrait l'email de l'utilisateur à partir du token JWT
     */
    @Override
    public String extractUserEmail(String jwtToken) {
        return extractClaims(jwtToken, Claims::getSubject);
    }

    @Override
    public String extractTokenType(String token) {
        return extractClaims(token).get("tokenType", String.class);
    }

    /**
     * Extrait les claims du token JWT
     */
    private <T> T extractClaims(String jwtToken, Function<Claims, T> claimResolver) {

        Claims claims = extractClaims(jwtToken);

        return claimResolver.apply(claims);
    }

    /**
     * Extrait les claims du token JWT
     */
    private Claims extractClaims(String jwtToken) {
        String tokenToProcess;
       Jwts.parser().verifyWith(generateKey()).build().parseSignedClaims(jwtToken);
            tokenToProcess = jwtToken;

        return Jwts.parser()
                .verifyWith(generateKey())
                .build()
                .parseSignedClaims(tokenToProcess)
                .getPayload();
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String userEmail = extractUserEmail(token);

        return userEmail.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    /**
     * Vérifie si le token JWT est expiré
     */
    @Override
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extrait la date d'expiration du token JWT
     */
    private Date extractExpiration(String token) {
        return extractClaims(token, Claims::getExpiration);
    }

    /**
     * Génère une clé secrète à partir de la clé secrète
     */
    private SecretKey generateKey() {
        byte[] decode = Decoders.BASE64.decode(this.key);
        return Keys.hmacShaKeyFor(decode);
    }


    @Override
    public long tokenExpireIn() {
        return this.duration;
    }


    @Override
    @Transactional
    public void updateUserTokens(String email, String accessToken, String refreshedToken) {
        utilisateurRepository.updateTokensByEmail(
                email,
                accessToken,
                refreshedToken,
                true
        );
    }

}
