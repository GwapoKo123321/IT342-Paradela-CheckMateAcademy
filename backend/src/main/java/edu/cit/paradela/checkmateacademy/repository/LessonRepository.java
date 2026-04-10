package edu.cit.paradela.checkmateacademy.repository;

import edu.cit.paradela.checkmateacademy.model.Lesson;
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

    // NEW: Prevents any time overlaps, ensuring the coach is 100% free for that block
    @Query("SELECT COUNT(l) > 0 FROM Lesson l WHERE l.coachId = :coachId AND l.status != 'REJECTED' AND l.startTime < :endTime AND l.endTime > :startTime")
    boolean existsOverlappingLesson(@Param("coachId") UUID coachId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}