package edu.cit.paradela.checkmateacademy.controller;

import edu.cit.paradela.checkmateacademy.model.User;
import edu.cit.paradela.checkmateacademy.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService; // Using the Facade Pattern

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (authService.emailExists(user.getEmail())) {
            return ResponseEntity.badRequest().body("Email already in use!");
        }
        return ResponseEntity.ok(authService.registerUser(user));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        return authService.authenticate(credentials.get("email"), credentials.get("password"))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(401).body(null)); // Matches SDD 401 Unauthorized
    }
}