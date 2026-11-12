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
public class ReplyQuestionRequest {

    @NotBlank(message = "Answer cannot be empty")
    @Size(max = 1000, message = "Answer cannot exceed 1000 characters")
    private String answer;

    @Builder.Default
    private boolean isPublic = true; // true = public on wall, false = private to asker
}
