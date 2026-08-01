package com.legalease.document.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import com.legalease.cases.model.Case;
import com.legalease.cases.repository.CaseRepository;
import com.legalease.client.model.Client;
import com.legalease.client.repository.ClientRepository;
import com.legalease.common.enums.PDFDocumentType;
import com.legalease.common.exception.ResourceNotFoundException;
import com.legalease.document.dto.PDFExportRequest;
import com.legalease.hearing.model.Hearing;
import com.legalease.hearing.repository.HearingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PDFExportService {

    private final CaseRepository caseRepository;
    private final ClientRepository clientRepository;
    private final HearingRepository hearingRepository;
//    private static final PdfFont BOLD_FONT;
//
//    static {
//        try {
//            BOLD_FONT = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
//        } catch (IOException e) {
//            throw new ExceptionInInitializerError(e);
//        }
//    }

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy");

    // Returns PDF as byte array — controller streams to client
    public byte[] exportPDF(PDFExportRequest request, UUID lawyerId) {
        return switch (request.getDocumentType()) {
            case CASE_SUMMARY  -> exportCaseSummary(request.getDocumentId(), lawyerId);
            case CLIENT_INTAKE -> exportClientIntake(request.getDocumentId(), lawyerId);
            case HEARING_LOG   -> exportHearingLog(request.getDocumentId(), lawyerId);
        };
    }

    // ── CASE SUMMARY PDF ──────────────────────────────────────────────────────

    private byte[] exportCaseSummary(UUID caseId, UUID lawyerId) {
        Case legalCase = caseRepository.findByIdAndLawyerId(caseId, lawyerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Case not found or access denied"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfDocument pdf = new PdfDocument(new PdfWriter(out));
        Document doc = new Document(pdf);
        PdfFont boldFont;
        try {
            boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load PDF font", e);
        }

        // Header
        addHeader(doc, "Case Summary Report", boldFont);
        addDivider(doc);

        // Case details table
        Table table = new Table(UnitValue.createPercentArray(new float[]{35, 65}))
                .setWidth(UnitValue.createPercentValue(100));

        addRow(table, "Case Title",   legalCase.getCaseTitle(),boldFont);
        addRow(table, "Case Type",    legalCase.getCaseType().name(),boldFont);
        addRow(table, "Status",       legalCase.getCaseStatus().name(),boldFont);
        addRow(table, "Case Number",  orNA(legalCase.getCaseNumber()),boldFont);
        addRow(table, "Court",        orNA(legalCase.getCourtName()),boldFont);
        addRow(table, "Lawyer",       legalCase.getLawyer().getFullName(),boldFont);
        addRow(table, "Filed On",     legalCase.getCreatedAt()
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy")),boldFont);

        if (legalCase.getNotes() != null) {
            addRow(table, "Notes", legalCase.getNotes() ,boldFont);
        }

        doc.add(table);
        addFooter(doc);
        doc.close();

        log.info("Case summary PDF generated for caseId: {}", caseId);
        return out.toByteArray();
    }

    // ── CLIENT INTAKE PDF ─────────────────────────────────────────────────────

    private byte[] exportClientIntake(UUID clientId, UUID lawyerId) {
        Client client = clientRepository.findByIdAndLawyerId(clientId, lawyerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Client not found or access denied"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfDocument pdf = new PdfDocument(new PdfWriter(out));
        Document doc = new Document(pdf);

        PdfFont boldFont;
        try {
            boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load PDF font", e);
        }

        addHeader(doc, "Client Intake Form",boldFont);
        addDivider(doc);

        Table table = new Table(UnitValue.createPercentArray(new float[]{35, 65}))
                .setWidth(UnitValue.createPercentValue(100));

        addRow(table, "Client Name",     client.getClientName(),boldFont);
        addRow(table, "Phone",           orNA(client.getPhone()),boldFont);
        addRow(table, "Email",           orNA(client.getEmail()),boldFont);
        addRow(table, "Opposing Party",  orNA(client.getOpposingParty()),boldFont);
        addRow(table, "Case",            client.getLegalCase().getCaseTitle(),boldFont);
        addRow(table, "Case Background", orNA(client.getCaseBackground()),boldFont);
        addRow(table, "AI Assisted",     client.isAiAssisted() ? "Yes" : "No",boldFont);
        addRow(table, "Intake Date",     client.getCreatedAt()
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy")),boldFont);

        doc.add(table);
        addFooter(doc);
        doc.close();

        return out.toByteArray();
    }

    // ── HEARING LOG PDF ───────────────────────────────────────────────────────

    private byte[] exportHearingLog(UUID hearingId, UUID lawyerId) {
        Hearing hearing = hearingRepository
                .findByIdAndLawyerId(hearingId, lawyerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Hearing not found or access denied"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfDocument pdf = new PdfDocument(new PdfWriter(out));
        Document doc = new Document(pdf);

        PdfFont boldFont;
        try {
            boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load PDF font", e);
        }
        addHeader(doc, "Hearing Log",boldFont);
        addDivider(doc);

        Table table = new Table(UnitValue.createPercentArray(new float[]{35, 65}))
                .setWidth(UnitValue.createPercentValue(100));

        addRow(table, "Case",         hearing.getLegalCase().getCaseTitle(),boldFont);
        addRow(table, "Lawyer",       hearing.getLawyer().getFullName(),boldFont);
        addRow(table, "Hearing Date", hearing.getHearingDate().format(DATE_FMT),boldFont);
        addRow(table, "Next Date",    hearing.getNextDate() != null
                ? hearing.getNextDate().format(DATE_FMT) : "N/A",boldFont);
        addRow(table, "Outcome",      orNA(hearing.getOutcome()),boldFont);
        addRow(table, "Action Items", orNA(hearing.getActionItems()),boldFont);
        addRow(table, "AI Assisted",  hearing.isAiAssisted() ? "Yes" : "No",boldFont);

        if (hearing.getRawNote() != null) {
            addRow(table, "Original Note", hearing.getRawNote(),boldFont);
        }

        doc.add(table);
        addFooter(doc);
        doc.close();

        return out.toByteArray();
    }

    // ── PDF HELPERS ───────────────────────────────────────────────────────────

    private void addHeader(Document doc, String title, PdfFont boldFont) {

        doc.add(new Paragraph("LegalEase AI by Kartik")
                .setFontSize(10)
                .setFontColor(ColorConstants.GRAY));

        doc.add(new Paragraph(title)
                .setFont(boldFont)
                .setFontSize(20)
                .setMarginBottom(5));

        doc.add(new Paragraph(
                "Generated on: " + LocalDate.now().format(DATE_FMT))
                .setFontSize(9)
                .setFontColor(ColorConstants.GRAY)
                .setMarginBottom(10));
    }

    private void addDivider(Document doc) {
        doc.add(new Paragraph("─".repeat(80))
                .setFontSize(8)
                .setFontColor(ColorConstants.LIGHT_GRAY)
                .setMarginBottom(10));
    }

    private void addRow(Table table,
                        String label,
                        String value,
                        PdfFont boldFont) {

        table.addCell(
                new Cell()
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .setPadding(6)
                        .add(new Paragraph(label)
                                .setFont(boldFont)
                                .setFontSize(10))
        );

        table.addCell(
                new Cell()
                        .setPadding(6)
                        .add(new Paragraph(orNA(value))
                                .setFontSize(10))
        );
    }

    private void addFooter(Document doc) {
        doc.add(new Paragraph("\nThis document was generated by LegalEase AI.")
                .setFontSize(8)
                .setFontColor(ColorConstants.GRAY)
                .setMarginTop(20));
    }

    private String orNA(String value) {
        return (value != null && !value.isBlank()) ? value : "N/A";
    }
}