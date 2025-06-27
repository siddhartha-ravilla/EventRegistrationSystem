package com.eventregistration.controller;

import com.eventregistration.dto.UserRegistrationRequest;
import com.eventregistration.model.User;
import com.eventregistration.service.JwtService;
import com.eventregistration.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtService jwtService;
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        try {
            User user = userService.registerUser(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody Map<String, String> loginRequest) {
        try {
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");
            
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtService.generateToken((User) authentication.getPrincipal());
            
            User user = (User) authentication.getPrincipal();
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("type", "Bearer");
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("firstName", user.getFirstName());
            response.put("lastName", user.getLastName());
            response.put("role", user.getRole());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid username or password");
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                String jwt = token.substring(7);
                if (jwtService.validateJwtToken(jwt)) {
                    String username = jwtService.getUserNameFromJwtToken(jwt);
                    User user = userService.findByUsername(username).orElse(null);
                    
                    if (user != null) {
                        Map<String, Object> response = new HashMap<>();
                        response.put("valid", true);
                        response.put("userId", user.getId());
                        response.put("username", user.getUsername());
                        response.put("role", user.getRole());
                        return ResponseEntity.ok(response);
                    }
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("error", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
    
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                String jwt = token.substring(7);
                if (jwtService.validateJwtToken(jwt)) {
                    String username = jwtService.getUserNameFromJwtToken(jwt);
                    User user = userService.findByUsername(username).orElse(null);
                    
                    if (user != null) {
                        Map<String, Object> profile = new HashMap<>();
                        profile.put("id", user.getId());
                        profile.put("username", user.getUsername());
                        profile.put("email", user.getEmail());
                        profile.put("firstName", user.getFirstName());
                        profile.put("lastName", user.getLastName());
                        profile.put("role", user.getRole());
                        profile.put("enabled", user.isEnabled());
                        profile.put("createdAt", user.getCreatedAt());
                        
                        return ResponseEntity.ok(profile);
                    }
                }
            }
            
            return ResponseEntity.status(401).build();
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
} 