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
            log.debug("URL: {}", url);
            
            ResponseEntity<ReniecResponseDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                ReniecResponseDTO.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                ReniecResponseDTO data = response.getBody();
                
                log.info("Respuesta exitosa de RENIEC:");
                log.info("   - first_name: {}", data.getFirstName());
                log.info("   - first_last_name: {}", data.getFirstLastName());
                log.info("   - second_last_name: {}", data.getSecondLastName());
                log.info("   - full_name: {}", data.getFullName());
                log.info("   - document_number: {}", data.getDocumentNumber());
                
                return data;
            } else {
                log.error("Respuesta inválida de RENIEC: status={}", response.getStatusCode());
                throw new ReniecException("No se pudo obtener información del DNI");
            }
            
        } catch (ReniecException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al consultar RENIEC: {}", e.getMessage(), e);
            throw new ReniecException("Error al consultar DNI en RENIEC. Verifique su conexión e intente nuevamente.");
        }
    }
}