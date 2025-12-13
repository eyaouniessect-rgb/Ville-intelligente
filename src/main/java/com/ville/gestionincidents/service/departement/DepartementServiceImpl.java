package com.ville.gestionincidents.service.departement;

import com.ville.gestionincidents.entity.Departement;
import com.ville.gestionincidents.repository.DepartementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implémentation du service Département
 */
@Service //  C’EST ICI que va @Service
public class DepartementServiceImpl implements DepartementService {

    @Autowired
    private DepartementRepository departementRepository;

    @Override
    public List<Departement> findAll() {
        return departementRepository.findAll();
    }
}
