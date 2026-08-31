package com.ApexHire.resume.controller;

import com.ApexHire.resume.service.ResumeAnalysisService;
import com.ApexHire.resume.service.ResumeFileService;
import com.ApexHire.resume.service.ResumeService;
import com.ApexHire.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ResumeController.class)
class ResumeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ResumeAnalysisService resumeAnalysisService;

    @MockBean
    private ResumeService resumeService;

    @MockBean
    private ResumeFileService resumeFileService;

    @MockBean
    private UserRepository userRepository;

    @Test
    @WithMockUser
    void getAllReports_Authenticated_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/resumes/reports")
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
}
