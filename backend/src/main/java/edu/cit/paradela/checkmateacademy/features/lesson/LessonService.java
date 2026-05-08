package edu.cit.paradela.checkmateacademy.features.lesson;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class LessonService {

    @Autowired
    private LessonRepository lessonRepository;

    public Lesson createBooking(Lesson lesson) {
        boolean isBooked = lessonRepository.existsOverlappingLesson(
                lesson.getCoachId(), lesson.getStartTime(), lesson.getEndTime()
        );
        if (isBooked) throw new RuntimeException("TIME_CONFLICT");

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

    // NEW: Saves the live piece movements
    public Lesson updateBoardState(UUID lessonId, String boardState) {
        Lesson lesson = getLessonById(lessonId);
        lesson.setBoardState(boardState);
        return lessonRepository.save(lesson);
    }
}