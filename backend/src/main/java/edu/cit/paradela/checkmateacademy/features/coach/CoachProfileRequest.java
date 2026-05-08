package edu.cit.paradela.checkmateacademy.features.coach;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class CoachProfileRequest {
    private String specialties;
    private String bio;
    private List<CoachAvailability> availability = new ArrayList<>();
}
