package com.data.anac_api.service;

import com.data.anac_api.dto.request.ChangePasswordRequest;
import com.data.anac_api.dto.request.LoginRequest;
import com.data.anac_api.dto.request.RegisterRequest;
import com.data.anac_api.dto.request.VerificationRequest;
import com.data.anac_api.dto.response.LoginResponse;
import com.data.anac_api.dto.response.RegisterResponse;
import com.data.anac_api.utils.DataResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    DataResponse<RegisterResponse> register(RegisterRequest utilisateur);

    DataResponse<LoginResponse> login(LoginRequest utilisateur);

    DataResponse<Void> logoutUser(HttpServletRequest request);

    DataResponse<String> changePassword(String name, ChangePasswordRequest request);
}
