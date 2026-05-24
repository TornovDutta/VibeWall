package org.example.vibewall.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackRequest implements Serializable {
    @NotBlank(message = "Content cannot be blank")
    @Size(min = 1, max = 2000, message = "Content must be between 1 and 2000 characters")
    private String content;
}
