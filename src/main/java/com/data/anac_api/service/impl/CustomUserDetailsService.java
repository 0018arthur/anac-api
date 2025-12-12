package com.data.anac_api.service.impl;

import com.data.anac_api.entity.Utilisateur;
import com.data.anac_api.repository.UtilisateurRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    public CustomUserDetailsService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    /**
     * @author vinci_code_chronos
     * @param username
     * @return UserDetails
     * @apiNote Méthode qui permet de charger un utilisateur par son email
     * */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Utilisateur user = utilisateurRepository.findByEmailOrMatriculeWithRoles(username).orElseThrow(
                () -> new UsernameNotFoundException("Utilisateur non trouvé"));

        return new CustomUserDetails(user);
    }
}

