package org.example.vibewall.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.vibewall.model.Feedback;

import java.io.Serializable;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfessionResponse implements Serializable {
    private String id;
    private String content;
    private List<Feedback> feedbacks;


}
