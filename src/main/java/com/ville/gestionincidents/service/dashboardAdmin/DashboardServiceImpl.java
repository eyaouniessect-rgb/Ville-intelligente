package com.ville.gestionincidents.service.dashboardAdmin;

import com.ville.gestionincidents.dto.dashboardAdmin.DelaiResolutionDto;
import com.ville.gestionincidents.dto.dashboardAdmin.IncidentParQuartierDto;
import com.ville.gestionincidents.dto.dashboardAdmin.IncidentParServiceDto;
import com.ville.gestionincidents.entity.Incident;
import com.ville.gestionincidents.entity.ServiceMunicipal;
import com.ville.gestionincidents.entity.Quartier;
import com.ville.gestionincidents.enumeration.StatutIncident;
import com.ville.gestionincidents.repository.IncidentRepository;
import com.ville.gestionincidents.repository.ServiceMunicipalRepository;
import com.ville.gestionincidents.repository.QuartierRepository;
import com.ville.gestionincidents.util.export.CsvExportService;
import com.ville.gestionincidents.util.export.PdfExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.io.Writer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final IncidentRepository incidentRepository;
    private final ServiceMunicipalRepository serviceMunicipalRepository;
    private final QuartierRepository quartierRepository;
    private final CsvExportService csvExportService;
    private final PdfExportService pdfExportService;

    @Override
    @Transactional(readOnly = true)
    public long countTotalIncidents(LocalDate dateDebut, LocalDate dateFin) {
        LocalDateTime debut = dateDebut.atStartOfDay();
        LocalDateTime fin = dateFin.atTime(23, 59, 59);
        return incidentRepository.countByDateDeclarationBetween(debut, fin);
    }

    @Override
    @Transactional(readOnly = true)
    public long countIncidentsResolus(LocalDate dateDebut, LocalDate dateFin) {
        LocalDateTime debut = dateDebut.atStartOfDay();
        LocalDateTime fin = dateFin.atTime(23, 59, 59);
        return incidentRepository.countByDateDeclarationBetweenAndStatut(
                debut, fin, StatutIncident.RESOLU);
    }

    @Override
    @Transactional(readOnly = true)
    public long countIncidentsEnCours(LocalDate dateDebut, LocalDate dateFin) {
        LocalDateTime debut = dateDebut.atStartOfDay();
        LocalDateTime fin = dateFin.atTime(23, 59, 59);
        return incidentRepository.countByDateDeclarationBetweenAndStatutIn(
                debut, fin, Arrays.asList(
                        StatutIncident.PRIS_EN_CHARGE,
                        StatutIncident.EN_RESOLUTION
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public double calculerDelaiMoyenResolution(LocalDate dateDebut, LocalDate dateFin) {
        LocalDateTime debut = dateDebut.atStartOfDay();
        LocalDateTime fin = dateFin.atTime(23, 59, 59);

        List<Incident> incidentsResolus = incidentRepository
                .findByDateDeclarationBetweenAndStatut(debut, fin, StatutIncident.RESOLU);

        if (incidentsResolus.isEmpty()) {
            return 0.0;
        }

        double totalDelai = incidentsResolus.stream()
                .filter(i -> i.getDateDeclaration() != null && i.getDateDerniereMiseAJour() != null)
                .mapToLong(i -> ChronoUnit.DAYS.between(
                        i.getDateDeclaration(),
                        i.getDateDerniereMiseAJour()))
                .sum();

        return totalDelai / incidentsResolus.size();
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidentParServiceDto> getIncidentsParService(
            LocalDate dateDebut, LocalDate dateFin) {
        LocalDateTime debut = dateDebut.atStartOfDay();
        LocalDateTime fin = dateFin.atTime(23, 59, 59);

        List<ServiceMunicipal> services = serviceMunicipalRepository.findAll();

        return services.stream()
                .map(service -> {
                    long count = incidentRepository.countByServiceAndDateDeclarationBetween(
                            service, debut, fin);
                    return new IncidentParServiceDto(service.getNom(), count);
                })
                .filter(dto -> dto.getNombre() > 0) // Filtrer les services sans incidents
                .sorted((a, b) -> Long.compare(b.getNombre(), a.getNombre()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidentParQuartierDto> getIncidentsParQuartier(
            LocalDate dateDebut, LocalDate dateFin) {
        LocalDateTime debut = dateDebut.atStartOfDay();
        LocalDateTime fin = dateFin.atTime(23, 59, 59);

        List<Quartier> quartiers = quartierRepository.findAll();

        return quartiers.stream()
                .map(quartier -> {
                    long count = incidentRepository.countByQuartierAndDateDeclarationBetween(
                            quartier, debut, fin);
                    return new IncidentParQuartierDto(quartier.getNom(), count);
                })
                .filter(dto -> dto.getNombre() > 0)
                .sorted((a, b) -> Long.compare(b.getNombre(), a.getNombre()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DelaiResolutionDto> getDelaiResolutionParService(
            LocalDate dateDebut, LocalDate dateFin) {
        LocalDateTime debut = dateDebut.atStartOfDay();
        LocalDateTime fin = dateFin.atTime(23, 59, 59);

        List<ServiceMunicipal> services = serviceMunicipalRepository.findAll();

        return services.stream()
                .map(service -> {
                    List<Incident> incidentsResolus = incidentRepository
                            .findByServiceAndDateDeclarationBetweenAndStatut(
                                    service, debut, fin, StatutIncident.RESOLU);

                    if (incidentsResolus.isEmpty()) {
                        return new DelaiResolutionDto(service.getNom(), 0.0);
                    }

                    double delaiMoyen = incidentsResolus.stream()
                            .filter(i -> i.getDateDeclaration() != null &&
                                    i.getDateDerniereMiseAJour() != null)
                            .mapToLong(i -> ChronoUnit.DAYS.between(
                                    i.getDateDeclaration(),
                                    i.getDateDerniereMiseAJour()))
                            .average()
                            .orElse(0.0);

                    return new DelaiResolutionDto(service.getNom(), delaiMoyen);
                })
                .filter(dto -> dto.getDelaiMoyen() > 0)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getIncidentsParStatut(LocalDate dateDebut, LocalDate dateFin) {
        LocalDateTime debut = dateDebut.atStartOfDay();
        LocalDateTime fin = dateFin.atTime(23, 59, 59);

        Map<String, Long> result = new LinkedHashMap<>();

        for (StatutIncident statut : StatutIncident.values()) {
            long count = incidentRepository.countByDateDeclarationBetweenAndStatut(
                    debut, fin, statut);
            result.put(statut.name(), count);
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getIncidentsParMois(LocalDate dateDebut, LocalDate dateFin) {
        LocalDateTime debut = dateDebut.atStartOfDay();
        LocalDateTime fin = dateFin.atTime(23, 59, 59);

        List<Incident> incidents = incidentRepository
                .findByDateDeclarationBetween(debut, fin);

        return incidents.stream()
                .collect(Collectors.groupingBy(
                        i -> i.getDateDeclaration().getYear() + "-" +
                                String.format("%02d", i.getDateDeclaration().getMonthValue()),
                        Collectors.counting()
                ));
    }

    @Override
    public void exportCsv(LocalDate dateDebut, LocalDate dateFin, Writer writer)
            throws Exception {
        csvExportService.exportDashboardData(
                dateDebut, dateFin,
                getIncidentsParService(dateDebut, dateFin),
                getIncidentsParQuartier(dateDebut, dateFin),
                getDelaiResolutionParService(dateDebut, dateFin),
                writer
        );
    }

    @Override
    public void exportPdf(LocalDate dateDebut, LocalDate dateFin, OutputStream outputStream)
            throws Exception {
        pdfExportService.exportDashboardData(
                dateDebut, dateFin,
                countTotalIncidents(dateDebut, dateFin),
                countIncidentsResolus(dateDebut, dateFin),
                countIncidentsEnCours(dateDebut, dateFin),
                calculerDelaiMoyenResolution(dateDebut, dateFin),
                getIncidentsParService(dateDebut, dateFin),
                getIncidentsParQuartier(dateDebut, dateFin),
                getDelaiResolutionParService(dateDebut, dateFin),
                outputStream
        );
    }
}