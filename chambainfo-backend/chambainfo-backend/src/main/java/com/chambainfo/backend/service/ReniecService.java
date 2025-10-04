package com.chambainfo.backend.service;

import com.chambainfo.backend.dto.ReniecResponseDTO;

public interface ReniecService {
    ReniecResponseDTO consultarDni(String dni);
}