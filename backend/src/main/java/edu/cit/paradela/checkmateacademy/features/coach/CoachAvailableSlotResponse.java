package edu.cit.paradela.checkmateacademy.features.coach;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CoachAvailableSlotResponse {
    private UUID coachId;
    private String coachName;
    private Integer currentElo;
    private String specialties;
    private String bio;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public CoachAvailableSlotResponse(CoachProfileResponse coach, LocalDateTime startTime, LocalDateTime endTime) {
        this.coachId = coach.getId();
        this.coachName = coach.getFullName();
        this.currentElo = coach.getCurrentElo();
        this.specialties = coach.getSpecialties();
        this.bio = coach.getBio();
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
