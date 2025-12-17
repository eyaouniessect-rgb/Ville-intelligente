package com.ville.gestionincidents.util;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;

import org.springframework.stereotype.Component;

import com.ville.gestionincidents.service.log.LogService;

@Aspect
@Component
public class LoggingAspect {

    private final LogService logService;

    public LoggingAspect(LogService logService) {
        this.logService = logService;
    }

    @After("execution(* com.ville.gestionincidents.controller.*.*(..))")
    public void logAction(JoinPoint joinPoint) {
        String action = joinPoint.getSignature().getName();
        logService.saveLog(
                "INFO",
                "Action exécutée",
                "SYSTEM",
                action,
                "N/A"
        );
    }
}

