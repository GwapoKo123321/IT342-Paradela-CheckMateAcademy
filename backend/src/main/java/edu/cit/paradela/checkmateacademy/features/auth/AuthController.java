package edu.cit.paradela.checkmateacademy.features.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        Map<String, Object> response = new HashMap<>();


        if (authService.emailExists(user.getEmail())) {
            response.put("success", false);
            response.put("message", "Registration Failed. Email may already be in use.");
            return ResponseEntity.badRequest().body(response);
        }


        User savedUser = authService.registerUser(user);
        response.put("success", true);
        response.put("message", "Account Created!");
        response.put("token", "session-" + savedUser.getId());
        response.put("role", savedUser.getRole());
        response.put("userId", String.valueOf(savedUser.getId()));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        Map<String, Object> response = new HashMap<>();

        return authService.authenticate(credentials.get("email"), credentials.get("password"))
                .map(user -> {

                    response.put("success", true);
                    response.put("message", "Login successful!");
                    response.put("token", "session-" + user.getId());
                    response.put("role", user.getRole());
                    response.put("userId", String.valueOf(user.getId()));
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {

                    response.put("success", false);
                    response.put("message", "Invalid Credentials");
                    return ResponseEntity.status(401).body(response);
                });
    }
}