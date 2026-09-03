package com.ApexHire.resume.controller;

import com.ApexHire.resume.dto.response.PaginatedReportsResponse;
import com.ApexHire.resume.service.ResumeAnalysisService;
import com.ApexHire.resume.service.ResumeFileService;
import com.ApexHire.resume.service.ResumeService;
import com.ApexHire.user.model.User;
import com.ApexHire.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ResumeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ResumeAnalysisService resumeAnalysisService;

    @Mock
    private ResumeService resumeService;

    @Mock
    private ResumeFileService resumeFileService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ResumeController resumeController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(resumeController).build();
    }

    @Test
    void getAllReports_Authenticated_ReturnsOk() throws Exception {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "test@example.com", "password", List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        User mockUser = User.builder().id("user123").email("test@example.com").build();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(resumeService.getUserReports(anyString(), anyInt(), anyInt(), isNull(), isNull(), isNull(), anyString()))
                .thenReturn(PaginatedReportsResponse.builder()
                        .reports(Collections.emptyList())
                        .total(0)
                        .page(0)
                        .totalPages(0)
                        .build());

        mockMvc.perform(get("/api/resumes/reports")
                        .principal(auth)
                        .param("page", "0")
                        .param("limit", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllReports_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/resumes/reports")
                        .param("page", "0")
                        .param("limit", "10"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getReportById_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/resumes/reports/test-id"))
                .andExpect(status().isUnauthorized());
    }
}
