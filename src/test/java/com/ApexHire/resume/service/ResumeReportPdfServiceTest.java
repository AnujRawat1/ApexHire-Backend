package com.ApexHire.resume.service;

import com.ApexHire.resume.document.ResumeReport;
import com.ApexHire.resume.exception.ResumeAnalysisException;
import com.ApexHire.resume.enums.RecommendationPriority;
import com.ApexHire.resume.model.AnalysisResult;
import com.ApexHire.resume.model.AnalysisSection;
import com.ApexHire.resume.model.Recommendation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResumeReportPdfServiceTest {

    private ResumeReportPdfService pdfService;

    @BeforeEach
    void setUp() {
        // Set up Thymeleaf template engine with ClassLoaderTemplateResolver pointing to templates/
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resolver.setCacheable(false);

        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(resolver);

        pdfService = new ResumeReportPdfService(templateEngine);
    }

    @Test
    void generateReportPdf_WithCompleteData_GeneratesValidPdf() {
        AnalysisResult analysis = AnalysisResult.builder()
                .overallScore(88)
                .atsScore(92)
                .jobMatchScore(85)
                .summary("Demonstrates strong full stack experience with React, Java, and cloud technologies.")
                .sections(List.of(
                        AnalysisSection.builder()
                                .key("experience")
                                .title("Work Experience")
                                .score(90)
                                .summary("Strong chronological progression with quantified outcomes.")
                                .points(List.of("Led cloud migration reducing latency by 40%", "Architected microservices"))
                                .build(),
                        AnalysisSection.builder()
                                .key("skills")
                                .title("Technical Skills")
                                .score(85)
                                .summary("Comprehensive modern stack coverage.")
                                .points(List.of("Spring Boot, React, Docker, Kubernetes"))
                                .build()
                ))
                .strengths(List.of("Clear architectural achievements", "Strong quantitative metrics"))
                .weaknesses(List.of("Could elaborate on team mentoring"))
                .missingSkills(List.of("Terraform", "GraphQL"))
                .missingKeywords(List.of("CI/CD Automation", "Event-driven architecture"))
                .recommendations(List.of(
                        Recommendation.builder()
                                .title("Add Cloud Infrastructure Details")
                                .priority(RecommendationPriority.HIGH)
                                .detail("Highlight Terraform or AWS experience directly in experience bullets.")
                                .build()
                ))
                .improvements(List.of(
                        "Start each bullet point with strong action verbs",
                        "Include certifications section"
                ))
                .build();

        ResumeReport report = ResumeReport.builder()
                .id("report-123")
                .userId("user-456")
                .title("Senior Full Stack Engineer Resume")
                .fileName("john_doe_resume.pdf")
                .fileSize(1024L * 250) // 250 KB
                .targetRole("Senior Full Stack Engineer")
                .experienceLevel("Senior Level")
                .createdAt(LocalDateTime.now().minusDays(1))
                .analyzedAt(LocalDateTime.now())
                .analysis(analysis)
                .build();

        byte[] pdfBytes = pdfService.generateReportPdf(report);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 1000, "PDF should have substantial content size");

        // Save sample PDF for verification
        try {
            java.nio.file.Files.write(java.nio.file.Path.of("target/sample_resume_report.pdf"), pdfBytes);
        } catch (Exception ignored) {}

        // Verify PDF Magic Header "%PDF-"
        String header = new String(pdfBytes, 0, 5, StandardCharsets.US_ASCII);
        assertEquals("%PDF-", header, "Generated file should be a valid PDF document");
    }

    @Test
    void generateReportPdf_WithMinimalData_GeneratesValidPdf() {
        AnalysisResult analysis = AnalysisResult.builder()
                .overallScore(65)
                .atsScore(60)
                .jobMatchScore(null) // Optional job match score null
                .summary("Basic resume with key sections present.")
                .sections(List.of())
                .strengths(List.of())
                .weaknesses(List.of())
                .missingSkills(List.of())
                .missingKeywords(List.of())
                .recommendations(List.of())
                .improvements(List.of())
                .build();

        ResumeReport report = ResumeReport.builder()
                .id("report-minimal")
                .userId("user-456")
                .title(null) // Null title
                .fileName("minimal.pdf")
                .targetRole("Software Engineer")
                .experienceLevel("Entry Level")
                .analysis(analysis)
                .build();

        byte[] pdfBytes = pdfService.generateReportPdf(report);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 500);
        String header = new String(pdfBytes, 0, 5, StandardCharsets.US_ASCII);
        assertEquals("%PDF-", header);
    }

    @Test
    void generateReportPdf_WithNullReportOrAnalysis_ThrowsException() {
        assertThrows(ResumeAnalysisException.class, () -> pdfService.generateReportPdf(null));

        ResumeReport reportWithoutAnalysis = ResumeReport.builder().id("no-analysis").build();
        assertThrows(ResumeAnalysisException.class, () -> pdfService.generateReportPdf(reportWithoutAnalysis));
    }
}
