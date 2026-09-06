package com.ApexHire.coverletter.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCoverLetterRequest {

    private String title;

    @NotBlank(message = "Cover letter content cannot be blank")
    private String content;
}
