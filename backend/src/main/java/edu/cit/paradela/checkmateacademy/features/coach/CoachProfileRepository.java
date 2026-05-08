package edu.cit.paradela.checkmateacademy.features.coach;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CoachProfileRepository extends JpaRepository<CoachProfile, UUID> {
    Optional<CoachProfile> findByCoachId(UUID coachId);
}
