package edu.cit.paradela.checkmateacademy.features.coach;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface CoachAvailabilityRepository extends JpaRepository<CoachAvailability, UUID> {
    List<CoachAvailability> findByCoachIdOrderByDayOfWeekAscStartTimeAsc(UUID coachId);
    List<CoachAvailability> findByCoachIdAndDayOfWeek(UUID coachId, Integer dayOfWeek);
    void deleteByCoachId(UUID coachId);
}
