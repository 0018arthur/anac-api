package com.data.anac_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

// Configuration CORS - Gérée dans SecurityConfig.java aussi
@Configuration
public class CorsConfig {
    // Le CorsFilter peut être utilisé si nécessaire en complément de SecurityConfig

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Autoriser les origines locales pour le développement
        config.addAllowedOrigin("http://localhost:3000"); // Dashboard ANAC
        config.addAllowedOrigin("http://localhost:3001"); // SafeTraveler
        config.addAllowedOrigin("http://localhost:4200"); // Angular/autre frontend
        config.addAllowedOrigin("http://localhost:5173"); // Vite dev server
        config.addAllowedOrigin("http://127.0.0.1:3000");
        config.addAllowedOrigin("http://127.0.0.1:3001");
        config.addAllowedOrigin("https://safetraveler.vercel.app");
        config.addAllowedOrigin("https://anac-dashboard.vercel.app");

        config.setAllowCredentials(true);
        config.addAllowedHeader("*");
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("PATCH");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");
        config.addAllowedMethod("HEAD");

        config.addExposedHeader("Authorization");
        config.addExposedHeader("Content-Type");
        config.addExposedHeader("X-Total-Count");
        config.addExposedHeader("X-Page-Number");
        config.addExposedHeader("X-Page-Size");

        config.setMaxAge(3600L); // Cache preflight requests for 1 hour

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
    
}
