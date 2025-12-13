package com.data.anac_api.controller;

import com.data.anac_api.dto.request.*;
import com.data.anac_api.dto.response.LoginResponse;
import com.data.anac_api.dto.response.RegisterResponse;
import com.data.anac_api.service.AuthService;
import com.data.anac_api.service.JwtService;
import com.data.anac_api.utils.DataResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }


    @PostMapping("/register")
    public ResponseEntity<DataResponse<RegisterResponse>> register(@RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(authService.register(registerRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<DataResponse<LoginResponse>> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/logout")
    public ResponseEntity<DataResponse<Void>> logout(HttpServletRequest request) {
        return ResponseEntity.ok(authService.logoutUser(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<DataResponse<Map<String, String>>> refreshToken(@CookieValue("refresh_token") String encryptedToken) {
        return ResponseEntity.ok(jwtService.refreshToken(encryptedToken));
    }

    @PostMapping("/change-password")
    public ResponseEntity<DataResponse<String>> changePassword(
            @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        DataResponse<String> response = authService.changePassword(
                authentication.getName(),
                request
        );
        return ResponseEntity.ok(response);
    }
}