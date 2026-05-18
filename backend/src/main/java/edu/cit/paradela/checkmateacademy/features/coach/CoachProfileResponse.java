package edu.cit.paradela.checkmateacademy.features.coach;

import edu.cit.paradela.checkmateacademy.features.auth.User;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class CoachProfileResponse {
    private UUID id;
    private String email;
    private String fullName;
    private String role;
    private String chessUsername;
    private Integer currentElo;
    private boolean eloVerified;
    private String specialties;
    private String bio;
    private List<CoachAvailability> availability = new ArrayList<>();

    public CoachProfileResponse(User user, CoachProfile profile, List<CoachAvailability> availability) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.fullName = user.getFullName();
        this.role = user.getRole();
        this.chessUsername = user.getChessUsername();
        this.currentElo = user.getCurrentElo();


        this.eloVerified = user.getEloVerified() != null ? user.getEloVerified() : false;

        this.specialties = profile != null ? profile.getSpecialties() : "";
        this.bio = profile != null ? profile.getBio() : "";
        this.availability = availability;
    }
}