package com.ville.gestionincidents.service.notification;

import com.ville.gestionincidents.entity.Notification;
import com.ville.gestionincidents.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public List<Notification> findByUserEmail(String email) {
        return notificationRepository.findByUtilisateurEmail(email);
    }
}
