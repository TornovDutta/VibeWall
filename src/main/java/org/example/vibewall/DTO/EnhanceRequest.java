package org.example.vibewall.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnhanceRequest {

    @NotBlank(message = "Text cannot be blank")
    private String text;

    /**
     * Type of content being enhanced: "CONFESSION", "POLL", or "QUESTION"
     */
    @Builder.Default
    private String type = "CONFESSION";
}
