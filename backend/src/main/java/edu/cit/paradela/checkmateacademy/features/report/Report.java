    package edu.cit.paradela.checkmateacademy.features.report;

    import jakarta.persistence.*;
    import lombok.Data;
    import java.time.LocalDateTime;
    import java.util.UUID;

    @Entity
    @Table(name = "user_reports")
    @Data
    public class Report {
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private UUID id;

        @Column(name = "reporter_id")
        private UUID reporterId;

        @Column(name = "reported_id")
        private UUID reportedId;

        @Column(name = "reported_name")
        private String reportedName;

        @Column(columnDefinition = "TEXT")
        private String reason;

        @Column(name = "created_at")
        private LocalDateTime createdAt = LocalDateTime.now();

        private String status = "PENDING"; // Can be PENDING or RESOLVED
    }