package com.tickr.tickr.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CreateUserRequest {
    @JsonProperty("phoneNumber")
    private String phoneNumber;
    
    @JsonProperty("timezone")
    private String timezone;

    @JsonProperty("password")
    private String password;
}
