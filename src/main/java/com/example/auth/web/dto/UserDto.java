package com.example.auth.web.dto;

import lombok.Data;
import lombok.NonNull;

@Data
public class UserDto {

    @NonNull
    private String username;

    @NonNull
    private String password;

    private String name;

    private String address;

}
