package com.example.auth.service;

import static com.example.auth.persistence.model.RoleName.ROLE_ADMIN;
import static com.example.auth.persistence.model.RoleName.ROLE_USER;

import com.example.auth.exception.AccessDeniedException;
import com.example.auth.exception.InternalException;
import com.example.auth.exception.UserAlreadyExistException;
import com.example.auth.exception.UserNotFoundException;
import com.example.auth.persistence.dao.RoleRepository;
import com.example.auth.persistence.dao.UserRepository;
import com.example.auth.persistence.model.AuthenticatedUser;
import com.example.auth.persistence.model.Role;
import com.example.auth.persistence.model.RoleName;
import com.example.auth.persistence.model.User;
import com.example.auth.web.dto.UserDto;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService implements UserDetailsService, ApplicationListener<ContextRefreshedEvent> {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private boolean alreadySetup = false;

    public UserService(UserRepository userRepository, RoleRepository roleRepository,
        PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(final UserDto userDto) {
        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new UserAlreadyExistException(
                "There is an existed user with username: " + userDto.getUsername());
        }
        final User user = new User();
        user.setUsername(userDto.getUsername());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setName(userDto.getName());
        user.setAddress(userDto.getAddress());
        Role userRole = roleRepository.findByName("ROLE_USER")
            .orElseThrow(() -> new InternalException("User Role not set."));
        user.setRoles(Arrays.asList(userRole));
        return userRepository.insert(user);
    }

    public User getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(
                () -> new UserNotFoundException("There is no user with username: " + username));
        return user;
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return AuthenticatedUser.builder().user(getUserByUsername(username)).build();
    }

    @Override
    @Transactional
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        if (alreadySetup) {
            return;
        }

        // == create initial roles
        final Role adminRole = createRoleIfNotFound(ROLE_ADMIN.name());
        createRoleIfNotFound(ROLE_USER.name());

        // == create initial user
        createUserIfNotFound("test", "test", "Test", "test",
            new ArrayList<>(Arrays.asList(adminRole)));

        alreadySetup = true;
    }

    @Transactional
    Role createRoleIfNotFound(final String name) {
        if (roleRepository.existsByName(name)) {
            return roleRepository.findByName(name).get();
        } else {
            return roleRepository.save(Role.builder().name(RoleName.valueOf(name)).build());
        }
    }

    @Transactional
    User createUserIfNotFound(final String username, final String password, final String name,
        final String address, final List<Role> roles) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        User user;
        if (!userOpt.isPresent()) {
            user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setName(name);
            user.setAddress(address);
        } else {
            user = userOpt.get();
        }
        user.setRoles(roles);
        user = userRepository.save(user);
        return user;
    }
}
