package com.tickr.tickr.dto;

import lombok.Data;

@Data
public class CreateUserRequest {
    private String phoneNumber;
    private String timezone;
}
