package com.example.auth.persistence.model;

import lombok.Builder;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Value
@Builder
public class Role {

    @Id
    private String _id;
    @Indexed(unique = true)
    private RoleName name;
}
