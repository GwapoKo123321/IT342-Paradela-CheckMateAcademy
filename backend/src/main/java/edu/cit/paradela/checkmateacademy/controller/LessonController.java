package edu.cit.paradela.checkmateacademy.controller;

import edu.cit.paradela.checkmateacademy.model.Lesson;
import edu.cit.paradela.checkmateacademy.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/lessons")
@CrossOrigin(origins = "*")
public class LessonController {
    @Autowired
    private BookingService bookingService;

    @PostMapping
    public ResponseEntity<Lesson> bookLesson(@RequestBody Lesson lesson) {
        return ResponseEntity.ok(bookingService.createBooking(lesson));
    }

    @GetMapping("/coach/{coachId}")
    public ResponseEntity<List<Lesson>> getCoachLessons(@PathVariable UUID coachId) {
        return ResponseEntity.ok(bookingService.getLessonsForCoach(coachId));
    }

    @PutMapping("/{lessonId}/status")
    public ResponseEntity<Lesson> updateStatus(@PathVariable UUID lessonId, @RequestParam String status) {
        return ResponseEntity.ok(bookingService.updateLessonStatus(lessonId, status));
    }
}