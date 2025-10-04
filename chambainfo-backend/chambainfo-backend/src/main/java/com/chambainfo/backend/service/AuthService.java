
package com.chambainfo.backend.service;

import com.chambainfo.backend.dto.AuthResponseDTO;
import com.chambainfo.backend.dto.LoginRequestDTO;
import com.chambainfo.backend.dto.RegisterRequestDTO;

public interface AuthService {
    AuthResponseDTO register(RegisterRequestDTO request);
    AuthResponseDTO login(LoginRequestDTO request);
}