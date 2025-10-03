
package com.chambainfo.backend.service;

import com.chambainfo.backend.dto.ReniecResponseDTO;
import com.chambainfo.backend.exception.ReniecException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class ReniecServiceImpl implements ReniecService {
    
    @Value("${reniec.api.url}")
    private String reniecApiUrl;
    
    @Value("${reniec.api.token}")
    private String reniecApiToken;
    
    private final RestTemplate restTemplate;
    
    public ReniecServiceImpl() {
        this.restTemplate = new RestTemplate();
    }
    
    @Override
    public ReniecResponseDTO consultarDni(String dni) {
        try {
            String url = reniecApiUrl + "?numero=" + dni;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");
            headers.set("Authorization", "Bearer " + reniecApiToken);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            log.info("Consultando RENIEC para DNI: {}", dni);
            
            ResponseEntity<ReniecResponseDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                ReniecResponseDTO.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("Datos obtenidos de RENIEC: {}", response.getBody().getFullName());
                return response.getBody();
            } else {
                throw new ReniecException("No se pudo obtener información del DNI");
            }
            
        } catch (Exception e) {
            log.error("Error al consultar RENIEC: {}", e.getMessage());
            throw new ReniecException("Error al consultar DNI en RENIEC: " + e.getMessage());
        }
    }
}