package edu.cit.paradela.checkmateacademy.controller;

import edu.cit.paradela.checkmateacademy.model.Lesson;
import edu.cit.paradela.checkmateacademy.service.BookingService;
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
    private BookingService bookingService;

    @PostMapping
    public ResponseEntity<?> bookLesson(@RequestBody Lesson lesson) {
        try {
            return ResponseEntity.ok(bookingService.createBooking(lesson));
        } catch (RuntimeException e) {
            if ("TIME_CONFLICT".equals(e.getMessage())) {
                // Sends your exact requested error message to the UI
                return ResponseEntity.badRequest().body(Map.of("error", "Booking not available. This coach already has a session at this time."));
            }
            return ResponseEntity.internalServerError().body(Map.of("error", "An unexpected error occurred."));
        }
    }

    @GetMapping("/coach/{coachId}")
    public ResponseEntity<List<Lesson>> getCoachLessons(@PathVariable UUID coachId) {
        return ResponseEntity.ok(bookingService.getLessonsForCoach(coachId));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Lesson>> getStudentLessons(@PathVariable UUID studentId) {
        return ResponseEntity.ok(bookingService.getLessonsForStudent(studentId));
    }

    @PutMapping("/{lessonId}/status")
    public ResponseEntity<Lesson> updateStatus(@PathVariable UUID lessonId, @RequestParam String status) {
        return ResponseEntity.ok(bookingService.updateLessonStatus(lessonId, status));
    }

    @GetMapping("/{lessonId}")
    public ResponseEntity<Lesson> getLesson(@PathVariable UUID lessonId) {
        return ResponseEntity.ok(bookingService.getLessonById(lessonId));
    }

    @PutMapping("/{lessonId}/notes")
    public ResponseEntity<Lesson> updateNotes(@PathVariable UUID lessonId, @RequestBody String notes) {
        return ResponseEntity.ok(bookingService.saveLessonNotes(lessonId, notes));
    }
}