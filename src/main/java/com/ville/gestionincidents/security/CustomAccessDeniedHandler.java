package com.ville.gestionincidents.security;

import com.ville.gestionincidents.service.log.LogService;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final LogService logService;

    public CustomAccessDeniedHandler(LogService logService) {
        this.logService = logService;
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {

        String username = (request.getUserPrincipal() != null)
                ? request.getUserPrincipal().getName()
                : "ANONYMOUS";

        logService.saveLog(
                "WARN",
                "Tentative d'accès non autorisée",
                username,
                request.getRequestURI(),
                request.getRemoteAddr()
        );

        response.sendRedirect("/access-denied");
    }
}
