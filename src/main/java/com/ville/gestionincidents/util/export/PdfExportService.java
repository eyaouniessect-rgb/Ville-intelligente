package com.ville.gestionincidents.util.export;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.ville.gestionincidents.dto.dashboardAdmin.DelaiResolutionDto;
import com.ville.gestionincidents.dto.dashboardAdmin.IncidentParQuartierDto;
import com.ville.gestionincidents.dto.dashboardAdmin.IncidentParServiceDto;
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