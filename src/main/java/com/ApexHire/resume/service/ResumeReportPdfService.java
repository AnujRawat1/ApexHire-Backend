package com.ApexHire.resume.service;

import com.ApexHire.resume.document.ResumeReport;
import com.ApexHire.resume.exception.ResumeAnalysisException;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeReportPdfService {

    private final SpringTemplateEngine templateEngine;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd, yyyy · hh:mm a", Locale.ENGLISH);

    /**
     * Converts an analyzed ResumeReport into a beautifully styled PDF document byte array.
     *
     * @param report The analyzed resume report entity
     * @return Raw PDF bytes
     */
    public byte[] generateReportPdf(ResumeReport report) {
        if (report == null || report.getAnalysis() == null) {
            throw new ResumeAnalysisException("Cannot generate PDF: Report analysis data is missing");
        }

        try {
            log.info("Generating PDF report for reportId={}, title={}", report.getId(), report.getTitle());

            // 1. Prepare Thymeleaf template context
            Context context = new Context();
            context.setVariable("report", report);
            context.setVariable("analysis", report.getAnalysis());

            if (report.getAnalyzedAt() != null) {
                context.setVariable("formattedDate", report.getAnalyzedAt().format(DATE_FORMATTER));
            } else if (report.getCreatedAt() != null) {
                context.setVariable("formattedDate", report.getCreatedAt().format(DATE_FORMATTER));
            }

            if (report.getFileSize() != null && report.getFileSize() > 0) {
                context.setVariable("formattedFileSize", formatBytes(report.getFileSize()));
            }

            // 2. Render HTML string via Thymeleaf
            String htmlContent = templateEngine.process("resume-report", context);

            // 3. Clean and convert to W3C DOM via JSoup for OpenHTMLtoPDF compatibility
            Document jsoupDoc = Jsoup.parse(htmlContent, "UTF-8");
            jsoupDoc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
            org.w3c.dom.Document w3cDoc = new W3CDom().fromJsoup(jsoupDoc);

            // 4. Render PDF using OpenHTMLtoPDF
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();
                builder.withW3cDocument(w3cDoc, "/");
                builder.toStream(outputStream);
                builder.run();

                byte[] pdfBytes = outputStream.toByteArray();
                log.info("Successfully generated PDF report for reportId={}, size={} bytes",
                        report.getId(), pdfBytes.length);
                return pdfBytes;
            }

        } catch (Exception e) {
            log.error("Failed to generate PDF report for reportId={}: {}", report.getId(), e.getMessage(), e);
            throw new ResumeAnalysisException("Failed to generate PDF report: " + e.getMessage());
        }
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char unit = "KMGTPE".charAt(exp - 1);
        return String.format(Locale.ENGLISH, "%.1f %cB", bytes / Math.pow(1024, exp), unit);
    }
}
