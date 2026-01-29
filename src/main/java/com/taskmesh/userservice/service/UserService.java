package com.taskmesh.userservice.service;

import com.taskmesh.userservice.dto.Auth;
import com.taskmesh.userservice.dto.Login;
import com.taskmesh.userservice.dto.Register;
import com.taskmesh.userservice.entity.Role;
import com.taskmesh.userservice.entity.Status;
import com.taskmesh.userservice.entity.User;
import com.taskmesh.userservice.repo.UserRepo;
import com.taskmesh.userservice.utility.JWTUtility;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final UserEventProducer kafkaProducer;


    public UserService(UserRepo userRepo,
                       PasswordEncoder passwordEncoder,
                       UserEventProducer kafkaProducer) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.kafkaProducer = kafkaProducer;
    }


    public void register(Register register) {


        if (userRepo.existsByUsername(register.getUsername()))
            throw new RuntimeException("Username already exists");


        if (userRepo.existsByEmail(register.getEmail()))
            throw new RuntimeException("Email already exists");


        User user = new User();
        user.setUsername(register.getUsername());
        user.setEmail(register.getEmail());
        user.setPassword(passwordEncoder.encode(register.getPassword()));
        user.setRole(register.getRole() == null ? Role.USER : register.getRole());
        user.setStatus(Status.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());


        userRepo.save(user);
        kafkaProducer.publishUserCreated(user.getId());
    }


    public Auth login(Login request, JWTUtility jwtUtil) {


        User user = userRepo.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));


        if (user.getStatus() == Status.INACTIVE)
            throw new RuntimeException("User inactive");


        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
            throw new RuntimeException("Invalid credentials");


        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        return new Auth(token, 3600);
    }


    public User getProfile(Long id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }


    public void deactivate(Long id) {
        User user = userRepo.findById(id).orElseThrow();
        user.setStatus(Status.INACTIVE);
        userRepo.save(user);
        kafkaProducer.publishUserDeactivated(id);
    }
}
