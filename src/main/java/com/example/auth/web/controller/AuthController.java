package com.example.auth.web.controller;

import com.example.auth.exception.AccessDeniedException;
import com.example.auth.persistence.model.RoleName;
import com.example.auth.persistence.model.User;
import com.example.auth.service.JwtTokenProvider;
import com.example.auth.service.UserService;
import com.example.auth.web.dto.JwtDto;
import com.example.auth.web.dto.UserDto;
import com.example.auth.web.dto.ValidateTokenDto;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtTokenProvider tokenProvider;

    @Autowired
    UserService userService;

    @PostMapping("api/v1/user")
    public User createUser(
        @RequestBody UserDto userDto) {
        return userService.createUser(userDto);
    }

    @GetMapping("api/v1/user/{username}")
    public User getUser(@PathVariable String username) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
            .getAuthentication().getPrincipal();
        if (!username.equals(userDetails.getUsername()) &&
            userDetails.getAuthorities().stream().noneMatch(grantedAuthority ->
                grantedAuthority.getAuthority().equals(RoleName.ROLE_ADMIN.name()))) {
            throw new AccessDeniedException("Access Denied");
        }
        return userService.getUserByUsername(username);
    }

    @GetMapping("api/v1/user")
    public List<User> getUsers() {
        return userService.getUsers();
    }

    @PostMapping("api/v1/token")
    public JwtDto getToken(
        @RequestBody UserDto userDto) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                userDto.getUsername(),
                userDto.getPassword()
            )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);
        JwtDto jwtDto = new JwtDto();
        jwtDto.setAccessToken(jwt);
        return jwtDto;
    }

    @PostMapping("api/v1/token/validate")
    public ValidateTokenDto validateToken(@RequestBody JwtDto jwtDto) {
        String token = jwtDto.getAccessToken();
        ValidateTokenDto validateTokenDto = new ValidateTokenDto();
        boolean isValid = tokenProvider.validateToken(token);
        validateTokenDto.setValid(isValid);
        if (isValid) {
            validateTokenDto.setUsername(tokenProvider.getUsernameFromJWT(token));
            validateTokenDto.setRoles(tokenProvider.getRolesFromJWT(token));
        }
        return validateTokenDto;
    }
}
