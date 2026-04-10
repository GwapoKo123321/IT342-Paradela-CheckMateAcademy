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
        // Uses the new overlapping query to prevent bookings inside existing sessions
        boolean isBooked = lessonRepository.existsOverlappingLesson(
                lesson.getCoachId(), lesson.getStartTime(), lesson.getEndTime()
        );

        if (isBooked) {
            throw new RuntimeException("TIME_CONFLICT");
        }

        lesson.setStatus("PENDING");
        return lessonRepository.save(lesson);
    }

    public List<Lesson> getLessonsForCoach(UUID coachId) {
        return lessonRepository.findByCoachIdOrderByStartTimeDesc(coachId);
    }

    public List<Lesson> getLessonsForStudent(UUID studentId) {
        return lessonRepository.findByStudentIdOrderByStartTimeDesc(studentId);
    }

    public Lesson updateLessonStatus(UUID lessonId, String status) {
        Lesson lesson = getLessonById(lessonId);
        lesson.setStatus(status);
        return lessonRepository.save(lesson);
    }

    public Lesson getLessonById(UUID lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
    }

    public Lesson saveLessonNotes(UUID lessonId, String notes) {
        Lesson lesson = getLessonById(lessonId);
        lesson.setNotes(notes);
        return lessonRepository.save(lesson);
    }
}