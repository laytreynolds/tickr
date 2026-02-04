package com.tickr.tickr.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CreateUserRequest {
    @JsonProperty("phoneNumber")
    private String phoneNumber;
    
    private String timezone;

    private String password;
}
