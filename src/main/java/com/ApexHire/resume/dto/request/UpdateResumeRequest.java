package com.ApexHire.resume.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateResumeRequest {
    @Size(max = 120, message = "Title must not exceed 120 characters")
    private String title;
}
