package edu.cit.paradela.checkmateacademy.features.auth;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Entity
@Table(name = "profiles")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String email;
    private String password;

    @Column(name = "full_name")
    private String fullName;

    private String role;

    @Column(name = "chess_username")
    private String chessUsername;

    @Column(name = "current_elo")
    private Integer currentElo = 0;
}