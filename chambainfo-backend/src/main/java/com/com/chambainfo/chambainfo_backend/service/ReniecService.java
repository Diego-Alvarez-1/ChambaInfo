package com.chambainfo.service;

import com.chambainfo.dto.ReniecResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ReniecService {
    
    @Value("${reniec.api.url:https://api.apis.net.pe/v2/reniec/dni}")
    private String reniecApiUrl;
    
    @Value("${reniec.api.token:}")
    private String reniecApiToken;
    
    private final RestTemplate restTemplate;
    
    public ReniecService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    public ReniecResponse consultarDni(String dni) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + reniecApiToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            String url = reniecApiUrl + "?numero=" + dni;
            ResponseEntity<ReniecResponse> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                entity, 
                ReniecResponse.class
            );
            
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Error al consultar RENIEC: " + e.getMessage());
        }
    }
}