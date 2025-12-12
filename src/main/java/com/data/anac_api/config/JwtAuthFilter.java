package com.data.anac_api.config;

import com.data.anac_api.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.coyote.BadRequestException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtre qui intercepte les requêtes entrantes et vérifie
 * si elles contiennent un token JWT valide. Si le token est
 * valide, le filtre extrait l'email de l'utilisateur à partir
 * du token et authentifie l'utilisateur.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Méthode qui intercepte les requêtes entrantes et vérifie
     * si elles contiennent un token JWT valide. Si le token est
     * valide, le filtre extrait l'email de l'utilisateur à partir
     * du token et authentifie l'utilisateur.
     * @author vinci_code_chronos
     * @param request     La requête entrante
     * @param response    La réponse à renvoyer
     * @param filterChain Le prochain filtre à appeler
     * @throws ServletException Si une erreur se produit lors du traitement de la requête
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwtToken = authHeader.substring(7);

        // Vérifier si le token est révoqué
        if (jwtService.isTokenExpired(jwtToken)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            throw new BadRequestException("Token révoqué !");
        }

        final String userEmail = jwtService.extractUserEmail(jwtToken);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (userEmail != null && authentication == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            if (jwtService.isTokenValid(jwtToken, userDetails)) {
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        filterChain.doFilter(request, response);
    }

}
