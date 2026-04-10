package edu.cit.paradela.checkmateacademy.service;

import edu.cit.paradela.checkmateacademy.model.Lesson;
import edu.cit.paradela.checkmateacademy.repository.LessonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {
    @Autowired
    private LessonRepository lessonRepository;

    public Lesson createBooking(Lesson lesson) {
        lesson.setStatus("PENDING");
        return lessonRepository.save(lesson);
    }

    public List<Lesson> getLessonsForCoach(UUID coachId) {
        return lessonRepository.findByCoachIdOrderByStartTimeDesc(coachId);
    }

    // Handles the Accept/Reject logic
    public Lesson updateLessonStatus(UUID lessonId, String status) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        lesson.setStatus(status);
        return lessonRepository.save(lesson);
    }
}