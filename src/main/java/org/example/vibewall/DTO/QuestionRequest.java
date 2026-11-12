package org.example.vibewall.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionRequest {

    @NotBlank(message = "Question content cannot be empty")
    @Size(max = 500, message = "Question cannot exceed 500 characters")
    private String content;

    @Builder.Default
    private boolean anonymous = true; // Default to true for anonymous ask

    private String askerName; // Optional custom pseudonym if visitor/unauthenticated
}
