package edu.cit.paradela.checkmateacademy.features.lesson;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lessons")
@Data
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "coach_id")
    private UUID coachId;

    @Column(name = "student_id")
    private UUID studentId;

    @Column(name = "student_name")
    private String studentName;

    @Column(name = "coach_name")
    private String coachName;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    private String status = "PENDING";

    @Column(columnDefinition = "TEXT")
    private String notes; //  Chat JSON array

    @Column(name = "board_state", columnDefinition = "TEXT")
    private String boardState; // FEN snapshot


    @Column(name = "pgn_history", columnDefinition = "TEXT")
    private String pgnHistory;
}