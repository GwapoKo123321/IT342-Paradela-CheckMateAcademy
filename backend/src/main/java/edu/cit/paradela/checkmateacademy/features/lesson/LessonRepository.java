package edu.cit.paradela.checkmateacademy.features.lesson;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, UUID> {

    List<Lesson> findByCoachIdOrderByStartTimeDesc(UUID coachId);
    List<Lesson> findByStudentIdOrderByStartTimeDesc(UUID studentId);

    @Query("SELECT COUNT(l) > 0 FROM Lesson l WHERE l.coachId = :coachId AND l.status != 'REJECTED' AND l.startTime < :endTime AND l.endTime > :startTime")
    boolean existsOverlappingLesson(@Param("coachId") UUID coachId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COUNT(l) > 0 FROM Lesson l WHERE l.studentId = :studentId AND l.status != 'REJECTED' AND l.startTime < :endTime AND l.endTime > :startTime")
    boolean existsOverlappingStudentLesson(@Param("studentId") UUID studentId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query("SELECT l FROM Lesson l WHERE l.coachId = :coachId AND l.status != 'REJECTED' AND l.startTime < :endTime AND l.endTime > :startTime")
    List<Lesson> findOverlappingLessons(@Param("coachId") UUID coachId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query("SELECT l FROM Lesson l WHERE l.studentId = :studentId AND l.status != 'REJECTED' AND l.startTime < :endTime AND l.endTime > :startTime")
    List<Lesson> findOverlappingStudentLessons(@Param("studentId") UUID studentId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
