# 📊 Guide Complet : Dashboard Analytique pour l'Admin

## 🎯 Objectif

Créer un dashboard pour l'**ADMIN** (pas SUPERADMIN) avec :
- 📈 Graphiques Chart.js (incidents par service, quartier, délai de résolution)
- 📄 Export CSV et PDF
- 📊 Rapports analytiques via Thymeleaf

---

## 📋 Table des Matières

1. [Structure des Fichiers à Créer](#structure)
2. [Étape 1 : Créer le Controller Admin](#etape1)
3. [Étape 2 : Créer le Service de Statistiques](#etape2)
4. [Étape 3 : Créer les DTOs pour les Graphiques](#etape3)
5. [Étape 4 : Ajouter les Méthodes au Repository](#etape4)
6. [Étape 5 : Créer le Template Thymeleaf](#etape5)
7. [Étape 6 : Ajouter Chart.js](#etape6)
8. [Étape 7 : Implémenter l'Export CSV](#etape7)
9. [Étape 8 : Implémenter l'Export PDF](#etape8)
10. [Étape 9 : Configuration de la Sécurité](#etape9)

---

## 📁 Structure des Fichiers à Créer {#structure}

```
src/main/java/com/ville/gestionincidents/
├── controller/
│   └── admin/
│       └── AdminDashboardController.java          ⭐ NOUVEAU
├── service/
│   └── dashboard/
│       ├── DashboardService.java                  ⭐ NOUVEAU
│       └── DashboardServiceImpl.java              ⭐ NOUVEAU
├── dto/
│   └── dashboard/
│       ├── IncidentParServiceDto.java             ⭐ NOUVEAU
│       ├── IncidentParQuartierDto.java            ⭐ NOUVEAU
│       └── DelaiResolutionDto.java                ⭐ NOUVEAU
└── util/
    └── export/
        ├── CsvExportService.java                  ⭐ NOUVEAU
        └── PdfExportService.java                   ⭐ NOUVEAU

src/main/resources/templates/
└── admin/
    └── dashboard.html                             ⭐ NOUVEAU

src/main/resources/static/
└── js/
    └── chart-config.js                            ⭐ NOUVEAU (optionnel)
```

---

## 🔧 Étape 1 : Créer le Controller Admin {#etape1}

### 📄 Fichier : `src/main/java/com/ville/gestionincidents/controller/admin/AdminDashboardController.java`

```java
package com.ville.gestionincidents.controller.admin;

import com.ville.gestionincidents.service.dashboard.DashboardService;
import com.ville.gestionincidents.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final DashboardService dashboardService;
    private final CurrentUserService currentUserService;

    /**
     * Affiche le dashboard principal avec tous les graphiques
     */
    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(required = false) LocalDate dateDebut,
            @RequestParam(required = false) LocalDate dateFin,
            Model model) {
        
        // Si pas de dates, prendre les 30 derniers jours
        if (dateDebut == null) {
            dateDebut = LocalDate.now().minusDays(30);
        }
        if (dateFin == null) {
            dateFin = LocalDate.now();
        }

        // Récupérer l'admin connecté
        var admin = currentUserService.getCurrentUser();
        
        // Statistiques générales
        model.addAttribute("totalIncidents", 
            dashboardService.countTotalIncidents(dateDebut, dateFin));
        model.addAttribute("incidentsResolus", 
            dashboardService.countIncidentsResolus(dateDebut, dateFin));
        model.addAttribute("incidentsEnCours", 
            dashboardService.countIncidentsEnCours(dateDebut, dateFin));
        model.addAttribute("delaiMoyenResolution", 
            dashboardService.calculerDelaiMoyenResolution(dateDebut, dateFin));

        // Données pour les graphiques
        model.addAttribute("incidentsParService", 
            dashboardService.getIncidentsParService(dateDebut, dateFin));
        model.addAttribute("incidentsParQuartier", 
            dashboardService.getIncidentsParQuartier(dateDebut, dateFin));
        model.addAttribute("delaiResolutionParService", 
            dashboardService.getDelaiResolutionParService(dateDebut, dateFin));
        model.addAttribute("incidentsParStatut", 
            dashboardService.getIncidentsParStatut(dateDebut, dateFin));
        model.addAttribute("incidentsParMois", 
            dashboardService.getIncidentsParMois(dateDebut, dateFin));

        // Filtres de date
        model.addAttribute("dateDebut", dateDebut);
        model.addAttribute("dateFin", dateFin);
        model.addAttribute("admin", admin);

        return "admin/dashboard";
    }

    /**
     * Export CSV des statistiques
     */
    @GetMapping("/dashboard/export/csv")
    public void exportCsv(
            @RequestParam(required = false) LocalDate dateDebut,
            @RequestParam(required = false) LocalDate dateFin,
            HttpServletResponse response) throws IOException {
        
        if (dateDebut == null) dateDebut = LocalDate.now().minusDays(30);
        if (dateFin == null) dateFin = LocalDate.now();

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", 
            "attachment; filename=rapport_incidents_" + LocalDate.now() + ".csv");

        dashboardService.exportCsv(dateDebut, dateFin, response.getWriter());
    }

    /**
     * Export PDF des statistiques
     */
    @GetMapping("/dashboard/export/pdf")
    public void exportPdf(
            @RequestParam(required = false) LocalDate dateDebut,
            @RequestParam(required = false) LocalDate dateFin,
            HttpServletResponse response) throws IOException {
        
        if (dateDebut == null) dateDebut = LocalDate.now().minusDays(30);
        if (dateFin == null) dateFin = LocalDate.now();

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", 
            "attachment; filename=rapport_incidents_" + LocalDate.now() + ".pdf");

        dashboardService.exportPdf(dateDebut, dateFin, response.getOutputStream());
    }
}
```

---

## 🔧 Étape 2 : Créer le Service de Statistiques {#etape2}

### 📄 Fichier : `src/main/java/com/ville/gestionincidents/service/dashboard/DashboardService.java`

```java
package com.ville.gestionincidents.service.dashboard;

import com.ville.gestionincidents.dto.dashboard.DelaiResolutionDto;
import com.ville.gestionincidents.dto.dashboard.IncidentParQuartierDto;
import com.ville.gestionincidents.dto.dashboard.IncidentParServiceDto;

import java.io.OutputStream;
import java.io.Writer;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface DashboardService {
    
    // ==================== COMPTEURS ====================
    long countTotalIncidents(LocalDate dateDebut, LocalDate dateFin);
    long countIncidentsResolus(LocalDate dateDebut, LocalDate dateFin);
    long countIncidentsEnCours(LocalDate dateDebut, LocalDate dateFin);
    double calculerDelaiMoyenResolution(LocalDate dateDebut, LocalDate dateFin);
    
    // ==================== DONNÉES POUR GRAPHIQUES ====================
    List<IncidentParServiceDto> getIncidentsParService(LocalDate dateDebut, LocalDate dateFin);
    List<IncidentParQuartierDto> getIncidentsParQuartier(LocalDate dateDebut, LocalDate dateFin);
    List<DelaiResolutionDto> getDelaiResolutionParService(LocalDate dateDebut, LocalDate dateFin);
    Map<String, Long> getIncidentsParStatut(LocalDate dateDebut, LocalDate dateFin);
    Map<String, Long> getIncidentsParMois(LocalDate dateDebut, LocalDate dateFin);
    
    // ==================== EXPORTS ====================
    void exportCsv(LocalDate dateDebut, LocalDate dateFin, Writer writer) throws Exception;
    void exportPdf(LocalDate dateDebut, LocalDate dateFin, OutputStream outputStream) throws Exception;
}
```

### 📄 Fichier : `src/main/java/com/ville/gestionincidents/service/dashboard/DashboardServiceImpl.java`

```java
package com.ville.gestionincidents.service.dashboard;

import com.ville.gestionincidents.dto.dashboard.DelaiResolutionDto;
import com.ville.gestionincidents.dto.dashboard.IncidentParQuartierDto;
import com.ville.gestionincidents.dto.dashboard.IncidentParServiceDto;
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
```

---

## 🔧 Étape 3 : Créer les DTOs pour les Graphiques {#etape3}

### 📄 Fichier : `src/main/java/com/ville/gestionincidents/dto/dashboard/IncidentParServiceDto.java`

```java
package com.ville.gestionincidents.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidentParServiceDto {
    private String nomService;
    private Long nombre;
}
```

### 📄 Fichier : `src/main/java/com/ville/gestionincidents/dto/dashboard/IncidentParQuartierDto.java`

```java
package com.ville.gestionincidents.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidentParQuartierDto {
    private String nomQuartier;
    private Long nombre;
}
```

### 📄 Fichier : `src/main/java/com/ville/gestionincidents/dto/dashboard/DelaiResolutionDto.java`

```java
package com.ville.gestionincidents.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DelaiResolutionDto {
    private String nomService;
    private Double delaiMoyen; // en jours
}
```

---

## 🔧 Étape 4 : Ajouter les Méthodes au Repository {#etape4}

### 📄 Fichier : `src/main/java/com/ville/gestionincidents/repository/IncidentRepository.java`

**Ajoutez ces méthodes à votre repository existant :**

```java
// ... code existant ...

// NOUVELLES MÉTHODES À AJOUTER :

// Compter les incidents entre deux dates
long countByDateDeclarationBetween(LocalDateTime debut, LocalDateTime fin);

// Compter les incidents par statut entre deux dates
long countByDateDeclarationBetweenAndStatut(
    LocalDateTime debut, LocalDateTime fin, StatutIncident statut);

// Compter les incidents par statuts multiples
long countByDateDeclarationBetweenAndStatutIn(
    LocalDateTime debut, LocalDateTime fin, List<StatutIncident> statuts);

// Trouver les incidents résolus entre deux dates
List<Incident> findByDateDeclarationBetweenAndStatut(
    LocalDateTime debut, LocalDateTime fin, StatutIncident statut);

// Compter les incidents par service entre deux dates
long countByServiceAndDateDeclarationBetween(
    ServiceMunicipal service, LocalDateTime debut, LocalDateTime fin);

// Compter les incidents par quartier entre deux dates
long countByQuartierAndDateDeclarationBetween(
    Quartier quartier, LocalDateTime debut, LocalDateTime fin);

// Trouver les incidents par service et statut entre deux dates
List<Incident> findByServiceAndDateDeclarationBetweenAndStatut(
    ServiceMunicipal service, LocalDateTime debut, LocalDateTime fin, StatutIncident statut);

// Trouver tous les incidents entre deux dates
List<Incident> findByDateDeclarationBetween(LocalDateTime debut, LocalDateTime fin);
```

**Note :** Spring Data JPA générera automatiquement ces méthodes à partir des noms. Assurez-vous que les imports sont corrects :
```java
import java.time.LocalDateTime;
import com.ville.gestionincidents.entity.ServiceMunicipal;
import com.ville.gestionincidents.entity.Quartier;
```

---

## 🔧 Étape 5 : Créer le Template Thymeleaf {#etape5}

### 📄 Fichier : `src/main/resources/templates/admin/dashboard.html`

**Voir la section complète dans le fichier suivant (trop long pour ici).**

**Structure du template :**
1. Header avec filtres de date
2. Cartes de statistiques (Total, Résolus, En cours, Délai moyen)
3. Graphiques Chart.js :
   - Graphique en barres : Incidents par service
   - Graphique en barres : Incidents par quartier
   - Graphique en ligne : Délai moyen de résolution
   - Graphique en camembert : Incidents par statut
   - Graphique en ligne : Évolution mensuelle
4. Boutons d'export (CSV, PDF)

---

## 🔧 Étape 6 : Ajouter Chart.js {#etape6}

### Option 1 : CDN (Recommandé pour débuter)

Dans votre template `dashboard.html`, ajoutez dans le `<head>` :

```html
<!-- Chart.js depuis CDN -->
<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
```

### Option 2 : Fichier Local

1. Téléchargez Chart.js depuis https://www.chartjs.org/
2. Placez le fichier dans `src/main/resources/static/js/chart.js`
3. Ajoutez dans le template :
```html
<script th:src="@{/js/chart.js}"></script>
```

### Exemple de Configuration de Graphique

```javascript
// Graphique : Incidents par Service
const ctxService = document.getElementById('chartService').getContext('2d');
const chartService = new Chart(ctxService, {
    type: 'bar',
    data: {
        labels: /*[[${incidentsParService.![nomService]}]]*/ [],
        datasets: [{
            label: 'Nombre d\'incidents',
            data: /*[[${incidentsParService.![nombre]}]]*/ [],
            backgroundColor: 'rgba(102, 126, 234, 0.8)',
            borderColor: 'rgba(102, 126, 234, 1)',
            borderWidth: 1
        }]
    },
    options: {
        responsive: true,
        plugins: {
            title: {
                display: true,
                text: 'Incidents par Service'
            }
        },
        scales: {
            y: {
                beginAtZero: true
            }
        }
    }
});
```

---

## 🔧 Étape 7 : Implémenter l'Export CSV {#etape7}

### 📄 Fichier : `src/main/java/com/ville/gestionincidents/util/export/CsvExportService.java`

```java
package com.ville.gestionincidents.util.export;

import com.ville.gestionincidents.dto.dashboard.DelaiResolutionDto;
import com.ville.gestionincidents.dto.dashboard.IncidentParQuartierDto;
import com.ville.gestionincidents.dto.dashboard.IncidentParServiceDto;
import org.springframework.stereotype.Service;

import java.io.Writer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class CsvExportService {

    private static final String CSV_SEPARATOR = ",";
    private static final String CSV_NEWLINE = "\n";

    public void exportDashboardData(
            LocalDate dateDebut,
            LocalDate dateFin,
            List<IncidentParServiceDto> incidentsParService,
            List<IncidentParQuartierDto> incidentsParQuartier,
            List<DelaiResolutionDto> delaiResolution,
            Writer writer) throws Exception {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // En-tête
        writer.append("RAPPORT D'INCIDENTS - ").append(LocalDate.now().format(formatter));
        writer.append(CSV_NEWLINE);
        writer.append("Période : ").append(dateDebut.format(formatter))
              .append(" au ").append(dateFin.format(formatter));
        writer.append(CSV_NEWLINE).append(CSV_NEWLINE);

        // Section : Incidents par Service
        writer.append("=== INCIDENTS PAR SERVICE ===").append(CSV_NEWLINE);
        writer.append("Service").append(CSV_SEPARATOR).append("Nombre").append(CSV_NEWLINE);
        for (IncidentParServiceDto dto : incidentsParService) {
            writer.append(escapeCsv(dto.getNomService()))
                  .append(CSV_SEPARATOR)
                  .append(String.valueOf(dto.getNombre()))
                  .append(CSV_NEWLINE);
        }
        writer.append(CSV_NEWLINE);

        // Section : Incidents par Quartier
        writer.append("=== INCIDENTS PAR QUARTIER ===").append(CSV_NEWLINE);
        writer.append("Quartier").append(CSV_SEPARATOR).append("Nombre").append(CSV_NEWLINE);
        for (IncidentParQuartierDto dto : incidentsParQuartier) {
            writer.append(escapeCsv(dto.getNomQuartier()))
                  .append(CSV_SEPARATOR)
                  .append(String.valueOf(dto.getNombre()))
                  .append(CSV_NEWLINE);
        }
        writer.append(CSV_NEWLINE);

        // Section : Délai de Résolution
        writer.append("=== DÉLAI MOYEN DE RÉSOLUTION PAR SERVICE ===").append(CSV_NEWLINE);
        writer.append("Service").append(CSV_SEPARATOR).append("Délai Moyen (jours)").append(CSV_NEWLINE);
        for (DelaiResolutionDto dto : delaiResolution) {
            writer.append(escapeCsv(dto.getNomService()))
                  .append(CSV_SEPARATOR)
                  .append(String.format("%.2f", dto.getDelaiMoyen()))
                  .append(CSV_NEWLINE);
        }

        writer.flush();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
```

---

## 🔧 Étape 8 : Implémenter l'Export PDF {#etape8}

### Ajouter la Dépendance dans `pom.xml`

```xml
<!-- iText pour génération PDF -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itextpdf</artifactId>
    <version>5.5.13.3</version>
</dependency>
```

### 📄 Fichier : `src/main/java/com/ville/gestionincidents/util/export/PdfExportService.java`

```java
package com.ville.gestionincidents.util.export;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.ville.gestionincidents.dto.dashboard.DelaiResolutionDto;
import com.ville.gestionincidents.dto.dashboard.IncidentParQuartierDto;
import com.ville.gestionincidents.dto.dashboard.IncidentParServiceDto;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfExportService {

    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);

    public void exportDashboardData(
            LocalDate dateDebut,
            LocalDate dateFin,
            long totalIncidents,
            long incidentsResolus,
            long incidentsEnCours,
            double delaiMoyen,
            List<IncidentParServiceDto> incidentsParService,
            List<IncidentParQuartierDto> incidentsParQuartier,
            List<DelaiResolutionDto> delaiResolution,
            OutputStream outputStream) throws Exception {

        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);
        document.open();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Titre
        Paragraph title = new Paragraph("RAPPORT D'INCIDENTS", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);

        // Période
        Paragraph period = new Paragraph(
            "Période : " + dateDebut.format(formatter) + " au " + dateFin.format(formatter),
            NORMAL_FONT);
        period.setAlignment(Element.ALIGN_CENTER);
        period.setSpacingAfter(20);
        document.add(period);

        // Statistiques générales
        document.add(createStatsTable(totalIncidents, incidentsResolus, incidentsEnCours, delaiMoyen));
        document.add(Chunk.NEWLINE);

        // Table : Incidents par Service
        document.add(new Paragraph("Incidents par Service", HEADER_FONT));
        document.add(createServiceTable(incidentsParService));
        document.add(Chunk.NEWLINE);

        // Table : Incidents par Quartier
        document.add(new Paragraph("Incidents par Quartier", HEADER_FONT));
        document.add(createQuartierTable(incidentsParQuartier));
        document.add(Chunk.NEWLINE);

        // Table : Délai de Résolution
        document.add(new Paragraph("Délai Moyen de Résolution par Service", HEADER_FONT));
        document.add(createDelaiTable(delaiResolution));

        document.close();
    }

    private PdfPTable createStatsTable(long total, long resolus, long enCours, double delai) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        addCell(table, "Total Incidents", String.valueOf(total), true);
        addCell(table, "Incidents Résolus", String.valueOf(resolus), true);
        addCell(table, "Incidents en Cours", String.valueOf(enCours), true);
        addCell(table, "Délai Moyen (jours)", String.format("%.2f", delai), true);

        return table;
    }

    private PdfPTable createServiceTable(List<IncidentParServiceDto> data) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        addHeaderCell(table, "Service");
        addHeaderCell(table, "Nombre");

        for (IncidentParServiceDto dto : data) {
            addCell(table, dto.getNomService(), String.valueOf(dto.getNombre()), false);
        }

        return table;
    }

    private PdfPTable createQuartierTable(List<IncidentParQuartierDto> data) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        addHeaderCell(table, "Quartier");
        addHeaderCell(table, "Nombre");

        for (IncidentParQuartierDto dto : data) {
            addCell(table, dto.getNomQuartier(), String.valueOf(dto.getNombre()), false);
        }

        return table;
    }

    private PdfPTable createDelaiTable(List<DelaiResolutionDto> data) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        addHeaderCell(table, "Service");
        addHeaderCell(table, "Délai Moyen (jours)");

        for (DelaiResolutionDto dto : data) {
            addCell(table, dto.getNomService(), String.format("%.2f", dto.getDelaiMoyen()), false);
        }

        return table;
    }

    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addCell(PdfPTable table, String label, String value, boolean isHeader) {
        Font font = isHeader ? HEADER_FONT : NORMAL_FONT;
        table.addCell(new PdfPCell(new Phrase(label, font)));
        table.addCell(new PdfPCell(new Phrase(value, font)));
    }
}
```

---

## 🔧 Étape 9 : Configuration de la Sécurité {#etape9}

### Modifier `src/main/java/com/ville/gestionincidents/security/SecurityConfig.java`

**Ajoutez cette ligne dans la méthode `filterChain` :**

```java
// 🔐 ACCÈS ADMIN
.antMatchers("/admin/**").hasRole("ADMIN")
```

**Avant la ligne :**
```java
// 🔐 ACCÈS CITOYEN
.antMatchers("/citoyen/**").hasRole("CITOYEN")
```

---

## 📝 Template HTML Complet

Le template complet `dashboard.html` est trop long pour ce document. Voici la structure à suivre :

1. **Header** : Titre + Filtres de date (formulaire)
2. **Cartes Statistiques** : 4 cartes avec icônes
3. **Graphiques Chart.js** : 5 graphiques différents
4. **Boutons Export** : CSV et PDF

**Points importants :**
- Utiliser `th:each` pour itérer sur les listes
- Utiliser `/*[[${variable}]]*/` pour injecter les données JavaScript
- Inclure Chart.js via CDN ou fichier local
- Ajouter des styles CSS pour un design moderne

---

## ✅ Checklist de Vérification

- [ ] Controller `AdminDashboardController` créé
- [ ] Service `DashboardService` et `DashboardServiceImpl` créés
- [ ] DTOs créés (IncidentParServiceDto, IncidentParQuartierDto, DelaiResolutionDto)
- [ ] Méthodes ajoutées au `IncidentRepository`
- [ ] Template `admin/dashboard.html` créé
- [ ] Chart.js intégré (CDN ou fichier local)
- [ ] Service `CsvExportService` créé
- [ ] Service `PdfExportService` créé (avec dépendance iText)
- [ ] Configuration de sécurité mise à jour
- [ ] Test de l'accès `/admin/dashboard`
- [ ] Test des exports CSV et PDF

---

## 🚀 Prochaines Étapes

1. **Tester le dashboard** : Accéder à `/admin/dashboard` avec un compte ADMIN
2. **Personnaliser les graphiques** : Modifier les couleurs, styles Chart.js
3. **Ajouter des filtres** : Par département, par catégorie d'incident
4. **Améliorer les exports** : Ajouter des graphiques dans le PDF

---

## 📚 Ressources Utiles

- **Chart.js Documentation** : https://www.chartjs.org/docs/latest/
- **iText PDF** : https://itextpdf.com/
- **Thymeleaf** : https://www.thymeleaf.org/documentation.html

---

**Note :** Ce guide vous donne la structure complète. Adaptez les styles CSS et les couleurs selon votre design existant.

