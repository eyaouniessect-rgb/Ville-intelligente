package com.ville.gestionincidents.service.notification;

import com.ville.gestionincidents.entity.Notification;
import java.util.List;

public interface NotificationService {

    List<Notification> findByUserEmail(String email);
}
