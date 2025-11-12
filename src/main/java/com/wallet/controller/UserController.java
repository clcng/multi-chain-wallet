package com.wallet.controller;

import com.wallet.dto.LoginDto;
import com.wallet.dto.UserRegistrationDto;
import com.wallet.model.User;
import com.wallet.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        try {
            User user = userService.registerUser(registrationDto);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDto loginDto) {
        try {
            if(!loginDto.getUsername().isEmpty()) {
                loginDto.setUsernameOrEmail(loginDto.getUsername());
            } else {
                loginDto.setUsernameOrEmail(loginDto.getEmail());
            }

            String token = userService.authenticateUser(loginDto);

            Map<String, String> response = new HashMap<>();
            response.put("token", "Bearer " + token);
//            response.put("token", token);
//            response.put("type", "Bearer");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
        }
    }

    @PostMapping("logout")
    public ResponseEntity<?> logout() {
        userService.logoutUser();
        Map<String, String> response = new HashMap<>();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUser() {
        try {
            User user = userService.getCurrentUser();

            Map<String, Object> response = new HashMap<>();
//            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("firstName", user.getFirstName());
            response.put("lastName", user.getLastName());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized"));
        }
    }

}
