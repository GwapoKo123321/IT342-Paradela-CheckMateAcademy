package edu.cit.paradela.checkmateacademy.features.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/coaches")
    public ResponseEntity<List<User>> getCoaches() {
        return ResponseEntity.ok(userRepository.findByRole("Coach"));
    }
}