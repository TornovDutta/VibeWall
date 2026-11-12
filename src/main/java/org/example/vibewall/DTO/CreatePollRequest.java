package org.example.vibewall.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePollRequest {

    @NotBlank(message = "Poll question cannot be empty")
    @Size(min = 5, max = 250, message = "Question must be between 5 and 250 characters")
    private String question;

    @NotEmpty(message = "At least two options are required")
    @Size(min = 2, max = 6, message = "Poll must have between 2 and 6 options")
    private List<@NotBlank(message = "Option cannot be blank") String> options;

    private String category;
}
