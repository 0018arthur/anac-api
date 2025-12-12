package com.data.anac_api.service;

import com.data.anac_api.dto.request.LoginRequest;
import com.data.anac_api.utils.DataResponse;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;

public interface JwtService {

    Map<String, String> generateTokens(LoginRequest user);

    DataResponse<Map<String, String>> refreshToken(String refreshToken);

    String extractUserEmail(String jwtToken);

    String extractTokenType(String token);

    boolean isTokenValid(String token, UserDetails userDetails);

    boolean isTokenExpired(String token);

    long tokenExpireIn();

    void updateUserTokens(String email, String accessToken, String refreshToken);

}
