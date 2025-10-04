package com.chambainfo.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReniecResponseDTO {
    
    @JsonProperty("first_name")
    private String firstName;
    
    @JsonProperty("first_last_name")
    private String firstLastName;
    
    @JsonProperty("second_last_name")
    private String secondLastName;
    
    @JsonProperty("full_name")
    private String fullName;
    
    @JsonProperty("document_number")
    private String documentNumber;
}