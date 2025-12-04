package org.example.vibewall.service.serviceImple;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.repo.ConfessionRepo;
import org.example.vibewall.repo.FeedbackRepo;
import org.example.vibewall.encryption.Encryption;
import org.example.vibewall.exception.ConfessionNotFoundException;
import org.example.vibewall.exception.FeedbackNotFoundException;
import org.example.vibewall.exception.PrincipalNotFollowException;
import org.example.vibewall.model.Confession;
import org.example.vibewall.model.Feedback;
import org.example.vibewall.service.FeedbackService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImple implements FeedbackService {

    private final ConfessionRepo confessionRepo;
    private final FeedbackRepo feedbackRepo;
    private final OpenAiService openAiService;
    private final Encryption encryption;


    @Override
    public String giveFeedback(String id, Feedback feedback)
            throws PrincipalNotFollowException, ConfessionNotFoundException {
        if (openAiService.safe(feedback.getFeedback())) {
            throw new PrincipalNotFollowException("Feedback violates safety policy");
        }
        Confession confession = confessionRepo.findById(id)
                .orElseThrow(() -> new ConfessionNotFoundException("Confession not found for ID: " + id));
        String encryptedFeedbackText = encryption.encode(feedback.getFeedback());
        Feedback encryptedFeedback = new Feedback(encryptedFeedbackText);
        confession.getFeedbacks().add(encryptedFeedback);
        confessionRepo.save(confession);
        return "Feedback added successfully!";
    }



    @Override
    public List<Feedback> get(String id) throws ConfessionNotFoundException {
        Confession confession = confessionRepo.findById(id)
                .orElseThrow(() -> new ConfessionNotFoundException("Confession not found for ID: " + id));


        return confession.getFeedbacks().stream()
                .map(fb -> {

                    String decodedText = encryption.decode(fb.getFeedback());


                    Feedback decodedFeedback = new Feedback();
                    decodedFeedback.setId(fb.getId());
                    decodedFeedback.setDate(fb.getDate());
                    decodedFeedback.setFeedback(decodedText);

                    return decodedFeedback;
                })
                .toList();
    }

    @Override
    public String update(String id, Feedback feedback) throws PrincipalNotFollowException, FeedbackNotFoundException {
        if (openAiService.safe(feedback.getFeedback())) {
            throw new PrincipalNotFollowException("Feedback violates safety policy");
        }
        Feedback existingFeedback = feedbackRepo.findById(id)
                .orElseThrow(() -> new FeedbackNotFoundException("Feedback not found for ID: " + id));
        String encryptedText = encryption.encode(feedback.getFeedback());
        existingFeedback.setFeedback(encryptedText);
        existingFeedback.setDate(new Date());
        feedbackRepo.save(existingFeedback);

        return "Feedback updated successfully!";
    }


    @Override
    public Feedback getById(String id) throws FeedbackNotFoundException {
        Feedback feedback = feedbackRepo.findById(id)
                .orElseThrow(() -> new FeedbackNotFoundException("Feedback not found for ID: " + id));
        String decodedText = encryption.decode(feedback.getFeedback());
        feedback.setFeedback(decodedText);

        return feedback;
    }



    @Override
    public String delete(String id) throws FeedbackNotFoundException {
        Feedback feedback = feedbackRepo.findById(id)
                .orElseThrow(() -> new FeedbackNotFoundException("Feedback not found for ID: " + id));

        feedbackRepo.deleteById(id);
        return "Feedback deleted successfully!";
    }

}
