package edu.cit.paradela.checkmateacademy.features.auth;

import edu.cit.paradela.checkmateacademy.features.coach.CoachAvailableSlotResponse;
import edu.cit.paradela.checkmateacademy.features.coach.CoachProfileRequest;
import edu.cit.paradela.checkmateacademy.features.coach.CoachProfileResponse;
import edu.cit.paradela.checkmateacademy.features.coach.CoachProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CoachProfileService coachProfileService;

    // --- EXISTING COACH LOGIC ---

    @GetMapping("/coaches")
    public ResponseEntity<List<CoachProfileResponse>> getCoaches(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String style
    ) {
        LocalDateTime parsedStartTime = parseDateTime(startTime);
        LocalDateTime parsedEndTime = parseDateTime(endTime);
        List<User> coaches = userRepository.findByRole("Coach");

        if (parsedStartTime != null || parsedEndTime != null || (style != null && !style.isBlank())) {
            return ResponseEntity.ok(coachProfileService.filterCoaches(coaches, parsedStartTime, parsedEndTime, style));
        }

        return ResponseEntity.ok(coachProfileService.buildResponses(coaches));
    }

    @GetMapping("/coaches/available-slots")
    public ResponseEntity<?> getAvailableCoachSlots(
            @RequestParam String date,
            @RequestParam(required = false) String style,
            @RequestParam(required = false) UUID studentId
    ) {
        try {
            LocalDate parsedDate = LocalDate.parse(date);
            List<User> coaches = userRepository.findByRole("Coach");
            List<CoachAvailableSlotResponse> slots = coachProfileService.findAvailableLessonSlots(coaches, parsedDate, style, studentId);
            return ResponseEntity.ok(slots);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Please choose a valid booking date."));
        }
    }

    @GetMapping("/coaches/{coachId}/profile")
    public ResponseEntity<CoachProfileResponse> getCoachProfile(@PathVariable UUID coachId) {
        User coach = userRepository.findById(coachId).orElseThrow(() -> new RuntimeException("Coach not found"));
        return ResponseEntity.ok(coachProfileService.buildResponse(coach));
    }

    @PutMapping("/coaches/{coachId}/profile")
    public ResponseEntity<?> updateCoachProfile(
            @PathVariable UUID coachId,
            @RequestBody CoachProfileRequest request
    ) {
        try {
            User coach = userRepository.findById(coachId).orElseThrow(() -> new RuntimeException("Coach not found"));
            return ResponseEntity.ok(coachProfileService.saveProfile(coach, request));
        } catch (RuntimeException e) {
            if ("OVERLAPPING_AVAILABILITY_SLOT".equals(e.getMessage())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Availability slots on the same day cannot overlap."));
            }
            throw e;
        }
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) return null;
        return LocalDateTime.parse(value);
    }

    @PutMapping("/profile/update/{userId}")
    public ResponseEntity<?> updateProfile(@PathVariable UUID userId, @RequestBody User updateData) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // If the user attempts to change their ELO, automatically drop their verification status
        if (updateData.getCurrentElo() != null && !updateData.getCurrentElo().equals(user.getCurrentElo())) {
            user.setEloVerified(false);
        }

        user.setFullName(updateData.getFullName());
        user.setChessUsername(updateData.getChessUsername());
        user.setCurrentElo(updateData.getCurrentElo());

        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }
}