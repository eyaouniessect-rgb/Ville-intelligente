package com.ville.gestionincidents.service.log;

import com.ville.gestionincidents.entity.LogEntry;
import com.ville.gestionincidents.repository.LogEntryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LogServiceImpl implements LogService {

    private final LogEntryRepository logRepository;

    public LogServiceImpl(LogEntryRepository logRepository) {
        this.logRepository = logRepository;
    }

    @Override
    public void saveLog(String level,
                        String message,
                        String username,
                        String action,
                        String ipAddress) {

        LogEntry log = new LogEntry();
        log.setLevel(level);
        log.setMessage(message);
        log.setUsername(username);
        log.setAction(action);
        log.setTimestamp(LocalDateTime.now());
        log.setIpAddress(ipAddress);

        logRepository.save(log);
    }

    @Override
    public List<LogEntry> getAllLogs() {
        return logRepository.findAll();
    }
}
