package com.example.auth.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public class User {

    @Id
    private String _id;
    @Indexed(unique = true, direction = IndexDirection.ASCENDING)
    private String username;
    @JsonIgnore
    private String password;
    private String name;
    private String address;
    private List<Role> roles;
}
