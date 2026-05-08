package edu.cit.paradela.checkmateacademy.features.lesson;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/lessons")
@CrossOrigin(origins = "*")
public class LessonController {

    @Autowired
    private LessonService lessonService; // Corrected from bookingService for consistency

    @PostMapping
    public ResponseEntity<?> bookLesson(@RequestBody Lesson lesson) {
        try {
            return ResponseEntity.ok(lessonService.createBooking(lesson));
        } catch (RuntimeException e) {
            if ("TIME_CONFLICT".equals(e.getMessage())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Booking not available. This coach already has a session at this time."));
            }
            return ResponseEntity.internalServerError().body(Map.of("error", "An unexpected error occurred."));
        }
    }

    @GetMapping("/coach/{coachId}")
    public ResponseEntity<List<Lesson>> getCoachLessons(@PathVariable UUID coachId) {
        return ResponseEntity.ok(lessonService.getLessonsForCoach(coachId));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Lesson>> getStudentLessons(@PathVariable UUID studentId) {
        return ResponseEntity.ok(lessonService.getLessonsForStudent(studentId));
    }

    @PutMapping("/{lessonId}/status")
    public ResponseEntity<Lesson> updateStatus(@PathVariable UUID lessonId, @RequestParam String status) {
        return ResponseEntity.ok(lessonService.updateLessonStatus(lessonId, status));
    }

    @GetMapping("/{lessonId}")
    public ResponseEntity<Lesson> getLesson(@PathVariable UUID lessonId) {
        return ResponseEntity.ok(lessonService.getLessonById(lessonId));
    }

    @PutMapping("/{lessonId}/notes")
    public ResponseEntity<Lesson> updateNotes(@PathVariable UUID lessonId, @RequestBody String notes) {
        return ResponseEntity.ok(lessonService.saveLessonNotes(lessonId, notes));
    }

    // NEW: Endpoint to receive board state updates
    @PutMapping("/{lessonId}/board")
    public ResponseEntity<Lesson> updateBoardState(@PathVariable UUID lessonId, @RequestBody String boardState) {
        return ResponseEntity.ok(lessonService.updateBoardState(lessonId, boardState));
    }
}