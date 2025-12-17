package com.ville.gestionincidents.controller;

import com.ville.gestionincidents.entity.LogEntry;
import com.ville.gestionincidents.service.log.LogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/superadmin/logs")
@PreAuthorize("hasRole('SUPERADMIN')")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping
    public String viewLogs(Model model) {

        List<LogEntry> logs = logService.getAllLogs();

        long errorCount = logs.stream()
                .filter(l -> "ERROR".equals(l.getLevel()))
                .count();

        long warningCount = logs.stream()
                .filter(l -> "WARNING".equals(l.getLevel()))
                .count();

        long infoCount = logs.stream()
                .filter(l -> "INFO".equals(l.getLevel()))
                .count();

        model.addAttribute("logs", logs);
        model.addAttribute("errorCount", errorCount);
        model.addAttribute("warningCount", warningCount);
        model.addAttribute("infoCount", infoCount);

        return "superadmin/logs";
    }
}
