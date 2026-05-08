package org.example.vibewall.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ReportRequested implements Serializable {
    @NotBlank(message = "Content cannot be blank")
    @Size(min = 1, max = 3000, message = "Content must be between 1 and 3000 characters")
    private String content;
}
