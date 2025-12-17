package com.ville.gestionincidents.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String level;        // INFO, WARN, ERROR
    private String message;

    private String username;     // utilisateur concerné
    private String action;       // LOGIN, DELETE, ACCESS_DENIED

    private LocalDateTime timestamp;

    private String ipAddress;
}
