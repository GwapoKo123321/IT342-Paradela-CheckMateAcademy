package edu.cit.paradela.checkmateacademy.features.coach;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Entity
@Table(name = "coach_profiles")
@Data
public class CoachProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "coach_id", nullable = false, unique = true)
    private UUID coachId;

    @Column(columnDefinition = "TEXT")
    private String specialties;

    @Column(columnDefinition = "TEXT")
    private String bio;
}
