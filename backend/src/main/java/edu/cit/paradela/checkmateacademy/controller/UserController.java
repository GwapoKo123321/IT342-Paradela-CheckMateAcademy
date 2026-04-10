package edu.cit.paradela.checkmateacademy.controller;

import edu.cit.paradela.checkmateacademy.model.User;
import edu.cit.paradela.checkmateacademy.repository.UserRepository;
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
        // Automatically fetches all registered users where role = 'Coach'
        return ResponseEntity.ok(userRepository.findByRole("Coach"));
    }
}