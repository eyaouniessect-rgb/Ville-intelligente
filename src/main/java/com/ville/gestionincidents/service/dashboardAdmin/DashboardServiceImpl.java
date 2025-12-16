package com.ville.gestionincidents.service.dashboardAdmin;

import com.ville.gestionincidents.dto.dashboardAdmin.DelaiResolutionDto;
import com.ville.gestionincidents.dto.dashboardAdmin.IncidentParQuartierDto;
import com.ville.gestionincidents.dto.dashboardAdmin.IncidentParServiceDto;
import com.ville.gestionincidents.entity.Departement;
import com.ville.gestionincidents.entity.Incident;
import com.ville.gestionincidents.entity.ServiceMunicipal;
import com.ville.gestionincidents.enumeration.StatutIncident;
import com.ville.gestionincidents.repository.IncidentRepository;
import com.ville.gestionincidents.repository.QuartierRepository;
import com.ville.gestionincidents.repository.ServiceMunicipalRepository;
import com.ville.gestionincidents.security.CurrentUserService;
import com.ville.gestionincidents.util.export.CsvExportService;
import com.ville.gestionincidents.util.export.PdfExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.io.Writer;
import java.time.Duration;
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
    private final CurrentUserService currentUserService;

    /* ===================== UTILS ===================== */

    private LocalDateTime start(LocalDate d) {
        return d.atStartOfDay();
    }

    private LocalDateTime end(LocalDate d) {
        return d.atTime(23, 59, 59);
    }

    /* ===================== COMPTEURS ===================== */

    @Override
    @Transactional(readOnly = true)
    public long countTotalIncidentsByDepartement(
            Departement d, LocalDate dd, LocalDate df) {

        return incidentRepository
                .countByService_DepartementAndDateDeclarationBetween(
                        d, start(dd), end(df));
    }

    @Override
    @Transactional(readOnly = true)
    public long countIncidentsResolusByDepartement(
            Departement d, LocalDate dd, LocalDate df) {

        return incidentRepository
                .countByService_DepartementAndDateDeclarationBetweenAndStatut(
                        d, start(dd), end(df), StatutIncident.RESOLU);
    }

    @Override
    @Transactional(readOnly = true)
    public long countIncidentsEnCoursByDepartement(
            Departement d, LocalDate dd, LocalDate df) {

        return incidentRepository
                .countByService_DepartementAndDateDeclarationBetweenAndStatutIn(
                        d,
                        start(dd),
                        end(df),
                        List.of(
                                StatutIncident.PRIS_EN_CHARGE,
                                StatutIncident.EN_RESOLUTION
                        )
                );
    }

    @Override
    @Transactional(readOnly = true)
    public double calculerDelaiMoyenResolutionByDepartement(
            Departement d, LocalDate dd, LocalDate df) {

        List<Incident> incidents =
                incidentRepository.findByDateDeclarationBetween(start(dd), end(df))
                        .stream()
                        .filter(i ->
                                i.getService() != null &&
                                        i.getService().getDepartement().equals(d) &&
                                        i.getStatut() == StatutIncident.RESOLU &&
                                        i.getDateResolution() != null  // ✅ CORRECTION ICI
                        )
                        .collect(Collectors.toList());

        if (incidents.isEmpty()) return 0;

        return incidents.stream()
                .mapToLong(i ->
                        ChronoUnit.DAYS.between(
                                i.getDateDeclaration(),
                                i.getDateResolution()  // ✅ CORRECTION ICI
                        )
                )
                .average()
                .orElse(0);
    }
    /* ===================== GRAPHIQUES ===================== */

    @Override
    @Transactional(readOnly = true)
    public List<IncidentParServiceDto> getIncidentsParServiceByDepartement(
            Departement d, LocalDate dd, LocalDate df, Long serviceId) {

        return serviceMunicipalRepository.findByDepartement(d).stream()
                .filter(s -> serviceId == null || s.getId().equals(serviceId)) // filtre optionnel
                .map(s -> new IncidentParServiceDto(
                        s.getNom(),
                        incidentRepository.countByServiceAndDateDeclarationBetween(
                                s, start(dd), end(df)
                        )
                ))
                .filter(x -> x.getNombre() > 0)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidentParQuartierDto> getIncidentsParQuartierByDepartement(
            Departement d, LocalDate dd, LocalDate df, Long quartierId) {

        return incidentRepository.findByDateDeclarationBetween(start(dd), end(df))
                .stream()
                .filter(i -> i.getService() != null &&
                        i.getService().getDepartement().equals(d) &&
                        i.getQuartier() != null &&
                        (quartierId == null || i.getQuartier().getId().equals(quartierId))
                )
                .collect(Collectors.groupingBy(
                        i -> i.getQuartier().getNom(),
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .map(e -> new IncidentParQuartierDto(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }


    // Délai moyen global
    @Override
    public double calculDelaiMoyenGlobal() {
        List<Incident> incidentsResolus = incidentRepository.findByStatut(StatutIncident.RESOLU);
        return incidentsResolus.stream()
                .filter(i -> i.getDateResolution() != null && i.getDateDeclaration() != null)
                .mapToLong(i -> Duration.between(i.getDateDeclaration(), i.getDateResolution()).toDays())
                .average()
                .orElse(0);
    }

    // Délai moyen par service
    @Override
    public List<DelaiResolutionDto> getDelaiResolutionParServiceByDepartement(
            Departement d, LocalDate dd, LocalDate df) {

        return serviceMunicipalRepository.findByDepartement(d).stream()
                .map(s -> {
                    var list = incidentRepository
                            .findByServiceAndDateDeclarationBetweenAndStatut(
                                    s, start(dd), end(df), StatutIncident.RESOLU);

                    double avg = list.stream()
                            .filter(i -> i.getDateResolution() != null)
                            .mapToLong(i ->
                                    ChronoUnit.DAYS.between(
                                            i.getDateDeclaration(),
                                            i.getDateResolution() // <-- ici aussi
                                    )
                            )
                            .average()
                            .orElse(0);

                    return new DelaiResolutionDto(s.getNom(), avg);
                })
                .filter(x -> x.getDelaiMoyen() > 0)
                .collect(Collectors.toList());
    }



    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getIncidentsParStatutByDepartement(
            Departement d, LocalDate dd, LocalDate df) {

        Map<String, Long> result = new LinkedHashMap<>();

        for (StatutIncident statut : StatutIncident.values()) {
            long count = incidentRepository
                    .countByService_DepartementAndDateDeclarationBetweenAndStatut(
                            d, start(dd), end(df), statut);
            result.put(statut.name(), count);
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getIncidentsParMoisByDepartement(
            Departement d, LocalDate dd, LocalDate df) {

        return incidentRepository.findByDateDeclarationBetween(start(dd), end(df))
                .stream()
                .filter(i ->
                        i.getService() != null &&
                                i.getService().getDepartement().equals(d)
                )
                .collect(Collectors.groupingBy(
                        i -> i.getDateDeclaration().getYear() + "-" +
                                String.format("%02d", i.getDateDeclaration().getMonthValue()),
                        Collectors.counting()
                ));
    }

    /* ===================== EXPORTS ===================== */
    @Override
    public void exportCsv(LocalDate dd, LocalDate df, Long serviceId, Long quartierId, Writer writer) throws Exception {

        Departement dept = currentUserService.getCurrentUser().getDepartement();

        csvExportService.exportDashboardData(
                dd, df,
                getIncidentsParServiceByDepartement(dept, dd, df, serviceId),
                getIncidentsParQuartierByDepartement(dept, dd, df, quartierId),
                getDelaiResolutionParServiceByDepartement(dept, dd, df),
                writer
        );
    }

    @Override
    public void exportPdf(LocalDate dd, LocalDate df, Long serviceId, Long quartierId, OutputStream out) throws Exception {

        Departement dept = currentUserService.getCurrentUser().getDepartement();

        pdfExportService.exportDashboardData(
                dd, df,
                countTotalIncidentsByDepartement(dept, dd, df),
                countIncidentsResolusByDepartement(dept, dd, df),
                countIncidentsEnCoursByDepartement(dept, dd, df),
                calculerDelaiMoyenResolutionByDepartement(dept, dd, df),
                getIncidentsParServiceByDepartement(dept, dd, df, serviceId),
                getIncidentsParQuartierByDepartement(dept, dd, df, quartierId),
                getDelaiResolutionParServiceByDepartement(dept, dd, df),
                out
        );
    }


}
