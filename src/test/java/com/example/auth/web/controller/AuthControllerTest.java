package com.example.auth.web.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.auth.persistence.dao.RoleRepository;
import com.example.auth.persistence.dao.UserRepository;
import com.example.auth.persistence.model.Role;
import com.example.auth.persistence.model.RoleName;
import com.example.auth.persistence.model.User;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class AuthControllerTest {

    private final String username1 = "test";
    private final String password1 = "test";
    private final String name1 = "Test";
    private final String address1 = "test";
    private Role adminRole;
    private Role userRole;
    @LocalServerPort
    private int port;
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private RoleRepository roleRepository;

    @BeforeEach
    public void setUp() {
        adminRole = Role.builder().name(RoleName.ROLE_ADMIN).build();
        userRole = Role.builder().name(RoleName.ROLE_USER).build();
    }

    @Test
    public void testCreateUser(){
        when(userRepository.existsByUsername(username1)).thenReturn(false);
        when(roleRepository.findByName(RoleName.ROLE_USER.name())).thenReturn(Optional.of(userRole));
        String resourceUrl = "http://localhost:" + port + "/api/v1/user";
        Map<String, String> entity = new HashMap();
        entity.put("username", username1);
        entity.put("password", password1);
        entity.put("name", name1);
        entity.put("address", address1);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(entity, headers);
        restTemplate.postForEntity(resourceUrl, request, User.class);

        ArgumentCaptor<User> argumentCaptor =
            ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).insert(argumentCaptor.capture());
        User savedUser = argumentCaptor.getValue();
        assertEquals(username1, savedUser.getUsername());
        assertTrue(passwordEncoder.matches(password1, savedUser.getPassword()));
        assertEquals(name1, savedUser.getName());
        assertEquals(address1, savedUser.getAddress());
        assertEquals(RoleName.ROLE_USER, savedUser.getRoles().get(0).getName());
    }
}
