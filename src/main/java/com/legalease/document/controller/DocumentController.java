package com.legalease.document.controller;

import com.legalease.common.security.CurrentUser;
import com.legalease.document.dto.PDFExportRequest;
import com.legalease.document.service.PDFExportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/docs")
@RequiredArgsConstructor
public class DocumentController {

    private final PDFExportService pdfExportService;

    @PostMapping("/export-pdf")
    @PreAuthorize("hasRole('LAWYER)")
    public ResponseEntity<byte[]> exportPDF(
            @Valid @RequestBody PDFExportRequest request, @CurrentUser UUID lawyerId){
        byte[] pdf = pdfExportService.exportPDF(request, lawyerId);

        String filename = request.getDocumentType().name().toLowerCase()
                + "_" + request.getDocumentId() + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(pdf);
    }
}
