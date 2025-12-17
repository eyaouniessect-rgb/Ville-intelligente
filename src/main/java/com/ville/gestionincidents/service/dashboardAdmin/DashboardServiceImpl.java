package com.ville.gestionincidents.service.dashboardAdmin;

import com.ville.gestionincidents.dto.dashboardAdmin.DelaiResolutionDto;
import com.ville.gestionincidents.dto.dashboardAdmin.IncidentParQuartierDto;
import com.ville.gestionincidents.dto.dashboardAdmin.IncidentParServiceDto;
import com.ville.gestionincidents.entity.Departement;
import com.ville.gestionincidents.entity.Incident;
import com.ville.gestionincidents.entity.Rapport;
import com.ville.gestionincidents.enumeration.FormatRapport;
import com.ville.gestionincidents.enumeration.StatutIncident;
import com.ville.gestionincidents.repository.IncidentRepository;
import com.ville.gestionincidents.repository.QuartierRepository;
import com.ville.gestionincidents.repository.RapportRepository;
import com.ville.gestionincidents.repository.ServiceMunicipalRepository;
import com.ville.gestionincidents.security.CurrentUserService;
import com.ville.gestionincidents.util.export.CsvExportService;
import com.ville.gestionincidents.util.export.PdfExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final IncidentRepository incidentRepository;
    private final ServiceMunicipalRepository serviceMunicipalRepository;
    private final QuartierRepository quartierRepository;
    private final RapportRepository rapportRepository;
    private final CsvExportService csvExportService;
    private final PdfExportService pdfExportService;
    private final CurrentUserService currentUserService;

    @Value("${app.rapports.storage-path:./rapports}")
    private String rapportsStoragePath;

    /* ===================== UTILS ===================== */

    private LocalDateTime start(LocalDate d) {
        return d.atStartOfDay();
    }

    private LocalDateTime end(LocalDate d) {
        return d.atTime(23, 59, 59);
    }

    /**
     * ✅ Méthode utilitaire pour appliquer les filtres service et quartier
     */
    private List<Incident> applyFilters(List<Incident> incidents, Long serviceId, Long quartierId) {
        return incidents.stream()
                .filter(i -> serviceId == null ||
                        (i.getService() != null && i.getService().getId().equals(serviceId)))
                .filter(i -> quartierId == null ||
                        (i.getQuartier() != null && i.getQuartier().getId().equals(quartierId)))
                .collect(Collectors.toList());
    }

    /* ===================== COMPTEURS ===================== */

    @Override
    @Transactional(readOnly = true)
    public long countTotalIncidentsByDepartement(
            Departement d, LocalDate dd, LocalDate df, Long serviceId, Long quartierId) {

        List<Incident> incidents = incidentRepository
                .findByDepartementAndDateDeclarationBetween(d, start(dd), end(df));

        return applyFilters(incidents, serviceId, quartierId).size();
    }

    @Override
    @Transactional(readOnly = true)
    public long countIncidentsResolusByDepartement(
            Departement d, LocalDate dd, LocalDate df, Long serviceId, Long quartierId) {

        List<Incident> incidents = incidentRepository
                .findByDepartementAndDateDeclarationBetweenAndStatut(
                        d, start(dd), end(df), StatutIncident.RESOLU);

        return applyFilters(incidents, serviceId, quartierId).size();
    }

    @Override
    @Transactional(readOnly = true)
    public long countIncidentsEnCoursByDepartement(
            Departement d, LocalDate dd, LocalDate df, Long serviceId, Long quartierId) {

        List<Incident> incidents = incidentRepository
                .findByDepartementAndDateDeclarationBetween(d, start(dd), end(df))
                .stream()
                .filter(i -> i.getStatut() == StatutIncident.PRIS_EN_CHARGE ||
                        i.getStatut() == StatutIncident.EN_RESOLUTION)
                .collect(Collectors.toList());

        return applyFilters(incidents, serviceId, quartierId).size();
    }

    @Override
    @Transactional(readOnly = true)
    public double calculerDelaiMoyenResolutionByDepartement(
            Departement d, LocalDate dd, LocalDate df, Long serviceId, Long quartierId) {

        List<Incident> incidents = incidentRepository
                .findByDepartementAndDateDeclarationBetweenAndStatut(
                        d, start(dd), end(df), StatutIncident.RESOLU);

        incidents = applyFilters(incidents, serviceId, quartierId);

        if (incidents.isEmpty()) return 0;

        return incidents.stream()
                .filter(i -> i.getDateResolution() != null)
                .mapToLong(i -> ChronoUnit.DAYS.between(
                        i.getDateDeclaration(),
                        i.getDateResolution()))
                .average()
                .orElse(0);
    }

    /* ===================== GRAPHIQUES ===================== */

    @Override
    @Transactional(readOnly = true)
    public List<IncidentParServiceDto> getIncidentsParServiceByDepartement(
            Departement d, LocalDate dd, LocalDate df, Long serviceId, Long quartierId) {

        // Récupérer tous les incidents du département
        List<Incident> incidents = incidentRepository
                .findByDepartementAndDateDeclarationBetween(d, start(dd), end(df));

        // Appliquer le filtre quartier
        incidents = applyFilters(incidents, null, quartierId);

        // Grouper par service
        return incidents.stream()
                .filter(i -> i.getService() != null)
                .filter(i -> serviceId == null || i.getService().getId().equals(serviceId))
                .collect(Collectors.groupingBy(
                        i -> i.getService().getNom(),
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .map(e -> new IncidentParServiceDto(e.getKey(), e.getValue()))
                .sorted((a, b) -> Long.compare(b.getNombre(), a.getNombre()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidentParQuartierDto> getIncidentsParQuartierByDepartement(
            Departement d, LocalDate dd, LocalDate df, Long serviceId, Long quartierId) {

        // Récupérer tous les incidents du département
        List<Incident> incidents = incidentRepository
                .findByDepartementAndDateDeclarationBetween(d, start(dd), end(df));

        // Appliquer le filtre service
        incidents = applyFilters(incidents, serviceId, null);

        // Grouper par quartier
        return incidents.stream()
                .filter(i -> i.getQuartier() != null)
                .filter(i -> quartierId == null || i.getQuartier().getId().equals(quartierId))
                .collect(Collectors.groupingBy(
                        i -> i.getQuartier().getNom(),
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .map(e -> new IncidentParQuartierDto(e.getKey(), e.getValue()))
                .sorted((a, b) -> Long.compare(b.getNombre(), a.getNombre()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DelaiResolutionDto> getDelaiResolutionParServiceByDepartement(
            Departement d, LocalDate dd, LocalDate df, Long serviceId, Long quartierId) {

        // Récupérer tous les incidents résolus
        List<Incident> incidents = incidentRepository
                .findByDepartementAndDateDeclarationBetweenAndStatut(
                        d, start(dd), end(df), StatutIncident.RESOLU);

        // Appliquer les filtres
        incidents = applyFilters(incidents, serviceId, quartierId);

        // Grouper par service et calculer le délai moyen
        return incidents.stream()
                .filter(i -> i.getService() != null && i.getDateResolution() != null)
                .collect(Collectors.groupingBy(i -> i.getService().getNom()))
                .entrySet()
                .stream()
                .map(e -> {
                    double avg = e.getValue().stream()
                            .mapToLong(i -> ChronoUnit.DAYS.between(
                                    i.getDateDeclaration(),
                                    i.getDateResolution()))
                            .average()
                            .orElse(0);
                    return new DelaiResolutionDto(e.getKey(), avg);
                })
                .filter(dto -> dto.getDelaiMoyen() > 0)
                .sorted((a, b) -> Double.compare(b.getDelaiMoyen(), a.getDelaiMoyen()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getIncidentsParStatutByDepartement(
            Departement d, LocalDate dd, LocalDate df, Long serviceId, Long quartierId) {

        List<Incident> incidents = incidentRepository
                .findByDepartementAndDateDeclarationBetween(d, start(dd), end(df));

        incidents = applyFilters(incidents, serviceId, quartierId);

        Map<String, Long> result = new LinkedHashMap<>();

        for (StatutIncident statut : StatutIncident.values()) {
            long count = incidents.stream()
                    .filter(i -> i.getStatut() == statut)
                    .count();
            result.put(statut.name(), count);
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getIncidentsParMoisByDepartement(
            Departement d, LocalDate dd, LocalDate df, Long serviceId, Long quartierId) {

        List<Incident> incidents = incidentRepository
                .findByDepartementAndDateDeclarationBetween(d, start(dd), end(df));

        incidents = applyFilters(incidents, serviceId, quartierId);

        return incidents.stream()
                .collect(Collectors.groupingBy(
                        i -> i.getDateDeclaration().getYear() + "-" +
                                String.format("%02d", i.getDateDeclaration().getMonthValue()),
                        Collectors.counting()
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public double calculDelaiMoyenGlobal() {
        List<Incident> incidentsResolus = incidentRepository.findByStatut(StatutIncident.RESOLU);
        return incidentsResolus.stream()
                .filter(i -> i.getDateResolution() != null && i.getDateDeclaration() != null)
                .mapToLong(i -> ChronoUnit.DAYS.between(
                        i.getDateDeclaration(),
                        i.getDateResolution()))
                .average()
                .orElse(0);
    }

    /* ===================== EXPORTS (Téléchargement direct) ===================== */

    @Override
    public void exportCsv(LocalDate dd, LocalDate df, Long serviceId, Long quartierId, Writer writer) throws Exception {
        Departement dept = currentUserService.getCurrentUser().getDepartement();

        csvExportService.exportDashboardData(
                dd, df,
                getIncidentsParServiceByDepartement(dept, dd, df, serviceId, quartierId),
                getIncidentsParQuartierByDepartement(dept, dd, df, serviceId, quartierId),
                getDelaiResolutionParServiceByDepartement(dept, dd, df, serviceId, quartierId),
                writer
        );
    }

    @Override
    public void exportPdf(LocalDate dd, LocalDate df, Long serviceId, Long quartierId, OutputStream out) throws Exception {
        Departement dept = currentUserService.getCurrentUser().getDepartement();

        pdfExportService.exportDashboardData(
                dd, df,
                countTotalIncidentsByDepartement(dept, dd, df, serviceId, quartierId),
                countIncidentsResolusByDepartement(dept, dd, df, serviceId, quartierId),
                countIncidentsEnCoursByDepartement(dept, dd, df, serviceId, quartierId),
                calculerDelaiMoyenResolutionByDepartement(dept, dd, df, serviceId, quartierId),
                getIncidentsParServiceByDepartement(dept, dd, df, serviceId, quartierId),
                getIncidentsParQuartierByDepartement(dept, dd, df, serviceId, quartierId),
                getDelaiResolutionParServiceByDepartement(dept, dd, df, serviceId, quartierId),
                out
        );
    }

    /* ===================== EXPORTS (Sauvegarde en BD) ===================== */

    @Override
    @Transactional
    public Rapport exportCsvAndSave(LocalDate dd, LocalDate df, Long serviceId, Long quartierId) throws Exception {
        Departement dept = currentUserService.getCurrentUser().getDepartement();

        // Créer le répertoire si nécessaire
        Path storagePath = Paths.get(rapportsStoragePath);
        Files.createDirectories(storagePath);

        // Générer le nom de fichier
        String fileName = generateFileName("rapport", "csv", dd, df);
        Path filePath = storagePath.resolve(fileName);

        // Générer le CSV
        try (Writer writer = new FileWriter(filePath.toFile())) {
            csvExportService.exportDashboardData(
                    dd, df,
                    getIncidentsParServiceByDepartement(dept, dd, df, serviceId, quartierId),
                    getIncidentsParQuartierByDepartement(dept, dd, df, serviceId, quartierId),
                    getDelaiResolutionParServiceByDepartement(dept, dd, df, serviceId, quartierId),
                    writer
            );
        }

        // Sauvegarder dans la BD
        return saveRapport(dd, df, serviceId, quartierId, fileName, filePath.toString(), FormatRapport.CSV, dept);
    }

    @Override
    @Transactional
    public Rapport exportPdfAndSave(LocalDate dd, LocalDate df, Long serviceId, Long quartierId) throws Exception {
        Departement dept = currentUserService.getCurrentUser().getDepartement();

        // Créer le répertoire si nécessaire
        Path storagePath = Paths.get(rapportsStoragePath);
        Files.createDirectories(storagePath);

        // Générer le nom de fichier
        String fileName = generateFileName("rapport", "pdf", dd, df);
        Path filePath = storagePath.resolve(fileName);

        // Générer le PDF
        try (OutputStream out = new FileOutputStream(filePath.toFile())) {
            pdfExportService.exportDashboardData(
                    dd, df,
                    countTotalIncidentsByDepartement(dept, dd, df, serviceId, quartierId),
                    countIncidentsResolusByDepartement(dept, dd, df, serviceId, quartierId),
                    countIncidentsEnCoursByDepartement(dept, dd, df, serviceId, quartierId),
                    calculerDelaiMoyenResolutionByDepartement(dept, dd, df, serviceId, quartierId),
                    getIncidentsParServiceByDepartement(dept, dd, df, serviceId, quartierId),
                    getIncidentsParQuartierByDepartement(dept, dd, df, serviceId, quartierId),
                    getDelaiResolutionParServiceByDepartement(dept, dd, df, serviceId, quartierId),
                    out
            );
        }

        // Sauvegarder dans la BD
        return saveRapport(dd, df, serviceId, quartierId, fileName, filePath.toString(), FormatRapport.PDF, dept);
    }

    /* ===================== GESTION DES RAPPORTS ===================== */

    @Override
    @Transactional(readOnly = true)
    public List<Rapport> getRapportsByDepartement() {
        Departement dept = currentUserService.getCurrentUser().getDepartement();
        return rapportRepository.findByDepartementOrderByDateGenerationDesc(dept);
    }

    @Override
    @Transactional(readOnly = true)
    public Rapport getRapportById(Long rapportId) {
        return rapportRepository.findById(rapportId)
                .orElseThrow(() -> new RuntimeException("Rapport non trouvé avec l'ID: " + rapportId));
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] telechargerRapport(Long rapportId) throws IOException {
        Rapport rapport = getRapportById(rapportId);

        // Vérifier que l'utilisateur a accès à ce rapport
        Departement userDept = currentUserService.getCurrentUser().getDepartement();
        if (!rapport.getDepartement().getId().equals(userDept.getId())) {
            throw new RuntimeException("Accès non autorisé à ce rapport");
        }

        Path path = Paths.get(rapport.getCheminFichier());

        if (!Files.exists(path)) {
            throw new FileNotFoundException("Le fichier du rapport n'existe pas: " + rapport.getCheminFichier());
        }

        return Files.readAllBytes(path);
    }

    @Override
    @Transactional
    public void supprimerRapport(Long rapportId) throws IOException {
        Rapport rapport = getRapportById(rapportId);

        // Vérifier que l'utilisateur a accès à ce rapport
        Departement userDept = currentUserService.getCurrentUser().getDepartement();
        if (!rapport.getDepartement().getId().equals(userDept.getId())) {
            throw new RuntimeException("Accès non autorisé à ce rapport");
        }

        // Supprimer le fichier physique
        Path path = Paths.get(rapport.getCheminFichier());
        if (Files.exists(path)) {
            Files.delete(path);
        }

        // Supprimer l'enregistrement de la BD
        rapportRepository.delete(rapport);
    }

    /* ===================== MÉTHODES PRIVÉES ===================== */

    private Rapport saveRapport(LocalDate dd, LocalDate df, Long serviceId, Long quartierId,
                                String fileName, String cheminFichier, FormatRapport format,
                                Departement dept) throws IOException {

        File file = new File(cheminFichier);

        Rapport rapport = Rapport.builder()
                .titre(generateTitre(dd, df))
                .dateDebut(dd)
                .dateFin(df)
                .dateGeneration(LocalDateTime.now())
                .format(format)
                .nomFichier(fileName)
                .cheminFichier(cheminFichier)
                .tailleFichier(file.length())
                .departement(dept)
                .service(serviceId != null ? serviceMunicipalRepository.findById(serviceId).orElse(null) : null)
                .quartier(quartierId != null ? quartierRepository.findById(quartierId).orElse(null) : null)
                .generePar(currentUserService.getCurrentUser())
                .totalIncidents(countTotalIncidentsByDepartement(dept, dd, df, serviceId, quartierId))
                .incidentsResolus(countIncidentsResolusByDepartement(dept, dd, df, serviceId, quartierId))
                .incidentsEnCours(countIncidentsEnCoursByDepartement(dept, dd, df, serviceId, quartierId))
                .delaiMoyen(calculerDelaiMoyenResolutionByDepartement(dept, dd, df, serviceId, quartierId))
                .build();

        return rapportRepository.save(rapport);
    }

    private String generateFileName(String prefix, String extension, LocalDate dd, LocalDate df) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return String.format("%s_%s_%s_%s.%s", prefix, dd.format(fmt), df.format(fmt), timestamp, extension);
    }

    private String generateTitre(LocalDate dd, LocalDate df) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return String.format("Rapport du %s au %s", dd.format(fmt), df.format(fmt));
    }
}