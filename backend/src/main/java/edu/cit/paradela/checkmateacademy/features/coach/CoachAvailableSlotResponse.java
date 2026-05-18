package edu.cit.paradela.checkmateacademy.features.coach;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CoachAvailableSlotResponse {
    private UUID coachId;
    private String coachName;
    private Integer currentElo;
    private boolean eloVerified;
    private String specialties;
    private String bio;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean studentConflict;
    private String conflictLabel;

    public CoachAvailableSlotResponse(CoachProfileResponse coach, LocalDateTime startTime, LocalDateTime endTime, boolean studentConflict) {
        this.coachId = coach.getId();
        this.coachName = coach.getFullName();
        this.currentElo = coach.getCurrentElo();
        this.eloVerified = coach.isEloVerified();
        this.specialties = coach.getSpecialties();
        this.bio = coach.getBio();
        this.startTime = startTime;
        this.endTime = endTime;
        this.studentConflict = studentConflict;
        this.conflictLabel = studentConflict ? "Schedule conflict" : null;
    }
}