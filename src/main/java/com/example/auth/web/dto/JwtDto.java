package com.example.auth.web.dto;

import lombok.Data;

@Data
public class JwtDto {

    private String accessToken;
    private String tokenType = "Bearer";
}
