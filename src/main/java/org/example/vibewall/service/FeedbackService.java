package org.example.vibewall.service;

import org.example.vibewall.DAO.FeedbackRepo;
import org.example.vibewall.model.Feedback;
import org.springframework.stereotype.Service;

@Service
public class FeedbackService {
    private final FeedbackRepo repo;

    public FeedbackService(FeedbackRepo repo) {
        this.repo = repo;
    }

    public void giveFeedback(Feedback feedback) {
        repo.save(feedback);

    }
}
