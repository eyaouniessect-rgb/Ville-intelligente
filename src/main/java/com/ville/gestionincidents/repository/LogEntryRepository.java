package com.ville.gestionincidents.repository;

import com.ville.gestionincidents.entity.LogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface LogEntryRepository extends JpaRepository<LogEntry, Long> {

    @Query("""
        SELECT COUNT(l) FROM LogEntry l
        WHERE l.action = 'LOGIN_FAILED'
        AND l.username = :username
        AND l.timestamp >= :since
    """)
    int countFailedLogins(@Param("username") String username,
                          @Param("since") LocalDateTime since);
}
