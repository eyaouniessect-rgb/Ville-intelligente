package com.ville.gestionincidents.util.export;

import com.ville.gestionincidents.dto.dashboardAdmin.DelaiResolutionDto;
import com.ville.gestionincidents.dto.dashboardAdmin.IncidentParQuartierDto;
import com.ville.gestionincidents.dto.dashboardAdmin.IncidentParServiceDto;
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