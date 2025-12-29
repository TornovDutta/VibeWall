package org.example.vibewall.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import java.util.Date;

@Document()
@Data
public class Feedback {

    private Integer id;

    @NotBlank
    private String feedback;
    private Date date;


    public Feedback(){
        this.date=new Date();
    }
    public Feedback(String feedback){
        this.feedback=feedback;
        this.date=new Date();
    }
}
