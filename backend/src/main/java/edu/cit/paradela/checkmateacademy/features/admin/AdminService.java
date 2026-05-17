package edu.cit.paradela.checkmateacademy.features.admin;

import edu.cit.paradela.checkmateacademy.features.auth.User;
import edu.cit.paradela.checkmateacademy.features.auth.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUserRole(UUID userId, String newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole(newRole);
        return userRepository.save(user);
    }

    public void deleteUser(UUID userId) {
        userRepository.deleteById(userId);
    }

    // NEW LOGIC: Toggle Account Flag (Warning for bad comments/behavior)
    public User toggleUserFlag(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setIsFlagged(user.getIsFlagged() == null ? true : !user.getIsFlagged());
        return userRepository.save(user);
    }

    // NEW LOGIC: Toggle ELO Verification
    public User toggleEloVerification(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setEloVerified(user.getEloVerified() == null ? true : !user.getEloVerified());
        return userRepository.save(user);
    }
}