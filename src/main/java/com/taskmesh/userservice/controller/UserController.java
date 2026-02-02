package com.taskmesh.userservice.controller;

import com.taskmesh.userservice.dto.Auth;
import com.taskmesh.userservice.dto.Login;
import com.taskmesh.userservice.dto.Register;
import com.taskmesh.userservice.entity.User;
import com.taskmesh.userservice.service.UserService;
import com.taskmesh.userservice.utility.JWTUtility;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final JWTUtility jwtUtil;

    public UserController(UserService userService, JWTUtility jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid Register request) {
        userService.register(request);
        return ResponseEntity.ok("User registered");
    }

    @PostMapping("/login")
    public ResponseEntity<Auth> login(@RequestBody Login request) {
        return ResponseEntity.ok(userService.login(request, jwtUtil));
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getProfile(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getProfile(id));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN','MANAGER','USER')")
    public ResponseEntity<?> deactivate(@PathVariable Long id) {
        userService.deactivate(id);
        return ResponseEntity.ok("User deactivated");
    }
    @GetMapping("/{userId}/validate")
    @PreAuthorize("hasAnyRole('MANAGER','USER')")
    public ResponseEntity<Void> validateUser(@PathVariable Long userId) {
        return ResponseEntity.ok().build();
    }
}
