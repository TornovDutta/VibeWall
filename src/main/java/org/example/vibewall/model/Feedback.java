package org.example.vibewall.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collation = "feedback")
@Data
public class Feedback {
    @Id
    private Integer id;
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
