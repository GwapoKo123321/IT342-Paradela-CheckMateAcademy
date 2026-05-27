package edu.cit.paradela.checkmateacademy.features.admin;

import edu.cit.paradela.checkmateacademy.features.auth.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<User> updateUserRole(@PathVariable UUID userId, @RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(adminService.updateUserRole(userId, payload.get("role")));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable UUID userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    @PutMapping("/users/{userId}/flag")
    public ResponseEntity<User> toggleUserFlag(@PathVariable UUID userId) {
        return ResponseEntity.ok(adminService.toggleUserFlag(userId));
    }

    @PutMapping("/users/{userId}/verify-elo")
    public ResponseEntity<User> toggleEloVerification(@PathVariable UUID userId) {
        return ResponseEntity.ok(adminService.toggleEloVerification(userId));
    }
}
