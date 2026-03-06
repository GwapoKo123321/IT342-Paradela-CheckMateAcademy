package edu.cit.paradela.checkmateacademy.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "profiles")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private java.util.UUID id;

    private String email;
    private String password;

    @Column(name = "full_name")
    private String full_name;

    @Column(name = "role")
    private String role;

    @Column(name = "chess_username")
    private String chess_username;

    @Column(name = "current_elo")
    private Integer current_elo;
}