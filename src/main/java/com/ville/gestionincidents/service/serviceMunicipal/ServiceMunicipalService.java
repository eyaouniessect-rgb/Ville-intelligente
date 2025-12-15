package com.ville.gestionincidents.service.serviceMunicipal;

import com.ville.gestionincidents.entity.Departement;
import com.ville.gestionincidents.entity.ServiceMunicipal;
import java.util.List;

public interface ServiceMunicipalService {

    ServiceMunicipal addServiceToDepartement(Long departementId, ServiceMunicipal service);
    ServiceMunicipal updateService(Long serviceId, ServiceMunicipal service);
    void deleteService(Long serviceId);

    ServiceMunicipal findServiceById(Long serviceId);
    List<ServiceMunicipal> findServicesByDepartement(Long departementId);
    List<ServiceMunicipal> findAllServices();

    long countServices();

    List<ServiceMunicipal> findByDepartement(Departement departement);
}
