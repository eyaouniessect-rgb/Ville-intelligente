package com.ville.gestionincidents.service.log;

import com.ville.gestionincidents.entity.LogEntry;
import java.util.List;

public interface LogService {

    void saveLog(String level,
                 String message,
                 String username,
                 String action,
                 String ipAddress);

    List<LogEntry> getAllLogs();
}
