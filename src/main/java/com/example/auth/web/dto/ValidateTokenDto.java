package com.example.auth.web.dto;

import java.util.List;
import lombok.Data;

@Data
public class ValidateTokenDto {

    private boolean isValid;
    private String username;
    private List<String> roles;
}
