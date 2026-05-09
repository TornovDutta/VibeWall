package org.example.vibewall.model;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import jakarta.validation.constraints.NotBlank;
import java.util.Date;

@Document()
@Data
public class Feedback {

    private Integer id;

    @NotBlank
    @Field("feedback")
    private String content;

    private Date date;

    public Feedback() {
        this.date = new Date();
    }

    public Feedback(String content) {
        this.content = content;
        this.date = new Date();
    }

    public Feedback(Integer id, String content) {
        this.id = id;
        this.content = content;
        this.date = new Date();
    }
}
