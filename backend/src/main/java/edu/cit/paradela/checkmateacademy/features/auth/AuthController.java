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
        if (authService.emailExists(user.getEmail())) {
            return ResponseEntity.badRequest().body("Email already in use!");
        }
        return ResponseEntity.ok(authService.registerUser(user));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        return authService.authenticate(credentials.get("email"), credentials.get("password"))
                .map(user -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);

                    Map<String, Object> data = new HashMap<>();
                    data.put("user", user);
                    data.put("accessToken", "session-" + user.getId());

                    response.put("data", data);
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.status(401).build());
    }
}