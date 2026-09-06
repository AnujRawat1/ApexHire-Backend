package com.ApexHire.careermentor.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSessionTitleRequest {

    @NotBlank(message = "Title cannot be blank")
    @Size(max = 120, message = "Session title cannot exceed 120 characters")
    private String title;
}
