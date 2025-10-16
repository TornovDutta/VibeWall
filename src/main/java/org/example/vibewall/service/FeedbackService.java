package org.example.vibewall.service;

import org.example.vibewall.DAO.ConfessionRepo;
import org.example.vibewall.model.Confession;
import org.example.vibewall.model.Feedback;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FeedbackService {

    private final ConfessionRepo confessionRepo;

    public FeedbackService(ConfessionRepo confessionRepo) {
        this.confessionRepo=confessionRepo;
    }

    public void giveFeedback(String id,Feedback feedback) {
        Optional<Confession> confession=confessionRepo.findById(id);
        if(confession.isPresent()){
            Confession con=confession.get();
            con.getFeedbacks().add(feedback);
            confessionRepo.save(con);
        }else{
            System.out.println("Error");
        }

    }
}
