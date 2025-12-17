package com.ville.gestionincidents.util;

import com.ville.gestionincidents.service.log.LogService;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class LoggingAspect {

    private final LogService logService;
    private final HttpServletRequest request;

    public LoggingAspect(LogService logService,
                         HttpServletRequest request) {
        this.logService = logService;
        this.request = request;
    }

    /**
     *  Journalisation des actions normales
     */
    @After("execution(* com.ville.gestionincidents.controller..*(..))")
    public void logAction(JoinPoint joinPoint) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null) ? auth.getName() : "SYSTEM";

        String action = joinPoint.getSignature().getName();

        logService.saveLog(
                "INFO",
                "Action exécutée",
                username,
                action,
                request.getRemoteAddr()
        );
    }

    /**
     *  Journalisation des tentatives suspectes
     */
    @AfterThrowing(
            pointcut = "execution(* com.ville.gestionincidents.controller..*(..))",
            throwing = "ex"
    )
    public void logSuspiciousAccess(AccessDeniedException ex) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null) ? auth.getName() : "ANONYMOUS";

        logService.saveLog(
                "WARN",
                "Tentative d'accès non autorisée",
                username,
                request.getRequestURI(),
                request.getRemoteAddr()
        );
    }
}
